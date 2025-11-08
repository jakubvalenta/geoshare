package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

class GoogleMapsPointOutputGroupTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = GoogleMapsPointOutputGroup

    @Test
    fun copyOutput_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456"),
                Action.Copy("google.navigation:q=50.123456,-11.123456"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=50.123456,-11.123456&cbp=0,30,0,0,-15"),
                Action.OpenChooser("google.navigation:q=50.123456,-11.123456"),
                @Suppress("SpellCheckingInspection")
                Action.OpenChooser("google.streetview:cbll=50.123456,-11.123456&cbp=0,30,0,0,-15"),
            ),
            Point(Srs.WGS84, 50.123456, -11.123456).let { point ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(point) }
                    .map { it.getAction(point, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2285067,121.475524"),
                Action.Copy("google.navigation:q=31.2285067,121.475524"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=31.2285067,121.475524&cbp=0,30,0,0,-15"),
                Action.OpenChooser("google.navigation:q=31.2285067,121.475524"),
                @Suppress("SpellCheckingInspection")
                Action.OpenChooser("google.streetview:cbll=31.2285067,121.475524&cbp=0,30,0,0,-15"),
            ),
            Point(Srs.WGS84, 31.23044166868017, 121.47099209401793).let { point ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(point) }
                    .map { it.getAction(point, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                Action.Copy("https://www.google.com/maps?q=31.2285069,121.4755246"),
                Action.Copy("google.navigation:q=31.2285069,121.4755246"),
                @Suppress("SpellCheckingInspection")
                Action.Copy("google.streetview:cbll=31.2285069,121.4755246&cbp=0,30,0,0,-15"),
                Action.OpenChooser("google.navigation:q=31.2285069,121.4755246"),
                @Suppress("SpellCheckingInspection")
                Action.OpenChooser("google.streetview:cbll=31.2285069,121.4755246&cbp=0,30,0,0,-15"),
            ),
            Point(Srs.GCJ02, 31.22850685422705, 121.47552456472106).let { point ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(point) }
                    .map { it.getAction(point, uriQuote) }
            },
        )
    }
}
