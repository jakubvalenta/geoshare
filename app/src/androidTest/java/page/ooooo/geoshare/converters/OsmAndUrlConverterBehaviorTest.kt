package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class OsmAndUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position(52.51628, 13.37771, z = "15"),
            "https://osmand.net/map?pin=52.51628,13.37771#15/52.51628/13.37771",
        )
    }
}
