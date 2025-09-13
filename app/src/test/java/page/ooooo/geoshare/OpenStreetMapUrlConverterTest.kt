package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter

class OpenStreetMapUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = OpenStreetMapUrlConverter()

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(isSupportedUrl("ftp://www.openstreetmap.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl("https://www.example.com/"))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl("https://www.openstreetmap.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("51.49", "-0.13", z = "16"),
            parseUrl("https://www.openstreetmap.org/#map=16/51.49/-0.13")
        )
    }
}
