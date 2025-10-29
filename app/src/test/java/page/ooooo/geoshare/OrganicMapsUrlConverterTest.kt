package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.OrganicMapsUrlConverter

class OrganicMapsUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = OrganicMapsUrlConverter()

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp"))
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a/"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp/"))
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a/Nova_Iorque"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp/Nova_Iorque"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/Umse5f0H8a/Nova_Iorque"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://omaps.app/Umse5f0H8a/Nova_Iorque"))
        assertFalse(doesUriPatternMatch("ftp://comaps.at/Umse5f0H8a/Nova_Iorque"))
    }

    @Test
    fun parseUrl_noPath() {
        assertNull(parseUrl("https://omaps.app"))
        assertNull(parseUrl("https://comaps.at"))
        assertNull(parseUrl("https://omaps.app/"))
        assertNull(parseUrl("https://comaps.at/"))
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            Position("40.7127403", "-74.005997", z = "9"),
            parseUrl("https://omaps.app/Umse5f0H8a/Nova_Iorque")
        )
        assertEquals(
            Position("52.4877385", "13.3815233", z = "14"),
            parseUrl("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }
}
