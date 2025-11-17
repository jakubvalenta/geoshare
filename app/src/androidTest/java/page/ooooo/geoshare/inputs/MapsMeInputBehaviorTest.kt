package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class MapsMeInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Custom scheme
        testUri(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0),
            "ge0://ApYSV0YTAl/América_do_Norte",
        )

        // Maps.me short URI
        testUri(
            Position(Srs.WGS84, -18.9249432, 46.4416404, z = 4.0),
            "http://ge0.me/AbCMCNp0LO/Madagascar",
        )

        // Organic Maps short URI
        testUri(
            Position(Srs.WGS84, 40.7127405, -74.005997, z = 9.0),
            "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )

        // CoMaps short URI
        testUri(
            Position(Srs.WGS84, 52.4877386, 13.3815233, z = 14.0),
            "https://comaps.at/o4MnIOApKp/Kreuzberg",
        )

        // Text
        testTextUri(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0),
            "América do Norte, Lancer, Saskatchewan, Canadá\n" +
                "http://ge0.me/ApYSV0YTAl/América_do_Norte\n" +
                "(51.000001, -108.999988)",
        )

        // Text, which will get parsed by GeoUriInput, because it contains a geo: URI that precedes the short URI
        testTextUri(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque"),
            @Suppress("SpellCheckingInspection")
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}
