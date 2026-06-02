package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import page.ooooo.geoshare.closeIntro
import page.ooooo.geoshare.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.waitForAppToBeVisible

class GeoUriInputBehaviorTest {
    @Test
    fun geoUri() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Coordinates, query and zoom
        testUri(
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.URI),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )

        // Coordinates in query with space; this is not a valid URI, but we support it anyway
        testText(
            WGS84Point(45.4786785, 9.2473799, source = Source.URI),
            "geo:0,0?q=45.4786785, 9.2473799",
        )
    }
}
