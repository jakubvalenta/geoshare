package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class Ge0InputTest : BaseInputTest() {
    override val input = Ge0Input

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("ge0://AbCMCNp0LO"))
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO"))
        assertTrue(doesUriPatternMatch("https://omaps.app/AbCMCNp0LO"))
        assertTrue(doesUriPatternMatch("https://comaps.at/AbCMCNp0LO"))
        assertTrue(doesUriPatternMatch("ge0://AbCMCNp0LO/"))
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO/"))
        assertTrue(doesUriPatternMatch("https://omaps.app/AbCMCNp0LO/"))
        assertTrue(doesUriPatternMatch("https://comaps.at/AbCMCNp0LO/"))
        assertTrue(doesUriPatternMatch("ge0://AbCMCNp0LO/Madagascar"))
        assertTrue(doesUriPatternMatch("http://ge0.me/AbCMCNp0LO/Madagascar"))
        assertTrue(doesUriPatternMatch("https://omaps.app/AbCMCNp0LO/Madagascar"))
        assertTrue(doesUriPatternMatch("https://comaps.at/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://ge0.me/AbCMCNp0LO/Madagascar"))
        assertFalse(doesUriPatternMatch("ftp://omaps.app/AbCMCNp0LO/Madagascar"))
        assertFalse(doesUriPatternMatch("ftp://comaps.at/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun parseUri_noPath() {
        assertNull(parseUri("ge0:"))
        assertNull(parseUri("http://ge0.me"))
        assertNull(parseUri("https://omaps.app"))
        assertNull(parseUri("https://comaps.at"))
        assertNull(parseUri("ge0:/"))
        assertNull(parseUri("ge0://"))
        assertNull(parseUri("http://ge0.me/"))
        assertNull(parseUri("https://omaps.app/"))
        assertNull(parseUri("https://comaps.at/"))
    }

    @Test
    fun parseUri_shortLink() {
        assertEquals(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0),
            parseUri("ge0://ApYSV0YTAl/América_do_Norte"),
        )
        assertEquals(
            Position(Srs.WGS84, -18.9249432, 46.4416404, z = 4.0),
            parseUri("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            Position(Srs.WGS84, 40.7127405, -74.005997, z = 9.0),
            parseUri("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            Position(Srs.WGS84, 52.4877386, 13.3815233, z = 14.0),
            parseUri("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }

    @Test
    fun parseUri_hostThatLooksLikeHash() {
        assertNull(parseUri("https://ApYSV0YTAl/"))
    }
}
