package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testUri

class GoogleNavigationUriInputBehaviorTest {
    @Test
    fun googleNavigationUri_offline() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.123456, -120.123456, source = Source.URI),
            "google.navigation:q=50.123456,-120.123456",
        )
    }
}
