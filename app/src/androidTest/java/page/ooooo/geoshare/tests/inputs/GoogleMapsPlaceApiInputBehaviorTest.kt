package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.TestServer
import page.ooooo.geoshare.tests.TestServerParams
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.configureConnectionPermissionPreference
import page.ooooo.geoshare.tests.configureServer
import page.ooooo.geoshare.tests.getAndAssumeTestServer
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

@RunWith(Parameterized::class)
class GoogleMapsPlaceApiInputBehaviorTest(private val testServerParams: TestServerParams) : InputBehaviorTest {
    private lateinit var testServer: TestServer

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun configs() = listOf(
            TestServerParams.Configured(
                baseUrl = "https://api.geoshare-app.net",
                name = "GeoShare Remote",
                urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/places/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                registerUrl = "https://api.geoshare-app.net/v1/auth/register",
            ),
            TestServerParams.Configured(
                baseUrl = "http://127.0.0.1:8080",
                name = "GeoShare Local",
                urlTemplate = "http://127.0.0.1:8080/v1/google-maps/geocode/places/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "http://127.0.0.1:8080/v1/auth/challenge",
                loginUrl = "http://127.0.0.1:8080/v1/auth/login",
                registerUrl = "http://127.0.0.1:8080/v1/auth/register",
            ),
            TestServerParams.Configured(
                baseUrl = "https://geocode.googleapis.com",
                name = "Google Maps Apis",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/places/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
            ),
            TestServerParams.None,
        )
    }

    @Before
    fun setUp() {
        testServer = testServerParams.getAndAssumeTestServer()
    }

    @Test
    fun googleMapsPlaceApiInput_online() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)
        configureServer(testServer)

        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(47.5951518, -122.3316394, name = "Lumen Field", source = Source.API)
            } else if (htmlParsingSupported) {
                WGS84Point(47.5951518, -122.3316394, name = "Lumen Field", source = Source.URI)
            } else {
                WGS84Point(name = "Lumen Field", source = Source.URI)
            },
            "https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=Lumen%20Field&api=1",
            fallbackNames = if (testServer is TestServer.Configured) {
                emptySet()
            } else if (htmlParsingSupported) {
                setOf(
                    "Seattle Stadium",
                    "Seattle-Stadion",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Stade de Seattle",
                )
            } else {
                emptySet()
            },
        )
    }
}
