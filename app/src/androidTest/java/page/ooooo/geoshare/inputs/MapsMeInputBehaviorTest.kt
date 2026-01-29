package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.point.Position
import page.ooooo.geoshare.lib.point.Srs

class MapsMeInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Custom scheme
        testUri(
            WGS84Point(51.0000004, -108.9999868, z = 4.0, name = "América do Norte"),
            "ge0://ApYSV0YTAl/América_do_Norte",
        )

        // Maps.me short URI
        testUri(
            WGS84Point(-18.9249432, 46.4416404, z = 4.0, name = "Madagascar"),
            "http://ge0.me/AbCMCNp0LO/Madagascar",
        )

        // Organic Maps short URI
        testUri(
            @Suppress("SpellCheckingInspection")
            WGS84Point(40.7127405, -74.005997, z = 9.0, name = "Nova Iorque"),
            "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )

        // CoMaps short URI
        testUri(
            WGS84Point(52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg"),
            "https://comaps.at/o4MnIOApKp/Kreuzberg",
        )

        // Text
        testTextUri(
            WGS84Point(51.0000004, -108.9999868, z = 4.0, name = "América do Norte"),
            "América do Norte, Lancer, Saskatchewan, Canadá\n" +
                "http://ge0.me/ApYSV0YTAl/América_do_Norte\n" +
                "(51.000001, -108.999988)",
        )

        // Text, which will get parsed by GeoUriInput, because it contains a geo: URI that precedes the short URI
        testTextUri(
            @Suppress("SpellCheckingInspection")
            WGS84Point(40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque"),
            @Suppress("SpellCheckingInspection")
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}
