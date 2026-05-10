package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MapyComUriInputTest : InputTest {
    private val input = MapyComUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://mapy.cz?x=14.0184810&y=50.0525078&z=9",
            input.match("https://mapy.cz?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        @Suppress("SpellCheckingInspection") assertEquals(
            "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://mapy.com/s/jakuhelasu", input.match("https://mapy.com/s/jakuhelasu"))
        assertEquals("https://mapy.cz/s/jakuhelasu", input.match("https://mapy.cz/s/jakuhelasu"))
        @Suppress("SpellCheckingInspection") assertEquals("mapy.com/s/jakuhelasu", input.match("mapy.com/s/jakuhelasu"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("ftp://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun match_matchesCoordinatesInText() {
        assertEquals(
            "41.9966006N, 6.1223825W",
            input.match(@Suppress("SpellCheckingInspection") "Vega de Tera Calle Barrio de Abajo 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://mapy.com/en/zakladni?q=foobar",
            input.match("https://mapy.com/en/zakladni?q=foobar ")
        )
        assertEquals(
            "https://mapy.com/en/zakladni?q=foo bar",
            input.match("https://mapy.com/en/zakladni?q=foo bar ")
        )
        assertEquals(
            "https://mapy.com/en/zakladni?q=foo",
            input.match("https://mapy.com/en/zakladni?q=foo  bar")
        )
        assertEquals(
            "https://mapy.com/en/zakladni?q=foo",
            input.match("https://mapy.com/en/zakladni?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://mapy.com"))
        assertEquals(ParseResult(), input.parse("https://mapy.com/en"))
        assertEquals(ParseResult(), input.parse("https://mapy.com/en/"))
        assertEquals(ParseResult(), input.parse("https://mapy.com/en/zakladni"))
        assertEquals(ParseResult(), input.parse("https://mapy.com/en/zakladni?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            input.parse("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parse_coordinatesCsLanguage() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            input.parse("https://mapy.com/cs/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parse_coordinatesCzDomain() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            input.parse("https://mapy.cz/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parse_coordinatesOutdoorType() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            input.parse("https://mapy.com/en/turisticka?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parse_coordinatesMissingType() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            input.parse("https://mapy.com/?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parse_place() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.0992553, 14.4336590, z = 19.0, source = Source.URI))),
            input.parse("https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19"),
        )
    }

    @Test
    fun parse_textCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.9966006, -6.1223825, source = Source.TEXT))),
            input.parse(uriString = "41.9966006N, 6.1223825W"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-41.9966006, 6.1223825, source = Source.TEXT))),
            input.parse(uriString = "41.9966006S, 6.1223825E"),
        )
    }
}
