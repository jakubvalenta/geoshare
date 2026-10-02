package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.testUri

class MapQuestInputBehaviorTest {
    @Test
    fun mapQuest_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("mapq.st")
            assumeDomainResolvable("www.mapquest.com")
        }

        // Place within Mainland China
        testUri(
            GCJ02Point(39.91843414, 116.40497589, source = Source.HTML),
            "https://www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097",
            grantConnectionPermission = true,
        )

        // Place within Hong Kong
        testUri(
            WGS84Point(22.254182,113.905181, source = Source.HTML),
            "https://www.mapquest.com/cn/tian-tan-buddha-big-buddha-526039166",
            grantConnectionPermission = true,
        )

        // Place within Taiwan
        testUri(
            WGS84Point(24.998562,121.580986, source = Source.HTML),
            "https://www.mapquest.com/tw/taipei-zoo-524803950",
            grantConnectionPermission = true,
        )

        // Short link
        testUri(
            WGS84Point(52.49002,13.38267, source = Source.HTML),
            "https://mapq.st/4s3jg1r",
            grantConnectionPermission = true,
        )
    }
}
