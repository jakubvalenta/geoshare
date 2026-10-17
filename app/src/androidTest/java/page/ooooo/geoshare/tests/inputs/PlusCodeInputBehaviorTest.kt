package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.tests.testText
import page.ooooo.geoshare.tests.testUri

class PlusCodeInputBehaviorTest : InputBehaviorTest {
    @Test
    fun plusCode_offline() = uiAutomator {
        // Global Plus Code URI
        testUri(
            WGS84Point(14.917313, -23.5113130, source = Source.HASH),
            "https://plus.codes/796RWF8Q+WF",
        )

        // Global Plus Code text within mainland China
        testText(
            WGS84Point(39.917312, 116.397078, source = Source.HASH),
            @Suppress("GrazieInspectionRunner") "8PFRW98W+WRG",
        )
    }
}
