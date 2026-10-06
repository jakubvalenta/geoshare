package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testText
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.testUriFails
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class WazeInputBehaviorTest : InputBehaviorTest {
    @Test
    fun waze_offline() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Coordinates
        testUri(
            WGS84Point(45.6906304, -120.810983, z = 10.0, source = Source.URI),
            "https://waze.com/ul?ll=45.6906304,-120.810983&z=10",
        )
        testUri(
            WGS84Point(45.6906304, -120.810983, source = Source.URI),
            "https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983",
        )

        // Query
        testUri(
            WGS84Point(name = "66 Acacia Avenue", source = Source.URI),
            "https://waze.com/ul?q=66%20Acacia%20Avenue",
        )

        // Short link
        testUri(
            WGS84Point(19.402564, -99.165666, z = 16.0, source = Source.HASH),
            "https://waze.com/ul/h9g3qrkju0",
        )

        // Text
        testText(
            WGS84Point(45.829189, 1.259372, z = 16.0, source = Source.HASH),
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            "Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3",
        )
    }

    @Test
    fun waze_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("waze.com")
        }

        // Route
        testUriFails(
            setOf(
                "Route link is expired.",
                // TODO Add French
            ),
            "https://www.waze.com/ul?a=share_drive&locale=fr&sd=73U16hXD2OJwGD1-Rw-sd&env=row&utm_source=waze_app&utm_campaign=share_drive",
            grantConnectionPermission = true,
        )

        // Place id
        testUri(
            WGS84Point(52.000425474, 4.372739102, source = Source.JAVASCRIPT),
            "https://ul.waze.com/ul?venue_id=2884104.28644432.6709020",
            grantConnectionPermission = true,
        )
    }
}
