package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.testText
import page.ooooo.geoshare.testUri
import page.ooooo.geoshare.waitForAppToBeVisible

class MapsMeInputBehaviorTest {
    @Test
    fun mapsMe() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Custom scheme
        testUri(
            WGS84Point(51.0000004, -108.9999868, z = 4.0, name = "América do Norte", source = Source.HASH),
            "ge0://ApYSV0YTAl/América_do_Norte",
        )

        // Maps.me short link
        testUri(
            WGS84Point(-18.9249432, 46.4416404, z = 4.0, name = "Madagascar", source = Source.HASH),
            "http://ge0.me/AbCMCNp0LO/Madagascar",
        )

        // Organic Maps short link
        testUri(
            WGS84Point(
                40.7127405, -74.005997,
                z = 9.0,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Nova Iorque",
                source = Source.HASH
            ),
            "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )

        // CoMaps short link
        testUri(
            WGS84Point(52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg", source = Source.HASH),
            "https://comaps.at/o4MnIOApKp/Kreuzberg",
        )

        // Text
        testText(
            WGS84Point(51.0000004, -108.9999868, z = 4.0, name = "América do Norte", source = Source.HASH),
            "América do Norte, Lancer, Saskatchewan, Canadá\n" +
                "http://ge0.me/ApYSV0YTAl/América_do_Norte\n" +
                "(51.000001, -108.999988)",
        )

        // Text, which will get parsed by GeoUriInput, because it contains a geo: URI that precedes the short link
        testText(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            WGS84Point(40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque", source = Source.URI),
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
    }
}
