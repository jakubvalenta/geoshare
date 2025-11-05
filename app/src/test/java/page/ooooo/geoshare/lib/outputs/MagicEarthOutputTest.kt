package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class MagicEarthOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = MagicEarthOutputGroup

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Action.Copy("magicearth://?show_on_map&lat=50.123456&lon=-11.123456"),
            outputGroup.getActionOutputs().first()
                .getAction(Position("50.123456", "-11.123456", z = "5"), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsSearchAroundUriAndIgnoresZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Action.Copy("magicearth://?search_around&lat=50.123456&lon=-11.123456&q=foo%20bar"),
            outputGroup.getActionOutputs().first()
                .getAction(Position("50.123456", "-11.123456", q = "foo bar", z = "5"), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Action.Copy("magicearth://?open_search&q=foo%20bar"),
            outputGroup.getActionOutputs().first()
                .getAction(Position(q = "foo bar", z = "5"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsDriveToAndDriveViaUriStrings() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            listOf(
                Action.Copy("magicearth://?navigate_to&lat=50.123456&lon=-11.123456"),
                Action.Copy("magicearth://?navigate_via&lat=50.123456&lon=-11.123456"),
            ),
            outputGroup.getActionOutputs().slice(1..2).map {
                it.getAction(Position("50.123456", "-11.123456"), uriQuote)
            },
        )
    }
}
