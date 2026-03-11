package page.ooooo.geoshare.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.NotEmulator
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.WGS84Point

class GoogleMapsInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun googleMaps() {
        // Coordinates in data
        testUri(
            WGS84Point(52.41432182187929, 16.923708885113697, name = "Poznań Old Town, 61-001 Poznań, Poland"),
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=!4m6!3m5!1s0x47045b49399cf863:0xf61cbcaacd7d3070!7e2!8m2!3d52.4083009!4d16.929066199999998"
        )

        // Coordinates in data in China
        testUri(
            WGS84Point(39.916947439886265, 116.39073095659673),
            "https://www.google.com/maps/place/Forbidden+City/@39.9165742,116.3945834,17z/data=!4m7!3m6!1s0x35f052e94515d43d:0x674e2bd4dd3079f!8m2!3d39.9168038!4d116.3971621!15sCg5mb3JiaWRkZW4gY2l0eVoQIg5mb3JiaWRkZW4gY2l0eZIBEnRvdXJpc3RfYXR0cmFjdGlvbuABAA!16zL20vMGowYjI?entry=tts&g_ep=EgoyMDI2MDMwOS4wIPu8ASoASAFQAw%3D%3D&skid=5f340da1-a0d3-4b1c-bc05-7f90cfbd502a",
        )

        // Coordinates and query (address)
        testUri(
            WGS84Point(40.785091, -73.968285, z = 15.0, name = "Central Park"),
            "https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z",
        )

        // Coordinates and query (business)
        testUri(
            WGS84Point(
                50.448539371305024,
                8.047084600532981,
                name = @Suppress("SpellCheckingInspection") "Änderungsschneiderei Hadamar, Schulstraße 3, 65589 Hadamar",
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/place/%C3%84nderungsschneiderei+Hadamar,+Schulstra%C3%9Fe+3,+65589+Hadamar/@50.4484901,8.0469828,3a,54.9y,5.97h,62.4t/data=!3m5!1e1!3m3!1szFIo-lmR3NWYzi_eWhPHFQ!2e0!6shttps:%2F%2Fstreetviewpixels-pa.googleapis.com%2Fv1%2Fthumbnail%3Fpanoid%3DzFIo-lmR3NWYzi_eWhPHFQ%26w%3D900%26h%3D600%26ll%3D0.0,0.0%26yaw%3D5.0%26pitch%3D28.0%26cb_client%3Dgmm.iv.android!4m2!3m1!1s0x47bc3266a8f3bb4b:0x96d1177f5ecfc466?utm_source=mstt_0&g_ep=CAESBzI1LjM3LjAYACCBgQEqogEsOTQyNjc3MjcsOTQyNzU0MDcsOTQyODQ0NzgsOTQyMjMyOTksOTQyMTY0MTMsOTQyODA1NzYsOTQyMTI0OTYsOTQyMDczOTQsOTQyMDc1MDYsOTQyMDg1MDYsOTQyMTc1MjMsOTQyMTg2NTMsOTQyMjk4MzksOTQyNzUxNjgsOTQyNjI3MzMsNDcwODQzOTMsOTQyMTMyMDAsOTQyNTgzMjVCAkRF&skid=368dc137-203a-4698-9ed3-b974e7bee770&g_st=aw",
        )

        // Directions
        testUri(
            persistentListOf(
                GCJ02Point(
                    52.485822218541934,
                    13.423688319399824,
                    name = @Suppress("SpellCheckingInspection") "Hermannstraße 1, 12049 Berlin, Germany",
                ),
                GCJ02Point(
                    52.48810376596012,
                    13.425551838089799,
                    name = @Suppress("SpellCheckingInspection") "Weserstr. 1, 12047 Berlin, Germany",
                ),
                GCJ02Point(
                    52.48077002937864,
                    13.430029990871251,
                    name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany",
                    z = 16.0,
                ),
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2",
        )

        // Center
        testUri(
            WGS84Point(52.5067296, 13.2599309),
            "https://www.google.com/maps?center=52.5067296,13.2599309",
        )
    }

    @Test
    fun googleMapsHtml() = runBlocking {
        assumeDomainResolvable("maps.google.com")

        // Launch app and close intro
        launchApplication()
        closeIntro()
        setUserPreferenceConnectionPermissionToAlways()

        // Search
        testUri(
            WGS84Point(
                51.0657922,
                13.7555827,
                name = @Suppress("SpellCheckingInspection") "Louisenstraße 60, 01099 Dresden",
            ),
            "https://www.google.com/maps/search/?api=1&query=Louisenstra%C3%9Fe%2060,%2001099%20Dresden",
        )

        // Short URI in Japan
        testUri(
            WGS84Point(34.5945681010353, 133.75838190375345, z = 17.0, name = "Steak no Don"),
            "https://maps.app.goo.gl/mBtbC6qXLK2baGTV9",
        )

        // Short URI in China (GCJ-02)
        testUri(
            GCJ02Point(39.920439, 116.331538),
            "https://maps.app.goo.gl/FP3EV7tTUKYbmcVp7",
        )

        // Place
        testUri(
            WGS84Point(52.5200066, 13.404954, name = "Berlin"),
            "https://www.google.com/maps/place/Berlin,+Germany/",
        )

        // Map view
        testUri(
            WGS84Point(52.5067296, 13.2599309, z = 11.0, name = "Berlin, Germany"),
            "https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/",
        )
    }

    @Test
    @NotEmulator
    fun googleSearch() {
        assumeTrue(
            "This test currently fails, because Google returns a captcha, even though we only run the test on a real device",
            false,
        )

        // Google Search
        testUri(
            WGS84Point(27.765028, -15.600889),
            "https://g.co/kgs/91UYXud",
        )
    }

    @Test
    fun googleMapsPlaceList() {
        assumeTrue("This test currently fails, because Google changed their HTML", false)

        // Place list
        testUri(
            persistentListOf(
                GCJ02Point(59.1293656, 11.4585672),
                GCJ02Point(59.4154007, 11.659710599999999),
                GCJ02Point(59.3443991, 11.672637),
                GCJ02Point(59.2557409, 11.5857853),
                GCJ02Point(59.1579458, 11.7337507),
                GCJ02Point(59.229344899999994, 11.6892173),
                GCJ02Point(59.2999243, 11.6587237),
                GCJ02Point(59.147731699999994, 11.550661199999999),
            ),
            "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
        )
    }
}
