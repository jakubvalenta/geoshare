package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.waitForAppToBeVisible

class CoordinateInputBehaviorTest {
    @Test
    fun coordinates() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Decimal
        testText(
            WGS84Point(-68.648556, -152.775879, source = Source.TEXT),
            "N-68.648556,E-152.775879",
        )
    }
}
