package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class GoogleMapsOutputGroupTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = GoogleMapsOutputGroup

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4"),
                Action.Copy("google.navigation:q=50.123456,-11.123456"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=50.123456,-11.123456&cbp=0,30,0,0,-15"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasQueryAndZoom_returnsLinkWithQueryAndZoomAndNoStreetView() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=foo%20bar&z=3.4"),
                Action.Copy("google.navigation:q=foo+bar"),
            ),
            Position(q = "foo bar", z = 3.4).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4"),
                Action.Copy("google.navigation:q=50.123456,-11.123456"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=50.123456,-11.123456&cbp=0,30,0,0,-15"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2285067,121.475524"),
                Action.Copy("google.navigation:q=31.2285067,121.475524"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=31.2285067,121.475524&cbp=0,30,0,0,-15"),
            ),
            Position(Srs.WGS84, 31.23044166868017, 121.47099209401793).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2285069,121.4755246"),
                Action.Copy("google.navigation:q=31.2285069,121.4755246"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=31.2285069,121.4755246&cbp=0,30,0,0,-15"),
            ),
            Position(Srs.GCJ02, 31.22850685422705, 121.47552456472106).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasNeitherPointNorQuery_returnsEmptyLinks() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps"),
                Action.Copy("google.navigation:q=0,0"),
            ),
            Position().let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }
}
