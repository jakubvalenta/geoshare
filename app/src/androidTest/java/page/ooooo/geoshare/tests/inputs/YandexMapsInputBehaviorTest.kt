package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.testUri

class YandexMapsInputBehaviorTest : InputBehaviorTest {
    @Test
    fun yandexMaps_offline() = uiAutomator {
        // Point
        testUri(
            WGS84Point(52.294001, 8.065475, z = 13.24, source = Source.URI),
            "https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=13.24",
        )

        // Map center
        testUri(
            WGS84Point(-37.81384550094835, 144.96315783657042, z = 17.852003, source = Source.MAP_CENTER),
            "https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003",
        )
    }

    @Test
    fun yandexMaps_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("yandex.com")
        }

        // Short link
        testUri(
            WGS84Point(50.106526, 8.662158, source = Source.HTML),
            "https://yandex.com/maps/-/CLAvMI18",
            grantConnectionPermission = true,
        )

        // POI Geo
        testUri(
            WGS84Point(
                55.882227, 37.566898,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Keramichesky Drive",
                source = Source.HTML,
            ),
            "https://yandex.ru/maps/213/moscow/geo/keramicheskiy_proyezd/8062907/",
            grantConnectionPermission = true,
        )

        // POI Org
        testUri(
            WGS84Point(
                39.915043, 116.408914,
                z = 13.0,
                name = "Запретный город",
                source = Source.HTML,
            ),
            "https://yandex.com/maps/org/zapretny_gorod/5867973238",
            grantConnectionPermission = true,
        )

        // POI Org with map center
        testUri(
            WGS84Point(
                name = "Запретный город",
                source = Source.HTML,
            ),
            "https://yandex.com/maps/org/zapretny_gorod/5867973238/?ll=116.096354%2C40.045755&z=13",
            grantConnectionPermission = true,
        )
    }
}
