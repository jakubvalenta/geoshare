package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

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
            ParseUriResult(persistentListOf(WGS84Point(-1.289938, 36.820313, source = Source.HASH))),
            parseUri(@Suppress("SpellCheckingInspection") "6GCRPR6C+24"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(14.917313, -23.5113130, source = Source.HASH))),
            parseUri("796RWF8Q+WF"),
        )
    }

    // TODO Test global code within mainland China

    // TODO Test global code within western Japan

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
