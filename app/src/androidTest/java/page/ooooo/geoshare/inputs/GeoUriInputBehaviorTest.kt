package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class GeoUriInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates, query and zoom
        testUri(
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )

        // Coordinates in query with space; this is not a valid URI, but we support it anyway
        testTextUri(
            WGS84Point(45.4786785, 9.2473799),
            "geo:0,0?q=45.4786785, 9.2473799",
        )
    }
}
