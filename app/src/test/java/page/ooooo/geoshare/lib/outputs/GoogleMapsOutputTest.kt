package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class GoogleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()

    @Test
    fun getMainUriString_whenUriHasCoordinatesAndZoom_returnsCoordinatesAsQueryAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
            GoogleMapsOutput.getMainUriString(Position("50.123456", "-11.123456", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun getMainUriString_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=foo%20bar&z=3.4",
            GoogleMapsOutput.getMainUriString(Position(q = "foo bar", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun getMainUriString_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
            GoogleMapsOutput.getMainUriString(
                Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"), uriQuote
            ),
        )
    }
}
