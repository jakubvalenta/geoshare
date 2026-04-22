package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class UriFormatterTest {
    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndName_returnsCoordsTemplateWithFilledVariables() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=3.4&q=foo%20bar",
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=50.123456%2C-11.123456",
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.openstreetmap.org/?mlat=50.123456&mlon=-11.123456#map=3.4/50.123456/-11.123456",
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = @Suppress("SpellCheckingInspection") "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map={z}/{lat}/{lon}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndNameAndTemplateIsMissingVariables_returnsCoordsTemplateUnchanged() {
        assertEquals(
            "https://maps.apple.com/",
            UriFormatter.formatUriString(
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
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsOutsideMainlandChinaAndCoordsTemplateHasPlusCode_returnsCoordsTemplateWithFilledPlusCode() {
        assertEquals(
            "https://www.google.com/maps/place/9C2C4VFG%2B9JM",
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/place/{plus_code}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsWithinMainlandChinaAndCoordsTemplateHasPlusCode_returnsCoordsTemplateWithFilledPlusCode() {
        assertEquals(
            "https://www.google.com/maps/place/8PFRW98W%2BWRG",
            UriFormatter.formatUriString(
                GCJ02Point(39.917313, 116.397063, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/place/{plus_code}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsEmptyAndCoordsTemplateHasPlusCode_returnsNull() {
        assertNull(
            UriFormatter.formatUriString(
                WGS84Point(source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/place/{plus_code}",
                uriQuote = FakeUriQuote,
            )
        )
    }

    @Test
    fun formatUriString_whenPointHasNameAndZoomOnly_returnsNameTemplateWithFilledQueryVariableAndZoomVariableUnchanged() {
        assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z={z}",
            UriFormatter.formatUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                nameUriTemplate = "https://maps.apple.com/?q={q}&z={z}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            UriFormatter.formatUriString(
                WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            UriFormatter.formatUriString(
                WGS84Point(lat = 50.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uriQuote = FakeUriQuote,
            ),
        )
    }

    @Test
    fun formatUriString_whenPointIsEmpty_returnsNull() {
        assertNull(
            UriFormatter.formatUriString(
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
            UriFormatter.formatUriString(
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
            UriFormatter.formatUriString(
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
            UriFormatter.formatUriString(
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
            UriFormatter.formatUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED),
                coordsUriTemplate = "",
                nameUriTemplate = "",
                uriQuote = FakeUriQuote,
            )
        )
    }
}
