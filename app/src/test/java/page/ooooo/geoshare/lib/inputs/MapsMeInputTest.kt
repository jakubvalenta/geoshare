package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class MapsMeInputTest : BaseInputTest() {
    override val input = MapsMeInput

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
        assertEquals(
            Position() to null,
            parseUri("ge0:")
        )
        assertEquals(
            Position() to null,
            parseUri("http://ge0.me")
        )
        assertEquals(
            Position() to null,
            parseUri("https://omaps.app")
        )
        assertEquals(
            Position() to null,
            parseUri("https://comaps.at")
        )
        assertEquals(
            Position() to null,
            parseUri("ge0:/")
        )
        assertEquals(
            Position() to null,
            parseUri("ge0://")
        )
        assertEquals(
            Position() to null,
            parseUri("http://ge0.me/")
        )
        assertEquals(
            Position() to null,
            parseUri("https://omaps.app/")
        )
        assertEquals(
            Position() to null,
            parseUri("https://comaps.at/")
        )
    }

    @Test
    fun parseUri_shortLink() {
        assertEquals(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0, name = "América do Norte") to null,
            parseUri("ge0://ApYSV0YTAl/América_do_Norte"),
        )
        assertEquals(
            Position(Srs.WGS84, -18.9249432, 46.4416404, z = 4.0, name = "Madagascar") to null,
            parseUri("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127405, -74.005997, z = 9.0, name = "Nova Iorque") to null,
            parseUri("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            Position(Srs.WGS84, 52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg") to null,
            parseUri("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }

    @Test
    fun parseUri_shortLinkWithoutName() {
        assertEquals(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0) to null,
            parseUri("ge0://ApYSV0YTAl"),
        )
        assertEquals(
            Position(Srs.WGS84, 51.0000004, -108.9999868, z = 4.0) to null,
            parseUri("ge0://ApYSV0YTAl/"),
        )
        assertEquals(
            Position(Srs.WGS84, -18.9249432, 46.4416404, z = 4.0) to null,
            parseUri("http://ge0.me/AbCMCNp0LO"),
        )
        assertEquals(
            Position(Srs.WGS84, -18.9249432, 46.4416404, z = 4.0) to null,
            parseUri("http://ge0.me/AbCMCNp0LO/"),
        )
    }

    @Test
    fun parseUri_hostThatLooksLikeHash() {
        assertEquals(
            Position() to null,
            parseUri("https://ApYSV0YTAl/")
        )
    }
}
