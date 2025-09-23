package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.OpenStreetMapUrlConverter

class OpenStreetMapUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = OpenStreetMapUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/#map=16/51.49/-0.13"))
        assertTrue(doesUriPatternMatch("www.openstreetmap.org/#map=16/51.49/-0.13"))
        assertTrue(doesUriPatternMatch("openstreetmap.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://www.openstreetmap.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("51.49", "-0.13", z = "16"),
            parseUrl("https://www.openstreetmap.org/#map=16/51.49/-0.13")
        )
    }

    @Test
    fun parseUrl_coordinatesEncoded() {
        assertEquals(
            Position("51.49", "-0.13", z = "16"),
            parseUrl("https://www.openstreetmap.org/#map%3D16%2F51.49%2F-0.13")
        )
    }
}
