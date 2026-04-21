package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class PlusCodeInputTest : InputTest {
    override val input = PlusCodeInput

    @Test
    fun uriPattern_globalCode() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "6GCRPR6C+24",
            getUri(@Suppress("SpellCheckingInspection") "6GCRPR6C+24")
        )
        assertEquals(
            "796RWF8Q+WF",
            getUri("796RWF8Q+WF"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "8FJ3HVHW+96",
            getUri("https://plus.codes/8FJ3HVHW+96"),
        )
    }

    @Test
    fun uriPattern_localCode() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals("8F+GG", getUri("8F+GG"))
        assertEquals("6C8F+GG", getUri("6C8F+GG"))
        assertEquals("WF8Q+WF", getUri("WF8Q+WF"))
        assertEquals("28WR+CW", getUri("28WR+CW"))
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW+96",
            getUri("https://plus.codes/HVHW+96"),
        )
    }

    @Test
    fun uriPattern_localCodeWithLocality() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            @Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde",
            getUri(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"),
        )
        assertEquals(
            "28WR+CW Comstock Park, Michigan",
            getUri("28WR+CW Comstock Park, Michigan"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW+96 Angoustrine-Villeneuve-des-Escaldes, France",
            getUri("https://plus.codes/HVHW+96%20Angoustrine-Villeneuve-des-Escaldes,%20France"),
        )
    }

    @Test
    fun uriPattern_localCodeWithLocalitySeparatedByMultipleSpaces_returnsCodeWithoutLocality() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals("WF8Q+WF", getUri(@Suppress("SpellCheckingInspection") "WF8Q+WF  Praia, Cabo Verde"))
        assertEquals("28WR+CW", getUri("28WR+CW  Comstock Park, Michigan"))
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW+96",
            getUri(@Suppress("SpellCheckingInspection") "https://plus.codes/HVHW+96  Angoustrine-Villeneuve-des-Escaldes, France"),
        )
    }

    @Test
    fun uriPattern_spaces() {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            "28WR+CW foobar",
            getUri("28WR+CW foobar ")
        )
        assertEquals(
            "28WR+CW foo bar",
            getUri("28WR+CW foo bar ")
        )
        assertEquals(
            "28WR+CW foo",
            getUri("28WR+CW foo  bar")
        )
        assertEquals(
            "28WR+CW foo",
            getUri("28WR+CW foo\tbar")
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "HVHW+96 Angoustrine-Villeneuve-des-Escaldes, France",
            getUri(@Suppress("SpellCheckingInspection") "https://plus.codes/HVHW+96 Angoustrine-Villeneuve-des-Escaldes, France"),
        )
    }

    @Test
    fun parseUri_unknown() = runTest {
        assertEquals(ParseUriResult(), parseUri("spam"))
    }

    @Test
    fun parseUri_globalCode() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(-1.289938, 36.820313, source = Source.HASH)
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "6GCRPR6C+24"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(GCJ02MainlandChinaPoint(14.917313, -23.5113130, source = Source.HASH))),
            parseUri("796RWF8Q+WF"),
        )
    }

    @Test
    fun parseUri_globalCodeWithinMainlandChina() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(39.917312, 116.397078, source = Source.HASH)
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "8PFRW98W+WRG"),
        )
    }

    @Test
    fun parseUri_globalCodeWithinWesternJapan() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(34.594538, 133.758328, source = Source.HASH)
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "8Q6MHQV5+R88"),
        )
    }

    @Test
    fun parseUri_localCodeWithoutLocality_returnsEmptyResult() = runTest {
        assertEquals(ParseUriResult(), parseUri("8F+GG"))
        assertEquals(ParseUriResult(), parseUri("6C8F+GG"))
        assertEquals(ParseUriResult(), parseUri("WF8Q+WF"))
        assertEquals(ParseUriResult(), parseUri("28WR+CW"))
    }

    @Test
    fun parseUri_localCodeWithLocality() = runTest {
        assumeTrue("Local Plus Codes are not implemented yet", false)
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(0.0, 0.0, source = Source.HASH)
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(0.0, 0.0, source = Source.HASH)
                )
            ),
            parseUri("28WR+CW Comstock Park, Michigan"),
        )
    }
}
