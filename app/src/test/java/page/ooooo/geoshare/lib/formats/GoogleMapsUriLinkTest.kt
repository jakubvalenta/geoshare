package page.ooooo.geoshare.lib.formats

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.formats.GoogleMapsUriFormat.formatNavigationUriString
import page.ooooo.geoshare.lib.formats.GoogleMapsUriFormat.formatStreetViewUriString
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.WGS84Point

class GoogleMapsUriLinkTest {
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatNavigationUriString_whenLastPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasQueryAndZoom_returnsLinkWithQueryAndZoom() {
        assertEquals(
            "google.navigation:q=foo+bar",
            formatNavigationUriString(
                WGS84Point(name = "foo bar", z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasQueryAndZoom_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            formatStreetViewUriString(
                WGS84Point(name = "foo bar", z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285067,121.475524",
            formatNavigationUriString(
                WGS84Point(31.23044166868017, 121.47099209401793),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285067,121.475524",
            formatStreetViewUriString(
                WGS84Point(31.23044166868017, 121.47099209401793),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285069,121.4755246",
            formatNavigationUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285069,121.4755246",
            formatStreetViewUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            "google.navigation:q=0,0",
            formatNavigationUriString(
                WGS84Point(),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            formatStreetViewUriString(
                WGS84Point(),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            formatNavigationUriString(
                WGS84Point(50.123456, -11.123456),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285067,121.475524",
            formatNavigationUriString(
                WGS84Point(31.23044166868017, 121.47099209401793),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285067,121.475524",
            formatStreetViewUriString(
                WGS84Point(31.23044166868017, 121.47099209401793),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285069,121.4755246",
            formatNavigationUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285069,121.4755246",
            formatStreetViewUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106),
                uriQuote,
            ),
        )
    }
}
