package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class UrbiInputBehaviorTest : InputBehaviorTest {
    @Test
    fun urbi() = uiAutomator {
        // Point with marker
        testUri(
            WGS84Point(25.25915, 55.225263, z = 12.77),
            "https://maps.urbi.ae/dubai/geo/55.171971%2C25.289452?m=55.225263%2C25.25915%2F12.77"
        )
    }

    @Test
    fun urbiHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("go.2gis.com")
        }

        // Short URI
        testUri(
            WGS84Point(
                41.285765, 69.234083,
                z = 17.0,
                name = "Music Store, магазин музыкальных инструментов",
            ),
            "https://go.2gis.com/WSTdK",
        )
    }
}
