package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class PlusCodeUriInputTest : InputTest {
    private val input = FakeInputRepository.plusCodeInput

    @Test
    fun match_globalCode() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "6GCRPR6C+24",
            input.match(@Suppress("SpellCheckingInspection") "6GCRPR6C+24")
        )
        assertEquals(
            "796RWF8Q+WF",
            input.match("796RWF8Q+WF"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "8FJ3HVHW%2B96",
            input.match("https://plus.codes/8FJ3HVHW%2B96"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "8FJ3HVHW%2B96",
            input.match("https://www.google.com/maps/place/8FJ3HVHW%2B96"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "8FJ3HVHW%2B96",
            input.match(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/8FJ3HVHW%2B96/@42.5784375,1.8955625,17z/"),
        )
    }

    @Test
    fun match_localCode() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals("8F+GG", input.match("8F+GG"))
        assertEquals("6C8F+GG", input.match("6C8F+GG"))
        assertEquals("WF8Q+WF", input.match("WF8Q+WF"))
        assertEquals("28WR+CW", input.match("28WR+CW"))
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96",
            input.match("https://plus.codes/HVHW%2B96"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96",
            input.match("https://www.google.com/maps/place/HVHW%2B96"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96",
            input.match(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/HVHW%2B96/@42.5784375,1.8955625,17z/"),
        )
    }

    @Test
    fun match_localCodeWithLocality() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            @Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde",
            input.match(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"),
        )
        assertEquals(
            "28WR+CW Comstock Park, Michigan",
            input.match("28WR+CW Comstock Park, Michigan"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France",
            input.match("https://plus.codes/HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France",
            input.match("https://www.google.com/maps/place/HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France",
            input.match(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France/@42.5784375,1.8955625,17z/"),
        )
    }

    @Test
    fun match_localCodeWithLocalitySeparatedByMultipleSpaces_returnsCodeWithoutLocality() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals("WF8Q+WF", input.match(@Suppress("SpellCheckingInspection") "WF8Q+WF  Praia, Cabo Verde"))
        assertEquals("28WR+CW", input.match("28WR+CW  Comstock Park, Michigan"))
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96",
            input.match(@Suppress("SpellCheckingInspection") "https://plus.codes/HVHW%2B96  Angoustrine-Villeneuve-des-Escaldes, France"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96",
            input.match(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/HVHW%2B96  Angoustrine-Villeneuve-des-Escaldes, France"),
        )
    }

    @Test
    fun match_spaces() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            "28WR+CW foobar",
            input.match("28WR+CW foobar ")
        )
        assertEquals(
            "28WR+CW foo bar",
            input.match("28WR+CW foo bar ")
        )
        assertEquals(
            "28WR+CW foo",
            input.match("28WR+CW foo  bar")
        )
        assertEquals(
            "28WR+CW foo",
            input.match("28WR+CW foo\tbar")
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW%2B96 Angoustrine-Villeneuve-des-Escaldes, France",
            input.match(@Suppress("SpellCheckingInspection") "https://plus.codes/HVHW%2B96 Angoustrine-Villeneuve-des-Escaldes, France"),
        )
    }

    @Test
    fun parse_unknown() = runTest {
        assertEquals(ParseResult(), input.parse("spam"))
    }

    @Test
    fun parse_globalCode() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(-1.289938, 36.820313, source = Source.HASH)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "6GCRPR6C+24"),
        )
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(14.917313, -23.5113130, source = Source.HASH))),
            input.parse("796RWF8Q+WF"),
        )
    }

    @Test
    fun parse_globalCodeUriEncoded() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(42.578438, 1.895563, source = Source.HASH)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "8FJ3HVHW%2B96"),
        )
    }

    @Test
    fun parse_globalCodeWithinMainlandChina() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(39.917312, 116.397078, source = Source.HASH)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "8PFRW98W+WRG"),
        )
    }

    @Test
    fun parse_globalCodeWithinWesternJapan() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(34.594538, 133.758328, source = Source.HASH)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "8Q6MHQV5+R88"),
        )
    }

    @Test
    fun parse_localCodeWithoutLocality_returnsNoPoints() = runTest {
        assertEquals(ParseResult(), input.parse("8F+GG"))
        assertEquals(ParseResult(), input.parse("6C8F+GG"))
        assertEquals(ParseResult(), input.parse("WF8Q+WF"))
        assertEquals(ParseResult(), input.parse("28WR+CW"))
    }

    @Test
    fun parse_localCodeWithLocality() = runTest {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(0.0, 0.0, source = Source.HASH)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(0.0, 0.0, source = Source.HASH)
                )
            ),
            input.parse("28WR+CW Comstock Park, Michigan"),
        )
    }
}
