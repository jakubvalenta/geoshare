package page.ooooo.geoshare.lib.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.Position

class Ge0UrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = Ge0UrlConverter()

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO"))
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp"))
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO/"))
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a/"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp/"))
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO/Madagascar"))
        assertTrue(doesUriPatternMatch("https://omaps.app/Umse5f0H8a/Nova_Iorque"))
        assertTrue(doesUriPatternMatch("https://comaps.at/o4MnIOApKp/Nova_Iorque"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/Umse5f0H8a/Nova_Iorque"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://ge0.me/AbCMCNp0LO/Madagascar"))
        assertFalse(doesUriPatternMatch("ftp://omaps.app/Umse5f0H8a/Nova_Iorque"))
        assertFalse(doesUriPatternMatch("ftp://comaps.at/Umse5f0H8a/Nova_Iorque"))
    }

    @Test
    fun parseUrl_noPath() {
        assertNull(parseUrl("http://ge0.me"))
        assertNull(parseUrl("https://omaps.app"))
        assertNull(parseUrl("https://comaps.at"))
        assertNull(parseUrl("http://ge0.me/"))
        assertNull(parseUrl("https://omaps.app/"))
        assertNull(parseUrl("https://comaps.at/"))
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            Position("-18.9249433", "46.4416404", z = "4"),
            parseUrl("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            Position("40.7127403", "-74.005997", z = "9"),
            parseUrl("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            Position("52.4877385", "13.3815233", z = "14"),
            parseUrl("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }
}
