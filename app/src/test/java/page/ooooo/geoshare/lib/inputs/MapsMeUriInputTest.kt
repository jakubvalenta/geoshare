package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MapsMeUriInputTest : InputTest {
    private val input = MapsMeUriInput

    @Test
    fun match_shortLink() {
        assertEquals("ge0://AbCMCNp0LO", input.match("ge0://AbCMCNp0LO"))
        assertEquals("http://ge0.me/AbCMCNp0LO", input.match("http://ge0.me/AbCMCNp0LO"))
        assertEquals("https://omaps.app/AbCMCNp0LO", input.match("https://omaps.app/AbCMCNp0LO"))
        assertEquals("https://comaps.at/AbCMCNp0LO", input.match("https://comaps.at/AbCMCNp0LO"))
        assertEquals("ge0://AbCMCNp0LO/", input.match("ge0://AbCMCNp0LO/"))
        assertEquals("http://ge0.me/AbCMCNp0LO/", input.match("http://ge0.me/AbCMCNp0LO/"))
        assertEquals("https://omaps.app/AbCMCNp0LO/", input.match("https://omaps.app/AbCMCNp0LO/"))
        assertEquals("https://comaps.at/AbCMCNp0LO/", input.match("https://comaps.at/AbCMCNp0LO/"))
        assertEquals("ge0://AbCMCNp0LO/Madagascar", input.match("ge0://AbCMCNp0LO/Madagascar"))
        assertEquals("http://ge0.me/AbCMCNp0LO/Madagascar", input.match("http://ge0.me/AbCMCNp0LO/Madagascar"))
        assertEquals("https://omaps.app/AbCMCNp0LO/Madagascar", input.match("https://omaps.app/AbCMCNp0LO/Madagascar"))
        assertEquals("https://comaps.at/AbCMCNp0LO/Madagascar", input.match("https://comaps.at/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/AbCMCNp0LO/Madagascar"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "ge0.me/AbCMCNp0LO/Madagascar",
            input.match("ftp://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "omaps.app/AbCMCNp0LO/Madagascar",
            input.match("ftp://omaps.app/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            "comaps.at/AbCMCNp0LO/Madagascar",
            input.match("ftp://comaps.at/AbCMCNp0LO/Madagascar"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://ge0.me/?q=foobar",
            input.match("https://ge0.me/?q=foobar ")
        )
        assertEquals(
            "https://ge0.me/?q=foo bar",
            input.match("https://ge0.me/?q=foo bar ")
        )
        assertEquals(
            "https://ge0.me/?q=foo",
            input.match("https://ge0.me/?q=foo  bar")
        )
        assertEquals(
            "https://ge0.me/?q=foo",
            input.match("https://ge0.me/?q=foo\tbar")
        )
    }

    @Test
    fun parse_noPath() = runTest {
        assertEquals(ParseResult(), input.parse("ge0:"))
        assertEquals(ParseResult(), input.parse("http://ge0.me"))
        assertEquals(ParseResult(), input.parse("https://omaps.app"))
        assertEquals(ParseResult(), input.parse("https://comaps.at"))
        assertEquals(ParseResult(), input.parse("ge0:/"))
        assertEquals(ParseResult(), input.parse("ge0://"))
        assertEquals(ParseResult(), input.parse("http://ge0.me/"))
        assertEquals(ParseResult(), input.parse("https://omaps.app/"))
        assertEquals(ParseResult(), input.parse("https://comaps.at/"))
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(
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
            input.parse("ge0://ApYSV0YTAl/América_do_Norte"),
        )
        assertEquals(
            ParseResult(
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
            input.parse("http://ge0.me/AbCMCNp0LO/Madagascar"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseResult(
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
            input.parse("https://omaps.app/Umse5f0H8a/Nova_Iorque"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(WGS84Point(52.4877386, 13.3815233, z = 14.0, name = "Kreuzberg", source = Source.HASH))
            ),
            input.parse("https://comaps.at/o4MnIOApKp/Kreuzberg"),
        )
    }

    @Test
    fun parse_shortLinkWithoutName() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0, source = Source.HASH))),
            input.parse("ge0://ApYSV0YTAl"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.0000004, -108.9999868, z = 4.0, source = Source.HASH))),
            input.parse("ge0://ApYSV0YTAl/"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0, source = Source.HASH))),
            input.parse("http://ge0.me/AbCMCNp0LO"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-18.9249432, 46.4416404, z = 4.0, source = Source.HASH))),
            input.parse("http://ge0.me/AbCMCNp0LO/"),
        )
    }

    @Test
    fun parse_hostThatLooksLikeHash() = runTest {
        assertEquals(ParseResult(), input.parse("https://ApYSV0YTAl/"))
    }
}
