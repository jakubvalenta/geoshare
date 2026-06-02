package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.TestServer
import page.ooooo.geoshare.TestServerParams
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.configureServer
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.getAndAssumeTestServer
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.waitForAppToBeVisible

@RunWith(Parameterized::class)
class GoogleMapsPlaceApiInputBehaviorTest(private val testServerParams: TestServerParams) {
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

    /**
     * Stores whether the current build variant supports HTML parsing or not. This way we can have one test class for
     * both build variants and all the tested links in one function.
     */
    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    private val htmlParsingSupported = BuildConfig.FLAVOR == "free"

    @Before
    fun setUp() {
        testServer = testServerParams.getAndAssumeTestServer()
    }

    @Test
    fun testGoogleMapsPlaceApiInput() = uiAutomator {
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
                WGS84Point(47.5951518, -122.3316394, name = "Seattle Stadium", source = Source.URI)
            } else {
                WGS84Point(name = "Lumen Field", source = Source.URI)
            },
            "https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=Lumen%20Field&api=1",
            fallbackPoint = if (testServer is TestServer.Configured) {
                null
            } else if (htmlParsingSupported) {
                WGS84Point(47.5951518, -122.3316394, name = "Lumen Field", source = Source.URI)
            } else {
                null
            },
        )
    }
}
