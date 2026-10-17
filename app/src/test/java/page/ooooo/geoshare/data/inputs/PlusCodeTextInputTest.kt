package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point

class PlusCodeTextInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.plusCodeTextInput

    @Test
    fun match_globalPlusCode() {
        assertEquals(
            @Suppress("GrazieInspectionRunner") "6GCRPR6C+24",
            input.match(@Suppress("GrazieInspectionRunner") "6GCRPR6C+24")
        )
        assertEquals(
            "796RWF8Q+WF",
            input.match("796RWF8Q+WF"),
        )
        assertEquals(
            "796RWF8Q+WF",
            input.match("spam 796RWF8Q+WF spam"),
        )
    }

    @Test
    fun match_globalPlusCodeInText_returnsMatch() {
        assertEquals(
            "796RWF8Q+WF",
            input.match("spam 796RWF8Q+WF spam"),
        )
    }

    @Test
    fun match_globalPlusCodeNotSeparatedByWhitespace_returnsNull() {
        assertNull(input.match("x796RWF8Q+WFx"))
    }

    @Test
    fun match_localPlusCode() {
        // Local Plus Codes are not implemented yet
        assertNull(input.match("8F+GG"))
        assertNull(input.match("6C8F+GG"))
        assertNull(input.match("WF8Q+WF"))
        assertNull(input.match("28WR+CW"))
    }

    @Test
    fun match_localPlusCodeWithLocality() {
        // Local Plus Codes are not implemented yet
        assertNull(
            input.match(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde")
        )
        assertNull(input.match("28WR+CW Comstock Park, Michigan"))
    }

    @Test
    fun match_localPlusCodeWithLocalitySeparatedByOneSpace() {
        // Local Plus Codes are not implemented yet
        assertNull(input.match("28WR+CW foobar "))
        assertNull(input.match("28WR+CW foo bar "))
        assertNull(input.match("28WR+CW foo  bar"))
        assertNull(input.match("28WR+CW foo\tbar"))
    }

    @Test
    fun match_localPlusCodeWithLocalitySeparatedByMultipleSpaces() {
        // Local Plus Codes are not implemented yet
        assertNull(
            input.match(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "WF8Q+WF  Praia, Cabo Verde")
        )
        assertNull(input.match("28WR+CW  Comstock Park, Michigan"))
    }

    @Test
    fun parse_unknown() = runTest {
        assertEquals(ParseResult.Success(), input.parse("spam"))
    }

    @Test
    fun parse_globalPlusCode() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(WGS84Point(-1.289938, 36.820313, source = Source.HASH))
            ),
            input.parse("6GCRPR6C+24"),
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(WGS84Point(14.917313, -23.5113130, source = Source.HASH))
            ),
            input.parse("796RWF8Q+WF"),
        )
    }

    @Test
    fun parse_globalPlusCodeUriEncoded_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult.Success(),
            input.parse(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "8FJ3HVHW%2B96"),
        )
    }

    @Test
    fun parse_localPlusCodeWithoutLocality_returnsNoPoints() = runTest {
        assertEquals(ParseResult.Success(), input.parse("8F+GG"))
        assertEquals(ParseResult.Success(), input.parse("6C8F+GG"))
        assertEquals(ParseResult.Success(), input.parse("WF8Q+WF"))
        assertEquals(ParseResult.Success(), input.parse("28WR+CW"))
    }

    @Test
    fun parse_localPlusCodeWithLocality() = runTest {
        // Local Plus Codes are not implemented yet
        assertEquals(
            ParseResult.Success(),
            input.parse(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "WF8Q+WF Praia, Cabo Verde"),
        )
        assertEquals(
            ParseResult.Success(),
            input.parse("28WR+CW Comstock Park, Michigan"),
        )
    }
}
