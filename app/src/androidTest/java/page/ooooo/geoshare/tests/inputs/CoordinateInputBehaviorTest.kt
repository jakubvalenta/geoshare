package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testText
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class CoordinateInputBehaviorTest : InputBehaviorTest {
    @Test
    fun coordinates_offline() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Decimal
        testText(
            WGS84Point(-68.648556, -152.775879, source = Source.TEXT),
            "N-68.648556,E-152.775879",
        )
    }
}
