package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GeoUriFormatterTest {
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatGeoUriString_whenLastPointDoesNotHaveCoordinates_returnsUriWithZeroCoordinates() {
        assertEquals(
            "geo:0,0",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasName_returnsUriWithZeroCoordinatesAndQParam() {
        assertEquals(
            "geo:0,0?q=foo%20bar",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinates_returnsUriWithPin() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndZoom_returnsUriWithPinAndZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndName_returnsUriWithPinWithName() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndZoom_returnsUriWithPinWithNameAndZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456(foo%20bar)",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndPinFlavorIsCoordsOnlyInQ_returnsUriWithPinWithoutName() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor(
                    pin = GeoUriFlavor.PinFlavor.COORDS_ONLY_IN_Q,
                    zoom = GeoUriFlavor.ZoomFlavor.ANY,
                ),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndPinFlavorIsNotAvailable_returnsUriWithoutPinAndWithoutQParam() {
        assertEquals(
            "geo:50.123456,-11.123456",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor(
                    pin = GeoUriFlavor.PinFlavor.NOT_AVAILABLE,
                    zoom = GeoUriFlavor.ZoomFlavor.ANY,
                ),
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndZoomAndZoomFlavorIsAloneOnly_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 12.0, name = "foo bar", source = Source.GENERATED),
                GeoUriFlavor(
                    pin = GeoUriFlavor.PinFlavor.COORDS_AND_NAME_IN_Q,
                    zoom = GeoUriFlavor.ZoomFlavor.ALONE_ONLY,
                ),
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndZoomAndZoomFlavorIsNotAvailable_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            GeoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                GeoUriFlavor(
                    pin = GeoUriFlavor.PinFlavor.COORDS_AND_NAME_IN_Q,
                    zoom = GeoUriFlavor.ZoomFlavor.NOT_AVAILABLE,
                ),
                uriQuote,
            ),
        )
    }
}
