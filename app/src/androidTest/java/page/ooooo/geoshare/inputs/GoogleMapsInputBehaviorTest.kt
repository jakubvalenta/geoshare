package page.ooooo.geoshare.inputs

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.uiAutomator
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.AssumptionViolatedException
import org.junit.Test
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.NotEmulator
import page.ooooo.geoshare.assumeDomainResolvable
import page.ooooo.geoshare.assumeHttpGetReturnsStatus
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.configureGoogleMapsServer
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.waitForAppToBeVisible

/**
 * Tests the Google Maps input.
 *
 * The test expects different results in the free variant, where HTML parsing is enabled; and it the pro variant, where
 * it's not enabled. Which variant is being tested is stored in the [htmlParsingSupported] property. Instead of having a
 * property like that, we could split the test in the androidTestFree and androidTestPro source sets, but it's easier to
 * maintain the test when all the tested links are in one place.
 */
class GoogleMapsInputBehaviorTest {
    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    private val htmlParsingSupported = BuildConfig.FLAVOR == "free"

    @Test
    fun googleMapsUriInput() = uiAutomator {
        // Coordinates in data
        testUri(
            WGS84Point(
                52.4083009, 16.929066199999998,
                name = "Poznań Old Town, 61-001 Poznań, Poland",
                source = Source.URI,
            ),
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=!4m6!3m5!1s0x47045b49399cf863:0xf61cbcaacd7d3070!7e2!8m2!3d52.4083009!4d16.929066199999998"
        )

        // Coordinates in data within mainland China
        testUri(
            GCJ02Point(39.9168038, 116.3971621, z = 17.0, name = "Forbidden City", source = Source.URI),
            @Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/Forbidden+City/@39.9165742,116.3945834,17z/data=!4m7!3m6!1s0x35f052e94515d43d:0x674e2bd4dd3079f!8m2!3d39.9168038!4d116.3971621!15sCg5mb3JiaWRkZW4gY2l0eVoQIg5mb3JiaWRkZW4gY2l0eZIBEnRvdXJpc3RfYXR0cmFjdGlvbuABAA!16zL20vMGowYjI?entry=tts&g_ep=EgoyMDI2MDMwOS4wIPu8ASoASAFQAw%3D%3D&skid=5f340da1-a0d3-4b1c-bc05-7f90cfbd502a",
        )

        @Suppress("SpellCheckingInspection")
        // Coordinates within mainland China on the Yangshan port island
        testUri(
            GCJ02Point(30.600649446449268, 122.13324202346543, source = Source.URI),
            "https://www.google.com/maps/?ll=30.600649446449268,122.13324202346543",
        )

        // Coordinates within Hong Kong
        testUri(
            WGS84Point(22.301015146333217, 114.17126075831801, source = Source.URI),
            "https://www.google.com/maps/?ll=22.301015146333217,114.17126075831801",
        )

        // Coordinates within Taiwan
        testUri(
            WGS84Point(24.146155957428395, 120.43731802970248, source = Source.URI),
            "https://www.google.com/maps/?ll=24.146155957428395,120.43731802970248",
        )

        // Coordinates within western Japan
        testUri(
            WGS84Point(33.2868514264416, 129.46775525445975, source = Source.URI),
            "https://www.google.com/maps/?ll=33.2868514264416,129.46775525445975",
        )

        // Coordinates and query (address)
        testUri(
            WGS84Point(40.785091, -73.968285, z = 15.0, name = "Central Park", source = Source.MAP_CENTER),
            "https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z",
        )

        // Coordinates and query (business)
        testUri(
            WGS84Point(
                50.4484901, 8.0469828,
                name = @Suppress("SpellCheckingInspection") "Änderungsschneiderei Hadamar, Schulstraße 3, 65589 Hadamar",
                source = Source.MAP_CENTER,
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/place/%C3%84nderungsschneiderei+Hadamar,+Schulstra%C3%9Fe+3,+65589+Hadamar/@50.4484901,8.0469828,3a,54.9y,5.97h,62.4t/data=!3m5!1e1!3m3!1szFIo-lmR3NWYzi_eWhPHFQ!2e0!6shttps:%2F%2Fstreetviewpixels-pa.googleapis.com%2Fv1%2Fthumbnail%3Fpanoid%3DzFIo-lmR3NWYzi_eWhPHFQ%26w%3D900%26h%3D600%26ll%3D0.0,0.0%26yaw%3D5.0%26pitch%3D28.0%26cb_client%3Dgmm.iv.android!4m2!3m1!1s0x47bc3266a8f3bb4b:0x96d1177f5ecfc466?utm_source=mstt_0&skid=368dc137-203a-4698-9ed3-b974e7bee770&g_st=aw",
        )

        // Directions
        testUri(
            persistentListOf(
                WGS84Point(
                    52.4858222, 13.4236883,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 1, 12049 Berlin, Germany",
                    source = Source.URI,
                ),
                WGS84Point(
                    52.4881038, 13.4255518,
                    name = @Suppress("SpellCheckingInspection") "Weserstr. 1, 12047 Berlin, Germany",
                    source = Source.URI,
                ),
                WGS84Point(
                    52.4807739, 13.4300356,
                    name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany",
                    z = 16.0,
                    source = Source.URI,
                ),
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2",
        )

        // Map center
        testUri(
            WGS84Point(52.5067296, 13.2599309, z = 11.0, name = "Berlin, Germany", source = Source.MAP_CENTER),
            "https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/",
        )

        // API
        testUri(
            WGS84Point(52.5067296, 13.2599309, source = Source.MAP_CENTER),
            "https://www.google.com/maps?center=52.5067296,13.2599309",
        )
    }

    @Test
    fun googleMapsShortLinkInput() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)

        // Short link within western Japan
        testUri(
            WGS84Point(34.5945482, 133.7583428, z = 17.0, name = "Steak no Don", source = Source.URI),
            "https://maps.app.goo.gl/mBtbC6qXLK2baGTV9",
        )

        // Short link within mainland China
        testUri(
            GCJ02Point(39.920439, 116.331538, source = Source.URI),
            "https://maps.app.goo.gl/FP3EV7tTUKYbmcVp7",
        )
    }

