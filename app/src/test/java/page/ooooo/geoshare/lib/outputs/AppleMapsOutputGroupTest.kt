package page.ooooo.geoshare.lib.outputs

import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class AppleMapsOutputGroupTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = AppleMapsOutputGroup

    @Test
    fun copyOutput_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            listOf(Action.Copy("https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4")),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4), uriQuote)
            }
        )
    }

    @Test
    fun copyOutput_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            listOf(Action.Copy("https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4")),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4), uriQuote)
            }
        )
    }

    @Test
    fun copyOutput_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        Assert.assertEquals(
            listOf(Action.Copy("https://maps.apple.com/?q=foo%20bar&z=3.4")),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(q = "foo bar", z = 3.4), uriQuote)
            }
        )
    }
}
