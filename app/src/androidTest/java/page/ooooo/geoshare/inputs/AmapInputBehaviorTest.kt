package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.Source

class AmapInputBehaviorTest : InputBehaviorTest {
    @Test
    fun amapHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable(@Suppress("SpellCheckingInspection") "surl.amap.com")
        }

        // Short URI
        testUri(
            GCJ02Point(
                31.222811749011463, 121.46840706467624,
                name = "上海市黄浦区巨鹿路15-17号",
                source = Source.URI,
            ),
            "https://surl.amap.com/4mkKGuyJ2bz",
        )
    }
}