    @Test
    @NotEmulator
    fun googleMapsAddressApiInput_serverGeoShare() = uiAutomator {
        runBlocking {
            assumeHttpGetReturnsStatus("https://api.geoshare-app.net/", HttpStatusCode.NotFound)
        }
        val server = Server(
            name = "GeoShare Proxy",
            description = "With Google Maps backend",
            urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/address/{q}",
            authType = ServerAuthType.ATTESTATION,
            challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
            loginUrl = "https://api.geoshare-app.net/v1/auth/login",
            registerUrl = "https://api.geoshare-app.net/v1/auth/register",
        )
        testGoogleMapsAddressApiInput(apiEnabled = true, server = server)
    }

    @Test
    @NotEmulator
    fun googleMapsAddressApiInput_serverGeoShareLocal() = uiAutomator {
        runBlocking {
            assumeHttpGetReturnsStatus("http://127.0.0.1:8080", HttpStatusCode.NotFound)
        }
        val server = Server(
            name = "Google Maps",
            urlTemplate = "http://127.0.0.1:8080/v1/google-maps/geocode/address/{q}",
            authType = ServerAuthType.ATTESTATION,
            challengeUrl = "http://127.0.0.1:8080/v1/auth/challenge",
            loginUrl = "http://127.0.0.1:8080/v1/auth/login",
            registerUrl = "http://127.0.0.1:8080/v1/auth/register",
        )
        testGoogleMapsAddressApiInput(apiEnabled = true, server = server)
    }

    @Test
    fun googleMapsAddressApiInput_serverGoogleApis() = uiAutomator {
        val apiKey = InstrumentationRegistry.getArguments().getString(SERVER_API_KEY_ARG)
            ?: throw AssumptionViolatedException("This test only works when the instrumentation extra argument $SERVER_API_KEY_ARG is set")
        runBlocking {
            assumeHttpGetReturnsStatus("https://geocode.googleapis.com", HttpStatusCode.NotFound)
        }
        val server = Server(
            name = "Google Maps",
            urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
            authType = ServerAuthType.API_KEY,
            apiKey = apiKey,
            apiKeyHeader = "X-Goog-Api-Key",
        )
        testGoogleMapsAddressApiInput(apiEnabled = true, server = server)
    }

    @Test
    fun googleMapsAddressApiInput_serverOff() = uiAutomator {
        testGoogleMapsAddressApiInput(apiEnabled = false, server = null)
    }

