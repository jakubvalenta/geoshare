package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.testText
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class GeoUriInputBehaviorTest : InputBehaviorTest {
    @Test
    fun geoUri_offline() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Coordinates, query and zoom
        testUri(
            WGS84Point(50.123456, -120.123456, name = "foo bar", z = 3.4, source = Source.URI),
            "geo:50.123456,-120.123456?q=foo%20bar&z=3.4",
        )

        // Coordinates in query with space; this is not a valid URI, but we support it anyway
        testText(
            WGS84Point(45.4786785, 9.2473799, source = Source.URI),
            "geo:0,0?q=45.4786785, 9.2473799",
        )

        // Text with geo: URIs as well as MapsMe URIs; the MapsMe URIs will be ignored
        testText(
            WGS84Point(
                40.7127400, -74.0059965,
                z = 9.0,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Nova Iorque",
                source = Source.URI
            ),
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            "Organic Maps: geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                "https://omaps.app/Umse5f0H8a/Nova_Iorque",
        )
        testText(
            WGS84Point(52.0553846, -2.7151898, source = Source.URI),
            "Follow this link https://comaps.at/ItdwBgeWW1/Hereford or paste these coordinates " +
                "geo:52.0553846,-2.7151898 into your maps app to see this location.",
        )
    }
}
