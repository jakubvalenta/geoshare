package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CartesIGNInputBehaviorTest : InputBehaviorTest {
    @Test
    fun cartesIGN() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.123456, -11.123456, z = 3.14, source = Source.URI),
            "https://cartes-ign.ign.fr?lng=-11.123456&lat=50.123456&z=3.14",
        )
    }
}
