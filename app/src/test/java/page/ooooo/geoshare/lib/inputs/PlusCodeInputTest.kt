package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertEquals("796RWF8Q+WF", getUri("796RWF8Q+WF"))
    }

    @Test
    fun uriPattern_localCode() {
        // TODO Update local code test after adding the support
        assertNull(getUri("8F+GG"))
        assertNull(getUri("6C8F+GG"))
        assertNull(getUri("WF8Q+WF"))
        assertNull(getUri("28WR+CW"))
    }

    @Test
    fun uriPattern_localCodeWithLocality() {
        // TODO Update local code with locality test after adding the support
        assertNull(getUri(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"))
        assertNull(getUri("28WR+CW Comstock Park, Michigan"))
    }

    @Test
    fun uriPattern_localCodeWithLocalitySeparatedByMultipleSpaces_returnsNull() {
        assertNull(getUri(@Suppress("SpellCheckingInspection") "WF8Q+WF  Praia, Cabo Verde"))
        assertNull(getUri("28WR+CW  Comstock Park, Michigan"))
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
    fun parseUri_localCode() = runTest {
        // TODO Update local code test after adding the support
        assertEquals(ParseUriResult(), parseUri("8F+GG"))
        assertEquals(ParseUriResult(), parseUri("6C8F+GG"))
        assertEquals(ParseUriResult(), parseUri("WF8Q+WF"))
        assertEquals(ParseUriResult(), parseUri("28WR+CW"))
    }

    @Test
    fun parseUri_localCodeWithLocality() = runTest {
        // TODO Update local code with locality test after adding the support
        assertEquals(ParseUriResult(), parseUri(@Suppress("SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"))
        assertEquals(ParseUriResult(), parseUri("28WR+CW Comstock Park, Michigan"))
    }

    @Test
    fun parseUri_invalidCode() = runTest {
        assertEquals(ParseUriResult(), parseUri("spam"))
    }
}
