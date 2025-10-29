package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class OrganicMapsUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // OrganicMaps short URI
        testUri(
            Position("40.7127403", "-74.005997", z = "9"),
            "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )

        // CoMaps short URI
        testUri(
            Position("52.4877385", "13.3815233", z = "14"),
            "https://comaps.at/o4MnIOApKp/Kreuzberg",
        )

        // Text, which will get parsed by GeoUrlConverter, because it contains a geo: URI that precedes the short URI
        testTextUri(
            Position("40.7127400", "-74.0059965", q = "40.7127400,-74.0059965(Nova Iorque)", z = "9"),
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}
