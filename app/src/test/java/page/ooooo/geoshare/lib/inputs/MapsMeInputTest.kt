package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

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
        assertEquals(
            "ge0.me/AbCMCNp0LO/Madagascar",
            input.uriPattern.find("ftp://ge0.me/AbCMCNp0LO/Madagascar")?.value,
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "omaps.app/AbCMCNp0LO/Madagascar",
            input.uriPattern.find("ftp://omaps.app/AbCMCNp0LO/Madagascar")?.value,
        )
        assertEquals(
            "comaps.at/AbCMCNp0LO/Madagascar",
            input.uriPattern.find("ftp://comaps.at/AbCMCNp0LO/Madagascar")?.value,
        )
    }

    @Test
    fun parseUri_noPath() = runTest {
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
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(
                persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0, name = "América do Norte"))
            ),
            parseUri("ge0://ApYSV0YTAl/América_do_Norte"),
        )
        assertEquals(
            ParseUriResult.Succeeded(
                persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0, name = "Madagascar"))
            ),
            parseUri("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseUriResult.Succeeded(
                persistentListOf(WGS84Point(40.7127405, -74.005997, z = 9.0, name = "Nova Iorque"))
            ),
            parseUri("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            ParseUriResult.Succeeded(
                persistentListOf(WGS84Point(52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg"))
            ),
            parseUri("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }

    @Test
    fun parseUri_shortLinkWithoutName() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0))),
            parseUri("ge0://ApYSV0YTAl"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0))),
            parseUri("ge0://ApYSV0YTAl/"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0))),
            parseUri("http://ge0.me/AbCMCNp0LO"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0))),
            parseUri("http://ge0.me/AbCMCNp0LO/"),
        )
    }

    @Test
    fun parseUri_hostThatLooksLikeHash() = runTest {
        assertNull(parseUri("https://ApYSV0YTAl/"))
    }
}
