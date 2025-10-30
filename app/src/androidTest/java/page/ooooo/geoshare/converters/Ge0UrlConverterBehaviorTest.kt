package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class Ge0UrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Custom scheme
        testUri(
            Position("51.0000004", "-108.9999868", z = "4"),
            "ge0://ApYSV0YTAl/América_do_Norte",
        )

        // Maps.me short URI
        testUri(
            Position("-18.9249432", "46.4416404", z = "4"),
            "http://ge0.me/AbCMCNp0LO/Madagascar",
        )

        // Organic Maps short URI
        testUri(
            Position("40.7127405", "-74.005997", z = "9"),
            "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )

        // CoMaps short URI
        testUri(
            Position("52.4877386", "13.3815233", z = "14"),
            "https://comaps.at/o4MnIOApKp/Kreuzberg",
        )

        // Text
        testTextUri(
            Position("51.0000004", "-108.9999868", z = "4"),
            "América do Norte, Lancer, Saskatchewan, Canadá\n" +
                    "http://ge0.me/ApYSV0YTAl/América_do_Norte\n" +
                    "(51.000001, -108.999988)",
        )

        // Text, which will get parsed by GeoUrlConverter, because it contains a geo: URI that precedes the short URI
        testTextUri(
            Position("40.7127400", "-74.0059965", q = "40.7127400,-74.0059965(Nova Iorque)", z = "9"),
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}
