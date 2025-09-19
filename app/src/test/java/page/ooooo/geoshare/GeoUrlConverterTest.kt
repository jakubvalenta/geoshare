package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.GeoUrlConverter
import page.ooooo.geoshare.lib.converters.UrlConverter

class GeoUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter: UrlConverter = GeoUrlConverter()

    @Test
    fun uriPattern_geoUri() {
        assertTrue(doesUriPatternMatch("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_noScheme() {
        assertFalse(doesUriPatternMatch("50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_nonGeoScheme() {
        assertFalse(doesUriPatternMatch("ftp:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_unknownPath() {
        assertTrue(doesUriPatternMatch("geo:example?q=foo%20bar&z=3.4"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("geo:"))
        assertNull(parseUrl("geo:?spam=1"))
    }

    @Test
    fun parseUrl_returnsAllCoordsAndParams() {
        assertEquals(
            Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"),
            parseUrl("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }
}
