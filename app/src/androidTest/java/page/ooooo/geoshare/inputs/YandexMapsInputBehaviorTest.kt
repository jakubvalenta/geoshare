package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class YandexMapsInputBehaviorTest : InputBehaviorTest {
    @Test
    fun yandexMaps() = uiAutomator {
        // Coordinates
        testUri(
            WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003),
            "https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003",
        )

        // Coordinates and query
        testUri(
            WGS84Point(52.294001, 8.065475, z = 13.24),
            "https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=13.24",
        )
    }

    @Test
    fun yandexMapsHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("yandex.com")
        }

        // Short URI
        testUri(
            WGS84Point(50.111192, 8.668963, z = 14.19),
            "https://yandex.com/maps/-/CLAvMI18",
        )

        // POI
        testUri(
            WGS84Point(55.882227, 37.566898),
            "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/",
        )
    }
}
