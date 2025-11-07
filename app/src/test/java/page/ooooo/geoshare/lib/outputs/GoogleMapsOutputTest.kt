package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Srs
import page.ooooo.geoshare.lib.UriQuote

class GoogleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = GoogleMapsOutputGroup

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4"),
            ),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4), uriQuote)
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasQueryAndZoom_returnsLinkWithQueryAndZoom() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=foo%20bar&z=3.4"),
            ),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(q = "foo bar", z = 3.4), uriQuote)
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4"),
            ),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4), uriQuote)
            },
        )
    }

    @Test
    fun copyOutput_whenPositionIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2285067,121.475524"),
            ),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.WGS84, 31.23044166868017, 121.47099209401793), uriQuote)
            },
        )
    }

    @Test
    fun copyOutput_whenPositionIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2304417,121.4709921"),
            ),
            outputGroup.getActionOutputs().map {
                it.getAction(Position(Srs.GCJ02, 31.23044166868017, 121.47099209401793), uriQuote)
            },
        )
    }
}
