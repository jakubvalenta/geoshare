package page.ooooo.geoshare.lib.converters

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Srs

class OsmAndUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = OsmAndUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://osmand.net/map?pin=52.51628,13.37771"))
        assertTrue(doesUriPatternMatch("osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun parseUrl_pin() {
        assertEquals(
            Position(Srs.WGS84, 52.51628, 13.37771),
            parseUrl("https://osmand.net/map?pin=52.51628,13.37771")
        )
    }

    @Test
    fun parseUrl_fragment() {
        assertEquals(
            Position(Srs.WGS84, -53.347932, -13.2347, z = 12.5),
            parseUrl("https://osmand.net/map#12.5/-53.347932/-13.2347")
        )
    }

    @Test
    fun parseUrl_parameterPinTakesPrecedenceOverFragment() {
        assertEquals(
            Position(Srs.WGS84, 52.51628, 13.37771, z = 12.5),
            parseUrl("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347")
        )
    }
}
