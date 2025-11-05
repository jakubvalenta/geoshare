package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class GoogleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val packageNames: List<String> = emptyList()
    private val outputs = GoogleMapsOutputManager.getOutputs(packageNames)

    @Test
    fun getOutputs_whenUriHasCoordinatesAndZoom_returnsCoordinatesAsQueryAndZoom() {
        assertEquals(
            listOf(Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4")),
            outputs.getActions().map {
                it.getAction(Position("50.123456", "-11.123456", z = "3.4"), uriQuote)
            },
        )
    }

    @Test
    fun getOutputs_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        assertEquals(
            listOf(Action.Copy("https://www.google.com/maps?q=foo%20bar&z=3.4")),
            outputs.getActions().map {
                it.getAction(Position(q = "foo bar", z = "3.4"), uriQuote)
            },
        )
    }

    @Test
    fun getOutputs_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            listOf(Action.Copy("https://www.google.com/maps?q=50.123456,-11.123456&z=3.4")),
            outputs.getActions().map {
                it.getAction(Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"), uriQuote)
            },
        )
    }
}
