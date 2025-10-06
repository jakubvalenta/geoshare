package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.lib.Position

class MagicEarthUrlConverterBehaviorTest : BaseUrlConverterBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position("48.85649", "2.35216"),
            "https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
        )

        // Query
        testUri(
            Position(q = "Paris", z = "5"),
            "https://magicearth.com/?q=Paris&mapmode=standard&z=5",
        )
        testUri(
            Position(q = "Central Park"),
            "https://magicearth.com/?name=Central+Park",
        )
    }
}
