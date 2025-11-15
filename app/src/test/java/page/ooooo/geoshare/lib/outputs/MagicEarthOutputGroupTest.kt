package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class MagicEarthOutputGroupTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = MagicEarthOutputGroup

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            Action.Copy("magicearth://?show_on_map&lat=50.123456&lon=-11.123456"),
            outputGroup.getActionOutputs().first()
                .getAction(Position(Srs.WGS84, 50.123456, -11.123456, z = 5.0), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            Action.Copy("magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar"),
            outputGroup.getActionOutputs().first()
                .getAction(Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar"), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsSearchAroundUriAndIgnoresZoom() {
        assertEquals(
            Action.Copy("magicearth://?search_around&lat=50.123456&lon=-11.123456&q=foo%20bar"),
            outputGroup.getActionOutputs().first()
                .getAction(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 5.0), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            Action.Copy("magicearth://?open_search&q=foo%20bar"),
            outputGroup.getActionOutputs().first()
                .getAction(Position(q = "foo bar", z = 5.0), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsDriveToAndDriveViaUriStrings() {
        assertEquals(
            listOf(
                Action.Copy("magicearth://?navigate_to&lat=50.123456&lon=-11.123456"),
                Action.Copy("magicearth://?navigate_via&lat=50.123456&lon=-11.123456"),
            ),
            outputGroup.getActionOutputs().slice(1..2).map {
                it.getAction(Position(Srs.WGS84, 50.123456, -11.123456), uriQuote)
            },
        )
    }
}
