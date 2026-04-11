package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MagicEarthInputBehaviorTest : InputBehaviorTest {
    @Test
    fun magicEarth() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(48.85649, 2.35216, source = Source.URI),
            "https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
        )

        // Query
        testUri(
            WGS84Point(name = "Paris", z = 5.0, source = Source.URI),
            "https://magicearth.com/?q=Paris&mapmode=standard&z=5",
        )
        testUri(
            WGS84Point(name = "Central Park", source = Source.URI),
            "https://magicearth.com/?name=Central+Park",
        )
    }
}
