package page.ooooo.geoshare.lib.formatters

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.ChinaGeometryTest
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.lib.geo.WGS84Point

class UriFormatterTest {
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
    private val uriFormatter = UriFormatter(coordinateConverter)

    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndName_returnsCoordsTemplateWithFilledVariables() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=3.4&q=foo%20bar",
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=50.123456%2C-11.123456",
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndNameAndTemplateIsMissingVariables_returnsCoordsTemplateUnchanged() {
        assertEquals(
            "https://maps.apple.com/",
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesOnly_returnsCoordsTemplateWithEmptyNameVariableAndDefaultZoomVariable() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=16&q=",
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointHasNameAndZoomOnly_returnsNameTemplateWithFilledQueryVariableAndZoomVariableUnchanged() {
        assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z={z}",
            uriFormatter.formatUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                nameUriTemplate = "https://maps.apple.com/?q={q}&z={z}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            uriFormatter.formatUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            uriFormatter.formatUriString(
                WGS84Point(lat = 50.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsWGS84AndInChinaAndSrsIsGCJ02_returnsCoordsTemplateWithConvertedCoords() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285067%2C121.475524",
            uriFormatter.formatUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                srs = Srs.GCJ02,
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsGCJ02AndInChinaAndSrsIsGCJ02_returnsCoordsTemplateWithCoordsUnchanged() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285069%2C121.4755246",
            uriFormatter.formatUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                srs = Srs.GCJ02,
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsEmpty_returnsNull() {
        assertNull(
            uriFormatter.formatUriString(
                WGS84Point(source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            )
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordsAndNameAndCoordsTemplateIsEmpty_returnsNameTemplate() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                coordsUriTemplate = "",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            )
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordsOnlyAndCoordsTemplateIsEmpty_returnsNull() {
        assertNull(
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                coordsUriTemplate = "",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            )
        )
    }

    @Test
    fun formatUriString_whenPointHasNameOnlyAndNameTemplateIsEmpty_returnsNull() {
        assertNull(
            uriFormatter.formatUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "",
                uriQuote = FakeUriQuote,
            )
        )
    }

    @Test
    fun formatUriString_whenBothTemplatesAreEmpty_returnsNull() {
        assertNull(
            uriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "",
                nameUriTemplate = "",
                uriQuote = FakeUriQuote,
            )
        )
    }
}
