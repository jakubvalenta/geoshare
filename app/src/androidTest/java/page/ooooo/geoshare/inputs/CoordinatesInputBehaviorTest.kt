package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CoordinatesInputBehaviorTest : InputBehaviorTest {
    @Test
    fun coordinates() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Decimal
        testTextUri(
            WGS84Point(-68.648556, -152.775879, source = Source.TEXT),
            "N-68.648556,E-152.775879",
        )
    }
}
