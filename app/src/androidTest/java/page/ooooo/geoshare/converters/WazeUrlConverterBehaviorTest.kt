package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class WazeUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates
        testUri(
            Position(45.6906304, -120.810983, z = 10.0),
            "https://waze.com/ul?ll=45.6906304,-120.810983&z=10",
        )

        // Query
        testUri(
            Position(q = "66 Acacia Avenue"),
            "https://waze.com/ul?q=66%20Acacia%20Avenue",
        )

        // Place id
        testUri(
            Position(43.64265563, -79.387202798),
            "https://ul.waze.com/ul?venue_id=183894452.1839010060.260192",
        )

        // Map view
        testUri(
            Position(45.6906304, -120.810983),
            "https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983",
        )

        // Short URI
        testUri(
            Position(19.402564, -99.165666, z = 16.0),
            "https://waze.com/ul/h9g3qrkju0",
        )

        // Text
        testTextUri(
            Position(45.829189, 1.259372, z = 16.0),
            @Suppress("SpellCheckingInspection")
            "Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3",
        )
    }
}
