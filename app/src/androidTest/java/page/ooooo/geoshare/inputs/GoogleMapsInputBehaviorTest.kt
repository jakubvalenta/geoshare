package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.NotEmulator
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GoogleMapsInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates in data
        testUri(
            Position(Srs.WGS84, 52.4083009, 16.929066199999998, name = "Poznań Old Town, 61-001 Poznań, Poland"),
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=!4m6!3m5!1s0x47045b49399cf863:0xf61cbcaacd7d3070!7e2!8m2!3d52.4083009!4d16.929066199999998"
        )

        // Coordinates and query (address)
        testUri(
            Position(Srs.WGS84, 40.785091, -73.968285, z = 15.0, name = "Central Park"),
            "https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2",
        )

        // Coordinates and query (business)
        testUri(
            Position(
                Srs.WGS84,
                50.4484901,
                8.0469828,
                name = "Änderungsschneiderei Hadamar, Schulstraße 3, 65589 Hadamar",
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/place/%C3%84nderungsschneiderei+Hadamar,+Schulstra%C3%9Fe+3,+65589+Hadamar/@50.4484901,8.0469828,3a,54.9y,5.97h,62.4t/data=!3m5!1e1!3m3!1szFIo-lmR3NWYzi_eWhPHFQ!2e0!6shttps:%2F%2Fstreetviewpixels-pa.googleapis.com%2Fv1%2Fthumbnail%3Fpanoid%3DzFIo-lmR3NWYzi_eWhPHFQ%26w%3D900%26h%3D600%26ll%3D0.0,0.0%26yaw%3D5.0%26pitch%3D28.0%26cb_client%3Dgmm.iv.android!4m2!3m1!1s0x47bc3266a8f3bb4b:0x96d1177f5ecfc466?utm_source=mstt_0&g_ep=CAESBzI1LjM3LjAYACCBgQEqogEsOTQyNjc3MjcsOTQyNzU0MDcsOTQyODQ0NzgsOTQyMjMyOTksOTQyMTY0MTMsOTQyODA1NzYsOTQyMTI0OTYsOTQyMDczOTQsOTQyMDc1MDYsOTQyMDg1MDYsOTQyMTc1MjMsOTQyMTg2NTMsOTQyMjk4MzksOTQyNzUxNjgsOTQyNjI3MzMsNDcwODQzOTMsOTQyMTMyMDAsOTQyNTgzMjVCAkRF&skid=368dc137-203a-4698-9ed3-b974e7bee770&g_st=aw",
        )

        // Query
        testUri(
            Position(Srs.WGS84, 52.5068441, 13.42473175),
            "https://www.google.com/maps/place/Berlin,+Germany/",
        )

        // Map view
        testUri(
            Position(Srs.WGS84, 52.5067296, 13.2599309, z = 11.0),
            "https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/",
        )

        // Short URI
        testUri(
            Position(Srs.WGS84, 52.4842015, 13.4167277),
            "https://maps.app.goo.gl/2ZjYqkBPrcgeVoJS6",
        )

        // Place list
        testUri(
            Position(Srs.WGS84, 59.147731699999994, 11.550661199999999),
            "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg",
        )

        // Directions
        testUri(
            @Suppress("SpellCheckingInspection")
            Position(
                Srs.WGS84,
                52.4807739,
                13.4300356,
                z = 16.0,
                name = "Reuterstraße 1, Berlin-Neukölln, Germany",
            ),
            @Suppress("SpellCheckingInspection")
            "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2",
        )

        // Text
        testUri(
            Position(Srs.WGS84, 52.5067296, 13.2599309),
            "https://www.google.com/maps?center=52.5067296,13.2599309",
        )
    }

    @Test
    // Require that the device is not an emulator, because Google recognizes an emulator and returns a captcha
    @NotEmulator
    fun testSearch() {
        // Launch app and set connection permission to Always
        launchApplication()
        closeIntro()
        setUserPreferenceConnectionPermissionToAlways()

        // Google Search
        testUri(
            Position(Srs.WGS84, 27.765028, -15.600889),
            "https://g.co/kgs/91UYXud",
        )
    }
}
