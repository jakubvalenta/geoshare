package page.ooooo.geoshare.converters

import org.junit.Test
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

class GeoUriConverterBehaviorTest : BaseActivityBehaviorTest() {
    @Test
    fun test() {
        // Coordinates
        testUri(
            Position("50.123456", "-11.123456", z = "3.4"),
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
        )
    }
}
