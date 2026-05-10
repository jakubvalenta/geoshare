package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MagicEarthUriInputTest : InputTest {
    private val input = MagicEarthUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
            input.match("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345")
        )
        assertEquals(
            "magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
            input.match("magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345")
        )
        assertEquals(
            "magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4",
            input.match("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
            input.match("ftp://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://magicearth.com/?q=foobar",
            input.match("https://magicearth.com/?q=foobar ")
        )
        assertEquals(
            "https://magicearth.com/?q=foo bar",
            input.match("https://magicearth.com/?q=foo bar ")
        )
        assertEquals(
            "https://magicearth.com/?q=foo",
            input.match("https://magicearth.com/?q=foo  bar")
        )
        assertEquals(
            "https://magicearth.com/?q=foo",
            input.match("https://magicearth.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://magicearth.com/"))
        assertEquals(ParseResult(), input.parse("https://magicearth.com/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(48.85649, 2.35216, source = Source.URI))),
            input.parse("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"),
        )
    }

    @Test
    fun parse_place() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Central Park", source = Source.URI))),
            input.parse("https://magicearth.com/?name=Central Park"),
        )
    }

    @Test
    fun parse_search() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Paris", z = 5.0, source = Source.URI))),
            input.parse("https://magicearth.com/?q=Paris&mapmode=standard&z=5"),
        )
    }

    @Test
    fun parse_destinationAddress() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "CH1 6BJ United Kingdom", source = Source.URI))),
            input.parse("https://magicearth.com/?daddr=CH1+6BJ+United+Kingdom"),
        )
    }

    @Test
    fun parse_parametersLatAndLonTakePrecedenceOverQ() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        -17.2165721, -149.9470294,
                        name = "Central Park",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://magicearth.com/?lat=-17.2165721&lon=-149.9470294&q=Central Park"),
        )
    }

    @Test
    fun parse_parameterDestinationAddressTakesPrecedenceOverQ() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://magicearth.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Test
    fun parse_parameterNameTakesPrecedenceOverQ() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://magicearth.com/?name=Reuterplatz&q=Central%20Park"),
        )
    }

    @Test
    fun parse_customScheme() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        50.123456, -11.123456,
                        z = 3.4,
                        name = "foo bar",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4"),
        )
    }
}
