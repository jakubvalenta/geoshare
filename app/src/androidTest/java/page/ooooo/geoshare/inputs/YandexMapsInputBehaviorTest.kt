package page.ooooo.geoshare.inputs

import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class YandexMapsInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun yandexMaps() {
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
    fun yandexMapsHtml() = runBlocking {
        assumeDomainResolvable("yandex.com")

        // Short URI
        testUri(
            WGS84Point(50.111192, 8.668963, z = 14.19),
            "https://yandex.com/maps/-/CLAvMI18",
        )
    }
}
