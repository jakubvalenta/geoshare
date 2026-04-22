package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class PlusCodeInputBehaviorTest : InputBehaviorTest {
    @Test
    fun plusCode() = uiAutomator {
        // Global code as URL https://plus.codes
        testUri(
            WGS84Point(14.917313, -23.5113130, source = Source.HASH),
            "https://plus.codes/796RWF8Q+WF",
        )

        // Global code as URL https://www.google.com/maps
        testUri(
            WGS84Point(-1.289938, 36.820313, source = Source.HASH),
            "https://www.google.com/maps/place/6GCRPR6C%2B24",
        )

        // Global code within mainland China as text
        testTextUri(
            GCJ02Point(39.917312, 116.397078, source = Source.HASH),
            @Suppress("SpellCheckingInspection") "8PFRW98W+WRG",
        )
    }
}
