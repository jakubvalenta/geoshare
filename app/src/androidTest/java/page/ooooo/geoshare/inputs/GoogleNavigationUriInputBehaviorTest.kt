package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.testUri

class GoogleNavigationUriInputBehaviorTest {
    @Test
    fun googleNavigationUri() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.123456, -120.123456, source = Source.URI),
            "google.navigation:q=50.123456,-120.123456",
        )
    }
}
