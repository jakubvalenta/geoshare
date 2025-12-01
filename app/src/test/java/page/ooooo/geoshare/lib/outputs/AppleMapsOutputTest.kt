package page.ooooo.geoshare.lib.outputs

import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class AppleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = AppleMapsOutput

    @Test
    fun copyAction_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4), null, uriQuote)
        )
    }

    @Test
    fun copyAction_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4), null, uriQuote)
        )
    }

    @Test
    fun copyAction_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        Assert.assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z=3.4",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(q = "foo bar", z = 3.4), null, uriQuote)
        )
    }
}
