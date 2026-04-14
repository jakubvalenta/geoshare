package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point

class WazeInputBehaviorTest : InputBehaviorTest {
    @Test
    fun waze() = uiAutomator {
        // Launch app and close intro
        launchApplication()
        closeIntro()

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

        // Short URI
        testUri(
            WGS84Point(19.402564, -99.165666, z = 16.0, source = Source.HASH),
            "https://waze.com/ul/h9g3qrkju0",
        )

        // Text
        testTextUri(
            WGS84Point(45.829189, 1.259372, z = 16.0, source = Source.HASH),
            @Suppress("SpellCheckingInspection")
            "Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3",
        )
    }

    @Test
    fun wazeHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("waze.com")
        }

        // Place id
        testUri(
            WGS84Point(52.000425474, 4.372739102, source = Source.JAVASCRIPT),
            "https://ul.waze.com/ul?venue_id=2884104.28644432.6709020",
        )
    }
}
