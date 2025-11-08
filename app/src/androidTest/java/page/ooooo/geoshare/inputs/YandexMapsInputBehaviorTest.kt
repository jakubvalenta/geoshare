package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class YandexMapsInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates
        testUri(
            Position(Srs.WGS84, -37.81384550094835, 144.96315783657042, z = 17.852003),
            "https://yandex.com/maps?ll=144.96315783657042%2C-37.81384550094835&z=17.852003",
        )

        // Coordinates and query
        testUri(
            Position(Srs.WGS84, 52.294001, 8.065475, z = 13.24),
            "https://yandex.com/maps/100513/osnabruck/?ll=8.055899%2C52.280743&mode=whatshere&whatshere%5Bpoint%5D=8.065475%2C52.294001&whatshere%5Bzoom%5D=13.24&z=13.24",
        )

        // Short URI
        testUri(
            Position(Srs.WGS84, 50.111192, 8.668963, z = 14.19),
            "https://yandex.com/maps/-/CLAvMI18",
        )
    }
}
