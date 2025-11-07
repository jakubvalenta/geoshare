package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Srs

class OsmAndUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position(Srs.WGS84, 52.51628, 13.37771, z = 15.0),
            "https://osmand.net/map?pin=52.51628,13.37771#15/52.51628/13.37771",
        )
    }
}
