package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeNotEmulator
import page.ooooo.geoshare.tests.testUri

class GoogleMapsHtmlInputBehaviorTest : InputBehaviorTest {
    @Test
    fun googleMapsHtmlInput_online_googleSearch() = uiAutomator {
        assumeNotEmulator()
        assumeTrue(
            "This test currently fails, because Google returns a captcha, even though we only run the test on a physical device",
            false,
        )

        // Google Search
        testUri(
            WGS84Point(27.765028, -15.600889, source = Source.HTML_NOT_SEMANTIC),
            "https://g.co/kgs/91UYXud",
            grantConnectionPermission = true,
        )
    }
}
