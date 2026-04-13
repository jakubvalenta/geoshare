package page.ooooo.geoshare.lib.formatters

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MagicEarthUriFormatterTest {
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val magicEarthUriFormatter = MagicEarthUriFormatter(coordinateConverter)
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatDisplayUriString_pointsIsEmpty_returnsShowOnMapWithZeroLatAndLon() {
        assertEquals(
            "magicearth://?show_on_map&lat=0&lon=0",
            magicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456",
            magicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(50.123456, -11.123456, z = 5.0, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar",
            magicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?open_search&q=foo%20bar",
            magicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(name = "foo bar", z = 5.0, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsIsEmpty_returnsNavigateToUriWithZeroLatAndLon() {
        assertEquals(
            "magicearth://?get_directions&lat=0&lon=0",
            magicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinates_returnsNavigateToUriWithLatAndLonParameters() {
        assertEquals(
            "magicearth://?get_directions&lat=50.123456&lon=-11.123456",
            magicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_hasNameOnly_returnsNavigateToUrWithQParameter() {
        assertEquals(
            "magicearth://?get_directions&q=foo%20bar",
            magicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(name = "foo bar", source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
