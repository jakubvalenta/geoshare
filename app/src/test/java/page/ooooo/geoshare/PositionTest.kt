package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position

class PositionTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun position_fromGeoUri_returnsAllCoordsAndParams() {
        assertEquals(
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"),
            Position.fromGeoUriString("geo:50.123456,-11.123456?q=foo%20bar&z=3.4", uriQuote),
        )
    }
}
