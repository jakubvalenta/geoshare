package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class MapyComInputBehaviorTest : InputBehaviorTest {
    @Test
    fun mapyCom() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(50.0525078, 14.0184810, z = 9.0),
            "https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
        )
        testUri(
            WGS84Point(50.0525078, 14.0184810, z = 9.0),
            "https://mapy.cz?x=14.0184810&y=50.0525078&z=9",
        )

        // Place
        testUri(
            WGS84Point(50.0992553, 14.4336590, z = 19.0),
            "https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19",
        )
    }

    @Test
    fun mapyComHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("mapy.com")
        }

        // Short URI
        testUri(
            WGS84Point(50.0831498, 14.4549515, z = 17.0),
            "https://mapy.com/s/jakuhelasu",
        )
        testUri(
            WGS84Point(50.0858554, 14.4624724, z = 17.0),
            "https://mapy.cz/s/jetucaputu",
        )
    }
}
