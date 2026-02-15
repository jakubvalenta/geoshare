package page.ooooo.geoshare.inputs

import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.GCJ02Point

class AmapInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun amapHtml() = runBlocking {
        assumeDomainResolvable(@Suppress("SpellCheckingInspection") "surl.amap.com")

        // Short URI
        testUri(
            GCJ02Point(31.222811749011463, 121.46840706467624),
            "https://surl.amap.com/4mkKGuyJ2bz",
        )
    }
}
