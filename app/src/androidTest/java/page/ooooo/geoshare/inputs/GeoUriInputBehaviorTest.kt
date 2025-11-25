package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GeoUriInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates, query and zoom
        testUri(
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )
    }
}
