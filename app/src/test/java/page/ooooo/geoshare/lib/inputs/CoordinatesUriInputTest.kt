package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CoordinatesUriInputTest : InputTest {
    private val input = CoordinatesInput

    @Test
    fun match_coordinates() {
        assertEquals("50.21972°\u00a0N, 0.68453°\u00a0W", input.match("50.21972°\u00a0N, 0.68453°\u00a0W"))
        assertEquals("31°57′N 35°56′E", input.match("31°57′N 35°56′E"))
    }

    @Test
    fun match_unknown() {
        assertNull(input.match(""))
        assertNull(input.match("spam"))
    }

    @Test
    fun parse_decimal() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.40338, 2.17403, source = Source.TEXT))),
            input.parse("41.40338, 2.17403"),
        )
    }

    @Test
    fun parse_decimalDegreeSign() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.21972, -0.68453, source = Source.TEXT))),
            input.parse("50.21972°\u00a0N, 0.68453°\u00a0W"),
        )
    }

    @Test
    fun parse_decimalNorthEastAfter() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.996601, 6.122383, source = Source.TEXT))),
            input.parse("41.9966006N, 6.1223825E"),
        )
    }

    @Test
    fun parse_decimalNorthEastBefore() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.40338, 2.17403, source = Source.TEXT))),
            input.parse("N 41.40338, E 2.17403"),
        )
    }

    @Test
    fun parse_decimalNegativeNorthEastBefore() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-68.648556, -152.775879, source = Source.TEXT))),
            input.parse("N -68.648556 E -152.775879"),
        )
    }

    @Test
    fun parse_decimalSouthWestBefore() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-68.648556, -152.775879, source = Source.TEXT))),
            input.parse("S 68.648556 W 152.775879"),
        )
    }

    @Test
    fun parse_decimalSouthEastAfter() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-41.996601, -6.122383, source = Source.TEXT))),
            input.parse("41.9966006S, 6.1223825W"),
        )
    }

    @Test
    fun parse_degreesMinutesSecondsTypographic() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(31.0, 36.5, source = Source.TEXT))),
            input.parse("""31° 0′ 0″ N, 36° 30′ 0″ E"""),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(31.95, 35.933333, source = Source.TEXT))),
            input.parse("""31°57′N 35°56′E"""),
        )
    }

    @Test
    fun parse_degreesMinutesSecondsNorthEastAfter() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.403389, 2.174028, source = Source.TEXT))),
            input.parse("""41°24'12.2"N 2°10'26.5"E"""),
        )
    }

    @Test
    fun parse_degreesMinutesSecondsSouthWestAfter() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-68.648556, -152.775879, source = Source.TEXT))),
            input.parse("""68°38'54.8016S 152°46'33.1644W"""),
        )
    }

    @Test
    fun parse_degreesMinutesSecondsNegativeNorthEastBefore() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-68.648556, -152.775879, source = Source.TEXT))),
            input.parse("""N -68° 38' 54.8016 E -152° 46' 33.1644"""),
        )
    }

    @Test
    fun parse_degreesMinutes() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(41.40338, 2.17403, source = Source.TEXT))),
            input.parse("41 24.2028, 2 10.4418"),
        )
    }

    @Test
    fun parse_degreesMinutesWhole() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(31.95, 35.933333, source = Source.TEXT))),
            input.parse("31°57′N 35°56′E"),
        )
    }

    @Test
    fun parse_degreesMinutesNegative() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-68.648556, -152.775879, source = Source.TEXT))),
            input.parse("-68 38.913360, -152 46.552740"),
        )
    }
}
