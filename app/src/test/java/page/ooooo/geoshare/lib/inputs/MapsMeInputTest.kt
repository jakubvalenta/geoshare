package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point

class MapsMeInputTest : InputTest {
    override val input = MapsMeInput()

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("ge0://AbCMCNp0LO", getUri("ge0://AbCMCNp0LO"))
        assertEquals("http://ge0.me/AbCMCNp0LO", getUri("http://ge0.me/AbCMCNp0LO"))
        assertEquals("https://omaps.app/AbCMCNp0LO", getUri("https://omaps.app/AbCMCNp0LO"))
        assertEquals("https://comaps.at/AbCMCNp0LO", getUri("https://comaps.at/AbCMCNp0LO"))
        assertEquals("ge0://AbCMCNp0LO/", getUri("ge0://AbCMCNp0LO/"))
        assertEquals("http://ge0.me/AbCMCNp0LO/", getUri("http://ge0.me/AbCMCNp0LO/"))
        assertEquals("https://omaps.app/AbCMCNp0LO/", getUri("https://omaps.app/AbCMCNp0LO/"))
        assertEquals("https://comaps.at/AbCMCNp0LO/", getUri("https://comaps.at/AbCMCNp0LO/"))
        assertEquals("ge0://AbCMCNp0LO/Madagascar", getUri("ge0://AbCMCNp0LO/Madagascar"))
        assertEquals("http://ge0.me/AbCMCNp0LO/Madagascar", getUri("http://ge0.me/AbCMCNp0LO/Madagascar"))
        assertEquals("https://omaps.app/AbCMCNp0LO/Madagascar", getUri("https://omaps.app/AbCMCNp0LO/Madagascar"))
        assertEquals("https://comaps.at/AbCMCNp0LO/Madagascar", getUri("https://comaps.at/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/AbCMCNp0LO/Madagascar"))
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
        assertEquals(ParseUriResult(), parseUri("ge0:"))
        assertEquals(ParseUriResult(), parseUri("http://ge0.me"))
        assertEquals(ParseUriResult(), parseUri("https://omaps.app"))
        assertEquals(ParseUriResult(), parseUri("https://comaps.at"))
        assertEquals(ParseUriResult(), parseUri("ge0:/"))
        assertEquals(ParseUriResult(), parseUri("ge0://"))
        assertEquals(ParseUriResult(), parseUri("http://ge0.me/"))
        assertEquals(ParseUriResult(), parseUri("https://omaps.app/"))
        assertEquals(ParseUriResult(), parseUri("https://comaps.at/"))
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        51.0000004,
                        -108.9999868,
                        z = 4.0,
                        name = "América do Norte",
                        source = Source.HASH
                    )
                )
            ),
            parseUri("ge0://ApYSV0YTAl/América_do_Norte"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        -18.9249432,
                        46.4416404,
                        z = 4.0,
                        name = "Madagascar",
                        source = Source.HASH
                    )
                )
            ),
            parseUri("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        40.7127405,
                        -74.005997,
                        z = 9.0,
                        name = "Nova Iorque",
                        source = Source.HASH
                    )
                )
            ),
            parseUri("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(WGS84Point(52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg", source = Source.HASH))
            ),
            parseUri("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }

    @Test
    fun parseUri_shortLinkWithoutName() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0, source = Source.HASH))),
            parseUri("ge0://ApYSV0YTAl"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0, source = Source.HASH))),
            parseUri("ge0://ApYSV0YTAl/"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0, source = Source.HASH))),
            parseUri("http://ge0.me/AbCMCNp0LO"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0, source = Source.HASH))),
            parseUri("http://ge0.me/AbCMCNp0LO/"),
        )
    }

    @Test
    fun parseUri_hostThatLooksLikeHash() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://ApYSV0YTAl/"))
    }
}
