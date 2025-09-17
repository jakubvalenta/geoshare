package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position

class PositionTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun toParamsString_returnsQueryAndZoomButNotPoints() {
        assertEquals(
            "foo bar \u2022 z3.4",
            Position(
                lat = "50.123456",
                lon = "11.123456",
                q = "foo bar",
                z = "3.4",
                points = listOf(
                    "59.1293656" to "11.4585672",
                    "59.4154007" to "11.659710599999999",
                    "59.147731699999994" to "11.550661199999999",
                ),
            ).toParamsString(),
        )
    }

    @Test
    fun toAppleMapsUriString_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=3.4",
            Position("50.123456", "-11.123456", z = "3.4").toAppleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toAppleMapsUriString_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=3.4",
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4").toAppleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toAppleMapsUriString_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z=3.4",
            Position(q = "foo bar", z = "3.4").toAppleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toGoogleMapsUriString_whenUriHasCoordinatesAndZoom_returnsCoordinatesAsQueryAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=50.123456%2C-11.123456&z=3.4",
            Position("50.123456", "-11.123456", z = "3.4").toGoogleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toGoogleMapsUriString_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=foo%20bar&z=3.4",
            Position(q = "foo bar", z = "3.4").toGoogleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toGoogleMapsUriString_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            "https://www.google.com/maps?q=50.123456%2C-11.123456&z=3.4",
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4").toGoogleMapsUriString(uriQuote),
        )
    }

    @Test
    fun toMagicEarthUriString_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        assertEquals(
            "magicearth://?lat=50.123456&lon=-11.123456&zoom=3.4",
            Position("50.123456", "-11.123456", z = "3.4").toMagicEarthUriString(uriQuote),
        )
    }

    @Test
    fun toMagicEarthUriString_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndQueryAndZoom() {
        assertEquals(
            "magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4",
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4").toMagicEarthUriString(uriQuote),
        )
    }

    @Test
    fun toNorthSouthWestEastDecCoordsString_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "17.2165721° S, 149.9470294° W",
            Position("-17.2165721", "-149.9470294").toNorthSouthWestEastDecCoordsString(),
        )
    }

    @Test
    fun toNorthSouthWestEastDecCoordsString_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52.5067296° N, 13.2599309° E",
            Position("52.5067296", "13.2599309").toNorthSouthWestEastDecCoordsString(),
        )
    }

    @Test
    fun toGpx() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456" />
<wpt lat="52.5067296" lon="13.2599309" />
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = listOf(
                        "50.123456" to "-11.123456",
                        "52.5067296" to "13.2599309",
                    ),
                ).toGpx(this, uriQuote)
            }.toString()
        )
    }

    @Test
    fun toGpx_escapesSpecialCharacters() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="%22" lon="%3C" />
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = listOf(
                        "\"" to "<",
                    ),
                ).toGpx(this, uriQuote)
            }.toString()
        )
    }

    @Test
    fun toGpx_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
</gpx>
""",
            StringBuilder().apply {
                Position(
                    lat = "52.5067296",
                    lon = "13.2599309",
                ).toGpx(this, uriQuote)
            }.toString()
        )
    }
}
