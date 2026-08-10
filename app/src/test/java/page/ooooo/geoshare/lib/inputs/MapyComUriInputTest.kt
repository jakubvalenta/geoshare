package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MapyComUriInputTest : InputTest {
    private val input = FakeInputRepository.mapyComUriInput

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
        assertEquals(
            "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://mapy.com/s/jakuhelasu", input.match("https://mapy.com/s/jakuhelasu"))
        assertEquals("https://mapy.cz/s/jakuhelasu", input.match("https://mapy.cz/s/jakuhelasu"))
        assertEquals("mapy.com/s/jakuhelasu", input.match("mapy.com/s/jakuhelasu"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.match("ftp://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun match_matchesCoordinatesInText() {
        assertEquals(
            "41.9966006N, 6.1223825W",
            input.match("Vega de Tera Calle Barrio de Abajo 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha"),
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
    fun parse_navigation() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(52.468305230140686, 13.430708348751068, source = Source.HASH),
                    WGS84Point(52.423603534698486, 13.521436750888824, source = Source.HASH),
                    WGS84Point(52.41917721927166, 13.543593138456345, source = Source.HASH),
                    WGS84Point(52.40594454109669, 13.620681166648865, source = Source.HASH),
                )
            ),
            input.parse(uriString = "https://mapy.com/en/turisticka?planovani-trasy&rc=9eJ.Ex5-GG9eY14x5Ls1jBHe8t9emdXcI5&rs=osm&rs=osm&rs=osm&rs=pubt&ri=20227705&ri=120789104&ri=81032078&ri=28872262&mrp=%7B%22c%22%3A121%2C%22dt%22%3A%22%22%2C%22d%22%3Atrue%7D&xc=%5B%5D&rbf=alf&x=13.4308032&y=52.4684951&z=18"),
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
