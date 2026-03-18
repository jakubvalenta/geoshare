package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class OsmAndInputBehaviorTest : InputBehaviorTest {
    @Test
    fun osmAnd() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(52.51628, 13.37771, z = 15.0),
            "https://osmand.net/map?pin=52.51628,13.37771#15/52.51628/13.37771",
        )
    }
}
