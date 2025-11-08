package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class OsmAndInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position(Srs.WGS84, 52.51628, 13.37771, z = 15.0),
            "https://osmand.net/map?pin=52.51628,13.37771#15/52.51628/13.37771",
        )
    }
}
