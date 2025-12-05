package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class MagicEarthOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = MagicEarthOutput

    @Test
    fun getPositionActions_positionHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456",
            output.getPositionActions().first()
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, z = 5.0), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_positionHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar",
            output.getPositionActions().first()
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar"), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_positionHasCoordinatesAndQueryAndZoom_returnsSearchAroundUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?search_around&lat=50.123456&lon=-11.123456&q=foo%20bar",
            output.getPositionActions().first()
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 5.0), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_positionHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?open_search&q=foo%20bar",
            output.getPositionActions().first()
                .getText(Position(q = "foo bar", z = 5.0), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_returnsNavigateToUri() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&lat=50.123456&lon=-11.123456",
            ),
            Position(Srs.WGS84, 50.123456, -11.123456).let { position ->
                output.getPositionActions().drop(1).map { it.getText(position, null, uriQuote) }
            },
        )
    }
}
