package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GoogleMapsUriFormatterTest {
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatNavigationUriString_whenPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasQueryAndZoom_returnsLinkWithQueryAndZoom() {
        assertEquals(
            "google.navigation:q=foo+bar",
            GoogleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasQueryAndZoom_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            GoogleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            "google.navigation:q=0,0",
            GoogleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            GoogleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            GoogleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
