package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.OsmAndUrlConverter

class OsmAndUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = OsmAndUrlConverter()

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(isSupportedUrl("ftp://osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl("https://www.example.com/"))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl("https://osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun parseUrl_pin() {
        assertEquals(
            Position("52.51628", "13.37771"),
            parseUrl("https://osmand.net/map?pin=52.51628,13.37771")
        )
    }

    @Test
    fun parseUrl_fragment() {
        assertEquals(
            Position("-53.347932", "-13.2347", z = "12.5"),
            parseUrl("https://osmand.net/map#12.5/-53.347932/-13.2347")
        )
    }

    @Test
    fun parseUrl_parameterPinTakesPrecedenceOverFragment() {
        assertEquals(
            Position("52.51628", "13.37771", z = "12.5"),
            parseUrl("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347")
        )
    }
}
