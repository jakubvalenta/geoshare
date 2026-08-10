package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testUri

class CartesIGNInputBehaviorTest {
    @Test
    fun cartesIGN_offline() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.123456, -120.123456, z = 3.14, source = Source.URI),
            "https://cartes-ign.ign.fr?lng=-120.123456&lat=50.123456&z=3.14",
        )
    }
}
