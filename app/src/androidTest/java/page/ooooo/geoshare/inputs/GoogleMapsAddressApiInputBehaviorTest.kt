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
import page.ooooo.geoshare.configureConnectionPermissionPreference
import page.ooooo.geoshare.configureServer
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.getAndAssumeTestServer
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.testUri
import page.ooooo.geoshare.testUriAnyCoordinates
import page.ooooo.geoshare.testUriFails
import page.ooooo.geoshare.waitForAppToBeVisible

@RunWith(Parameterized::class)
class GoogleMapsAddressApiInputBehaviorTest(private val testServerParams: TestServerParams) {
    private lateinit var testServer: TestServer

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun configs() = listOf(
            TestServerParams.Configured(
                baseUrl = "https://api.geoshare-app.net",
                name = "GeoShare Remote",
                urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/address/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                registerUrl = "https://api.geoshare-app.net/v1/auth/register",
            ),
            TestServerParams.Configured(
                baseUrl = "http://127.0.0.1:8080",
                name = "GeoShare Local",
                urlTemplate = "http://127.0.0.1:8080/v1/google-maps/geocode/address/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "http://127.0.0.1:8080/v1/auth/challenge",
                loginUrl = "http://127.0.0.1:8080/v1/auth/login",
                registerUrl = "http://127.0.0.1:8080/v1/auth/register",
            ),
            TestServerParams.Configured(
                baseUrl = "https://geocode.googleapis.com",
                name = "Google Maps Apis",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
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
    fun testGoogleMapsAddressApiInput() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)
        configureServer(testServer)

        // Search
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(
                    51.0657922, 13.7555827,
                    name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    51.0657922, 13.7555827,
                    name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
                    source = Source.URI,
                )
            },
            "https://www.google.com/maps/search/?api=1&query=Louisenstra%C3%9Fe%2060,%2001099%20Dresden",
            fallbackPoint = if (testServer is TestServer.Configured) {
                null
            } else if (htmlParsingSupported) {
                WGS84Point(
                    51.0657922, 13.7555827,
                    name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Neustadt",
                    source = Source.URI,
                )
            } else {
                null
            },
        )

        // Short links with coordinates in HTML
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(
                    51.1982447, 6.4389493,
                    name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    51.1982447, 6.4389493,
                    name = @Suppress("SpellCheckingInspection") "Café Heinemann",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                    source = Source.URI,
                )
            },
            "https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8",
            if (testServer is TestServer.Configured) {
                null
            } else if (htmlParsingSupported) {
                WGS84Point(
                    51.1982447, 6.4389493,
                    name = @Suppress("SpellCheckingInspection") "Konditorei+Heinemann",
                    source = Source.URI,
                )
            } else {
                null
            },
        )
        testUri(
            if (testServer is TestServer.Configured) {
                if ((testServer as TestServer.Configured).server.urlTemplate.startsWith("https://geocode.googleapis.com/")) {
                    // Expect slightly different coordinates for this point when running directly against Google Maps
                    // API instead of against GeoShare Server. It should be investigated why Google Maps returns
                    // different coordinates than GeoShare Server in this case.
                    WGS84Point(
                        44.4490666, 26.0888873,
                        name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                        source = Source.API,
                    )
                } else {
                    WGS84Point(
                        44.4490541, 26.0888398,
                        name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                        source = Source.API,
                    )
                }
            } else if (htmlParsingSupported) {
                WGS84Point(
                    44.4490541, 26.0888398,
                    name = "RAI - Romantic & Intimate",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    source = Source.URI,
                )
            },
            "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
        )
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide, Columbiadamm 160, 12049 Berlin, Germany",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide, Columbiadamm 160, 12049 Berlin, Germany",
                    source = Source.URI,
                )
            },
            "https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6",
            fallbackPoint = if (testServer is TestServer.Configured) {
                null
            } else if (htmlParsingSupported) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Hasenheide Park", // FIXME Parc public Hasenheide
                    source = Source.URI,
                )
            } else {
                null
            },
        )

        // Place
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(
                    52.4834254, 13.4245399,
                    name = @Suppress("SpellCheckingInspection") "Hermannstr. 20, Berlin",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    52.4834254, 13.4245399,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 20, 12049 Berlin",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Hermannstr. 20, Berlin",
                    source = Source.URI,
                )
            },
            "https://www.google.com/maps/place/Hermannstr.+20,+Berlin/"
        )

        // Directions address
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(43.7481582, -79.6332316, name = "2088 Albion Rd", source = Source.API)
            } else if (htmlParsingSupported) {
                WGS84Point(43.7481, -79.6332, name = "2088 Albion Rd @43.7481,-79.6332", source = Source.HTML)
            } else {
                WGS84Point(name = "2088 Albion Rd @43.7481,-79.6332", source = Source.URI)
            },
            @Suppress("SpellCheckingInspection") "https://maps.google.com/maps?f=d&daddr=2088%20Albion%20Rd+@43.7481,-79.6332&doflg=ptm&navigate=yes",
        )

        // Directions with geocode parameter, which can get stuck at intermediate URI with zero coordinates during web parsing
        testUri(
            if (testServer is TestServer.Configured) {
                WGS84Point(
                    40.6400258, 22.9589454,
                    z = 6.0,
                    name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Greece",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    40.6400537, 22.9589055,
                    z = 6.0,
                    name = @Suppress("SpellCheckingInspection") "Box+now+Ακροπόλεως+65,+Akropoleos+65,+Thessaloniki+546+34,+Greece",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Greece",
                    source = Source.URI,
                )
            },
            "https://maps.google.com/maps?oe=utf-8&client=firefox-b&um=1&ie=UTF-8&fb=1&gl=fr&sa=X&geocode=KWmqxjsAOagUMaSMgMRdOas1&daddr=Akropoleos+65,+Thessaloniki+546+34,+Gr%C3%A8ce",
            fallbackPoint = if (testServer is TestServer.Configured) {
                WGS84Point(
                    40.6400258, 22.9589454,
                    z = 6.0,
                    name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Grèce",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    40.6400537, 22.9589055,
                    z = 6.0,
                    name = @Suppress("SpellCheckingInspection") "Box+now+Ακροπόλεως+65,+Akropoleos+65,+Thessaloniki+546+34,+Grèce",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Akropoleos 65, Thessaloniki 546 34, Grèce",
                    source = Source.URI,
                )
            },
        )

        // No points found
        if (testServer is TestServer.Configured) {
            testUriFails(
                setOf(
                    "No points found",
                    @Suppress("SpellCheckingInspection") "Aucun point trouvé",
                ),
                "https://www.google.com/maps/place//",
            )
        } else if (htmlParsingSupported) {
            // Google Maps HTML shows coordinates of the IP address that the request came from
            testUriAnyCoordinates("https://www.google.com/maps/place//")
        } else {
            testUriFails(
                setOf(
                    "This link is not supported"
                    // TODO Add French translation
                ),
                "https://www.google.com/maps/place//",
            )
        }
    }
}
