package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class WazeInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates
        testUri(
            WGS84Point(45.6906304, -120.810983, z = 10.0),
            "https://waze.com/ul?ll=45.6906304,-120.810983&z=10",
        )

        // Query
        testUri(
            WGS84Point(name = "66 Acacia Avenue"),
            "https://waze.com/ul?q=66%20Acacia%20Avenue",
        )

        // Place id
        testUri(
            WGS84Point(52.000425474, 4.372739102),
            "https://ul.waze.com/ul?venue_id=2884104.28644432.6709020",
        )

        // Map view
        testUri(
            WGS84Point(45.6906304, -120.810983),
            "https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983",
        )

        // Short URI
        testUri(
            WGS84Point(19.402564, -99.165666, z = 16.0),
            "https://waze.com/ul/h9g3qrkju0",
        )

        // Text
        testTextUri(
            WGS84Point(45.829189, 1.259372, z = 16.0),
            @Suppress("SpellCheckingInspection")
            "Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3",
        )
    }
}
