package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.WGS84Point

class MagicEarthOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = MagicEarthOutput

    @Test
    fun getPositionActions_pointsHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456",
            output.getPositionActions().first()
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456, z = 5.0)), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_pointsHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar",
            output.getPositionActions().first()
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_pointsHasCoordinatesAndQueryAndZoom_returnsSearchAroundUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?search_around&lat=50.123456&lon=-11.123456&q=foo%20bar",
            output.getPositionActions().first()
                .getText(
                    persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar", z = 5.0)),
                    null,
                    uriQuote
                ),
        )
    }

    @Test
    fun getPositionActions_pointsHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?open_search&q=foo%20bar",
            output.getPositionActions().first()
                .getText(persistentListOf(WGS84Point(name = "foo bar", z = 5.0)), null, uriQuote),
        )
    }

    @Test
    fun getPositionActions_pointsHasCoordinates_returnsNavigateToUriWithLatAndLonParameters() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&lat=50.123456&lon=-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456)).let { points ->
                output.getPositionActions().drop(1).map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun getPositionActions_pointsHasCoordinates_returnsNavigateToUrWithQParameter() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&q=foo%20bar",
            ),
            persistentListOf(WGS84Point(name = "foo bar")).let { points ->
                output.getPositionActions().drop(1).map { it.getText(points, null, uriQuote) }
            },
        )
    }
}
