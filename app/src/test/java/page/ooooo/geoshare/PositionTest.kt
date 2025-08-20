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

    @Test
    fun position_toNorthSouthWestEastDecCoordsString_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "17.2165721° S, 149.9470294° W",
            Position("-17.2165721", "-149.9470294").toNorthSouthWestEastDecCoordsString(),
        )
    }

    @Test
    fun position_toNorthSouthWestEastDecCoordsString_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52.5067296° N, 13.2599309° E",
            Position("52.5067296", "13.2599309").toNorthSouthWestEastDecCoordsString(),
        )
    }

    @Test
    fun position_toMagicEarthUriString_returnsAllCoordsAndParams() {
        assertEquals(
            "magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4",
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4").toMagicEarthUriString(uriQuote),
        )
    }

}
