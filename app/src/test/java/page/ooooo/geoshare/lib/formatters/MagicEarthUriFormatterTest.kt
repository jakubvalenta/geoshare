package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MagicEarthUriFormatterTest {
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatDisplayUriString_pointsIsEmpty_returnsShowOnMapWithZeroLatAndLon() {
        assertEquals(
            "magicearth://?show_on_map&lat=0&lon=0",
            MagicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinatesAndZoom_returnsShowOnMapUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456",
            MagicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(50.123456, -11.123456, z = 5.0, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinatesAndName_returnsShowOnMapUriWithNameParam() {
        assertEquals(
            "magicearth://?show_on_map&lat=50.123456&lon=-11.123456&name=foo%20bar",
            MagicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasQueryAndZoom_returnsOpenSearchUriAndIgnoresZoom() {
        assertEquals(
            "magicearth://?open_search&q=foo%20bar",
            MagicEarthUriFormatter.formatDisplayUriString(
                WGS84Point(name = "foo bar", z = 5.0, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsIsEmpty_returnsNavigateToUriWithZeroLatAndLon() {
        assertEquals(
            "magicearth://?get_directions&lat=0&lon=0",
            MagicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_pointsHasCoordinates_returnsNavigateToUriWithLatAndLonParameters() {
        assertEquals(
            "magicearth://?get_directions&lat=50.123456&lon=-11.123456",
            MagicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatDisplayUriString_hasNameOnly_returnsNavigateToUrWithQParameter() {
        assertEquals(
            "magicearth://?get_directions&q=foo%20bar",
            MagicEarthUriFormatter.formatNavigationUriString(
                WGS84Point(name = "foo bar", source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