    private fun testGoogleMapsAddressApiInput(apiEnabled: Boolean, server: Server?) = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        configureConnectionPermissionPreference(Permission.ALWAYS)
        configureGoogleMapsApiPreference(apiEnabled)
        if (server != null) {
            configureGoogleMapsServer(server)
        }

        // Search
        testUri(
            if (apiEnabled) {
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
        )

        // Short links with coordinates in HTML
        testUri(
            if (apiEnabled) {
                WGS84Point(
                    51.1982447, 6.4389493,
                    name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    51.1982447, 6.4389493,
                    name = @Suppress("SpellCheckingInspection") "Konditorei+Heinemann",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                    source = Source.URI,
                )
            },
            "https://maps.app.goo.gl/v4MDUi9mCrh3mNjz8",
        )
        testUri(
            if (apiEnabled) {
                WGS84Point(
                    44.4490541, 26.0888398,
                    name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    source = Source.API,
                )
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
            if (apiEnabled) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide, Columbiadamm 160, 12049 Berlin, Germany",
                    source = Source.API,
                )
            } else if (htmlParsingSupported) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Hasenheide Park",
                    source = Source.URI,
                )
            } else {
                WGS84Point(
                    name = @Suppress("SpellCheckingInspection") "Volkspark Hasenheide, Columbiadamm 160, 12049 Berlin, Germany",
                    source = Source.URI,
                )
            },
            "https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6",
            fallbackPoint = if (apiEnabled) {
                null
            } else if (htmlParsingSupported) {
                WGS84Point(
                    52.4842015, 13.4167277,
                    name = @Suppress("SpellCheckingInspection") "Parc public Hasenheide",
                    source = Source.URI,
                )
            } else {
                null
            }
        )

        // Place
        testUri(
            if (apiEnabled) {
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
            if (apiEnabled) {
                WGS84Point(43.7481582, -79.6332316, name = "2088 Albion Rd @43.7481,-79.6332", source = Source.API)
            } else if (htmlParsingSupported) {
                WGS84Point(43.7481, -79.6332, name = "2088 Albion Rd @43.7481,-79.6332", source = Source.HTML)
            } else {
                WGS84Point(name = "2088 Albion Rd @43.7481,-79.6332", source = Source.URI)
            },
            @Suppress("SpellCheckingInspection") "https://maps.google.com/maps?f=d&daddr=2088%20Albion%20Rd+@43.7481,-79.6332&doflg=ptm&navigate=yes",
        )

        // Directions with geocode parameter, which can get stuck at intermediate URI with zero coordinates during web parsing
        testUri(
            if (apiEnabled) {
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
            fallbackPoint = if (apiEnabled) {
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
    }

    @Test
    fun googleMapsPlaceListInput() = uiAutomator {
        if (htmlParsingSupported) {
            testUri(
                persistentListOf(
                    GCJ02Point(59.1293656, 11.4585672, source = Source.JAVASCRIPT),
                    GCJ02Point(59.4154007, 11.659710599999999, source = Source.JAVASCRIPT),
                    GCJ02Point(59.3443991, 11.672637, source = Source.JAVASCRIPT),
                    GCJ02Point(59.2557409, 11.5857853, source = Source.JAVASCRIPT),
                    GCJ02Point(59.1579458, 11.7337507, source = Source.JAVASCRIPT),
                    GCJ02Point(59.229344899999994, 11.6892173, source = Source.JAVASCRIPT),
                    GCJ02Point(59.2999243, 11.6587237, source = Source.JAVASCRIPT),
                    GCJ02Point(59.147731699999994, 11.550661199999999, source = Source.JAVASCRIPT),
                ),
                "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
            )
        } else {
            testUriFails(
                setOf(
                    "Place lists are not supported",
                    // TODO Add French translation
                ),
                "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
            )
        }
    }

    @Test
    @NotEmulator
    fun googleMapsHtmlInput_googleSearch() = uiAutomator {
        assumeTrue(
            "This test currently fails, because Google returns a captcha, even though we only run the test on a real device",
            false,
        )

        // Google Search
        testUri(
            GCJ02Point(27.765028, -15.600889, source = Source.JAVASCRIPT),
            "https://g.co/kgs/91UYXud",
        )
    }

    private companion object {
        private const val SERVER_API_KEY_ARG = "SERVER_API_KEY"
    }
}
