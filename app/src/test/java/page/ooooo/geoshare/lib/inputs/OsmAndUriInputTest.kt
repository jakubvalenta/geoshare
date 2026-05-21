package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OsmAndUriInputTest : InputTest {
    private val input = FakeInputRepository.osmAndUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://osmand.net/map?pin=52.51628,13.37771",
            input.match("https://osmand.net/map?pin=52.51628,13.37771")
        )
        assertEquals("osmand.net/map?pin=52.51628,13.37771", input.match("osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "osmand.net/map?pin=52.51628,13.37771",
            input.match("ftp://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://osmand.net/?q=foobar",
            input.match("https://osmand.net/?q=foobar ")
        )
        assertEquals(
            "https://osmand.net/?q=foo bar",
            input.match("https://osmand.net/?q=foo bar ")
        )
        assertEquals(
            "https://osmand.net/?q=foo",
            input.match("https://osmand.net/?q=foo  bar")
        )
        assertEquals(
            "https://osmand.net/?q=foo",
            input.match("https://osmand.net/?q=foo\tbar")
        )
    }

    @Test
    fun parse_pin() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.51628, 13.37771, source = Source.URI))),
            input.parse("https://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun parse_directions() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(52.5, 13.5, source = Source.URI),
                    WGS84Point(52.51628, 13.37771, source = Source.URI),
                )
            ),
            input.parse("https://osmand.net/map?start=52.5,13.5&finish=52.51628,13.37771"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.5, 13.5, source = Source.URI))),
            input.parse("https://osmand.net/map?start=52.5,13.5"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.51628, 13.37771, source = Source.URI))),
            input.parse("https://osmand.net/map?finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parse_directionsTakesPrecedenceOverPin() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(52.5, 13.5, source = Source.URI),
                    WGS84Point(52.51628, 13.37771, source = Source.URI),
                )
            ),
            input.parse("https://osmand.net/map?pin=52.51628,13.37771&start=52.5,13.5&finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parse_fragment() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-53.347932, -13.2347, z = 12.5, source = Source.MAP_CENTER))),
            input.parse("https://osmand.net/map#12.5/-53.347932/-13.2347"),
        )
    }

    @Test
    fun parse_parameterPinTakesPrecedenceOverFragment() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.51628, 13.37771, z = 12.5, source = Source.URI))),
            input.parse("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347"),
        )
    }
}
