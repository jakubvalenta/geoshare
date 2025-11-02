package page.ooooo.geoshare.lib.outputs

import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class AppleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()

    @Test
    fun getPositionUriString_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
            AppleMapsOutput.getPositionUriString(Position("50.123456", "-11.123456", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun getPositionUriString_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
            AppleMapsOutput.getPositionUriString(
                Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"),
                uriQuote
            ),
        )
    }

    @Test
    fun getPositionUriString_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z=3.4",
            AppleMapsOutput.getPositionUriString(Position(q = "foo bar", z = "3.4"), uriQuote),
        )
    }
}
