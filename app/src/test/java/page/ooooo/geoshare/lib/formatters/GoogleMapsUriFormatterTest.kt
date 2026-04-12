package page.ooooo.geoshare.lib.formatters

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.ChinaGeometryTest
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GoogleMapsUriFormatterTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("china_ne_10m.wkb") } doReturn
            (ChinaGeometryTest::class.java.getResourceAsStream("/china_ne_10m.wkb")
                ?: error("china_ne_10m.wkb not found in test resources"))
    }
    private val mockContext: Context = mock {
        on { assets } doReturn mockAssetManager
    }
    private val chinaGeometry = ChinaGeometry(mockContext)
    private val coordinateConverter = CoordinateConverter(chinaGeometry)
    private val googleMapsUriFormatter = GoogleMapsUriFormatter(coordinateConverter)
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatNavigationUriString_whenLastPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasQueryAndZoom_returnsLinkWithQueryAndZoom() {
        assertEquals(
            "google.navigation:q=foo+bar",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasQueryAndZoom_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=50.123456,-11.123456",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointIsWithinChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285067,121.475524",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointIsWithinChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285067,121.475524",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointIsWithinChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285069,121.4755246",
            googleMapsUriFormatter.formatNavigationUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointIsWithinChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285069,121.4755246",
            googleMapsUriFormatter.formatStreetViewUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenLastPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            "google.navigation:q=0,0",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenLastPointHasNeitherPointNorQuery_returnsZeroCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=0,0",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            "google.navigation:q=50.123456,-11.123456",
            googleMapsUriFormatter.formatNavigationUriString(
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
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointIsWithinChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285067,121.475524",
            googleMapsUriFormatter.formatNavigationUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointIsWithinChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285067,121.475524",
            googleMapsUriFormatter.formatStreetViewUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatNavigationUriString_whenPointIsWithinChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            "google.navigation:q=31.2285069,121.4755246",
            googleMapsUriFormatter.formatNavigationUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatStreetViewUriString_whenPointIsWithinChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285069,121.4755246",
            googleMapsUriFormatter.formatStreetViewUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
