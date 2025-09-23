package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

class OpenStreetMapUrlConverterBehaviorTest : BaseActivityBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position("51.49", "-0.13", z = "16"),
            "https://www.openstreetmap.org/#map=16/51.49/-0.13",
        )
    }
}
