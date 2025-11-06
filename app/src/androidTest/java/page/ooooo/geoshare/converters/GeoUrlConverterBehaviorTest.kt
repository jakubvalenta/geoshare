package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class GeoUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates, query and zoom
        testUri(
            Position(50.123456, -11.123456, q = "foo bar", z = 3.4),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )
    }
}
