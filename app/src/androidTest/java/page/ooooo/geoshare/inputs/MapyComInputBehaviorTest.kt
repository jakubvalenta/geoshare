package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.point.Position
import page.ooooo.geoshare.lib.point.Srs

class MapyComInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

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

        // Short URI
        testUri(
            WGS84Point(50.0831498, 14.4549515, z = 17.0),
            "https://mapy.com/s/jakuhelasu",
        )
        testUri(
            WGS84Point(50.0858554, 14.4624724, z = 17.0),
            "https://mapy.cz/s/jetucaputu",
        )

        // Text
        testTextUri(
            WGS84Point(41.9966006, -6.1223825),
            @Suppress("SpellCheckingInspection")
            "Vega de Tera Calle Barrio de Abajo 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha",
        )
    }
}
