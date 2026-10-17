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

class PlusCodeUriInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.plusCodeUriInput

    @Test
    fun match_globalPlusCode() {
        assertEquals(
            "https://plus.codes/8FJ3HVHW%2B96",
            input.match("https://plus.codes/8FJ3HVHW%2B96"),
        )
        assertEquals(
            "https://plus.codes/8FJ3HVHW%2B96",
            input.match("https://plus.codes/8FJ3HVHW%2B96/"),
        )
        assertEquals(
            "https://plus.codes/8FJ3HVHW%2B96",
            input.match("https://plus.codes/8FJ3HVHW%2B96/spam"),
        )
    }

    @Test
    fun match_localPlusCode() {
        // Local Plus Codes are not implemented yet
        assertNull(input.match("https://plus.codes/HVHW%2B96"))
        assertNull(input.match("https://plus.codes/HVHW%2B96/"))
        assertNull(input.match("https://plus.codes/HVHW%2B96/spam"))
    }

    @Test
    fun match_localPlusCodeWithLocality() {
        // Local Plus Codes are not implemented yet
        assertNull(input.match("https://plus.codes/HVHW%2B96%20Angoustrine-Villeneuve-des-Escaldes,%20France"))
    }

    @Test
    fun match_localPlusCodeWithLocalitySeparatedByOneSpace() {
        // Local Plus Codes are not implemented yet
        assertNull(
            input.match(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "https://plus.codes/HVHW%2B96 Angoustrine-Villeneuve-des-Escaldes, France"
            ),
        )
    }

    @Test
    fun match_localPlusCodeWithLocalitySeparatedByMultipleSpaces() {
        // Local Plus Codes are not implemented yet
        assertNull(
            input.match(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "https://plus.codes/HVHW%2B96  Angoustrine-Villeneuve-des-Escaldes, France"
            )
        )
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
            input.parse("https://plus.codes/6GCRPR6C+24"),
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(WGS84Point(14.917313, -23.5113130, source = Source.HASH))
            ),
            input.parse("https://plus.codes/796RWF8Q+WF"),
        )
    }

    @Test
    fun parse_globalPlusCodeUriEncoded() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(WGS84Point(42.578438, 1.895563, source = Source.HASH))
            ),
            input.parse("https://plus.codes/8FJ3HVHW%2B96"),
        )
    }

    @Test
    fun parse_localPlusCodeWithoutLocality_returnsNoPoints() = runTest {
        assertEquals(ParseResult.Success(), input.parse("https://plus.codes/8F+GG"))
        assertEquals(ParseResult.Success(), input.parse("https://plus.codes/6C8F+GG"))
        assertEquals(ParseResult.Success(), input.parse("https://plus.codes/WF8Q+WF"))
        assertEquals(ParseResult.Success(), input.parse("https://plus.codes/28WR+CW"))
    }

    @Test
    fun parse_localPlusCodeWithLocality() = runTest {
        // Local Plus Codes are not implemented yet
        assertEquals(
            ParseResult.Success(),
            input.parse("https://plus.codes/WF8Q+WF%20Praia,%20Cabo%20Verde"),
        )
        assertEquals(
            ParseResult.Success(),
            input.parse("https://plus.codes/28WR+CW%20Comstock%20Park,%20Michigan"),
        )
    }
}
