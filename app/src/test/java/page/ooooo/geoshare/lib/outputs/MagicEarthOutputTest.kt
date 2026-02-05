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
    fun getPointsActions_pointsIsEmpty_returnsShowOnMapWithZeroLatAndLon() {
        assertEquals(
            "magicearth://?show_on_map&lat=0&lon=0",
            output.getPointsActions().first()
                .getText(persistentListOf(WGS84Point()), null, uriQuote),
        )
    }

    @Test
    fun getPointsActions_pointsHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456",
            output.getPointsActions().first()
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456, z = 5.0)), null, uriQuote),
        )
    }

    @Test
    fun getPointsActions_pointsHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar",
            output.getPointsActions().first()
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")), null, uriQuote),
        )
    }

    @Test
    fun getPointsActions_pointsHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?open_search&q=foo%20bar",
            output.getPointsActions().first()
                .getText(persistentListOf(WGS84Point(name = "foo bar", z = 5.0)), null, uriQuote),
        )
    }

    @Test
    fun getPointsActions_pointsIsEmpty_returnsNavigateToUriWithZeroLatAndLon() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&lat=0&lon=0",
            ),
            persistentListOf(WGS84Point()).let { points ->
                output.getPointsActions().drop(1).map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun getPointsActions_pointsHasCoordinates_returnsNavigateToUriWithLatAndLonParameters() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&lat=50.123456&lon=-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456)).let { points ->
                output.getPointsActions().drop(1).map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun getPointsActions_pointsHasCoordinates_returnsNavigateToUrWithQParameter() {
        assertEquals(
            listOf(
                "magicearth://?get_directions&q=foo%20bar",
            ),
            persistentListOf(WGS84Point(name = "foo bar")).let { points ->
                output.getPointsActions().drop(1).map { it.getText(points, null, uriQuote) }
            },
        )
    }
}
