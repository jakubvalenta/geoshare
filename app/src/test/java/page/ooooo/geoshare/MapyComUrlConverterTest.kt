package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.MapyComUrlConverter

@Suppress("SpellCheckingInspection")
class MapyComUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = MapyComUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://mapy.cz?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://mapy.com/s/jakuhelasu"))
        assertTrue(doesUriPatternMatch("https://mapy.cz/s/jakuhelasu"))
        assertTrue(doesUriPatternMatch("mapy.com/s/jakuhelasu"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://mapy.com"))
        assertNull(parseUrl("https://mapy.com/en"))
        assertNull(parseUrl("https://mapy.com/en/"))
        assertNull(parseUrl("https://mapy.com/en/zakladni"))
        assertNull(parseUrl("https://mapy.com/en/zakladni?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("50.0525078", "14.0184810", z = "9"),
            parseUrl("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun parseUrl_coordinatesCsLanguage() {
        assertEquals(
            Position("50.0525078", "14.0184810", z = "9"),
            parseUrl("https://mapy.com/cs/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun parseUrl_coordinatesCzDomain() {
        assertEquals(
            Position("50.0525078", "14.0184810", z = "9"),
            parseUrl("https://mapy.cz/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun parseUrl_coordinatesOutdoorType() {
        assertEquals(
            Position("50.0525078", "14.0184810", z = "9"),
            parseUrl("https://mapy.com/en/turisticka?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun parseUrl_coordinatesMissingType() {
        assertEquals(
            Position("50.0525078", "14.0184810", z = "9"),
            parseUrl("https://mapy.com/?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position("50.0992553", "14.4336590", z = "19"),
            parseUrl("https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19")
        )
    }

    @Test
    fun isShortUrl_correct() {
        assertTrue(isShortUrl("https://mapy.com/s/jakuhelasu"))
        assertTrue(isShortUrl("https://www.mapy.com/s/jakuhelasu"))
        assertTrue(isShortUrl("https://mapy.cz/s/jakuhelasu"))
    }

    @Test
    fun isShortUri_wrongPath() {
        assertFalse(isShortUrl("https://mapy.com/"))
        assertFalse(isShortUrl("https://mapy.com/s"))
        assertFalse(isShortUrl("https://mapy.com/s/"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUrl("https://www.example.com/foo"))
    }
}
