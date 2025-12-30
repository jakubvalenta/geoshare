package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class CoordinatesInputTest : BaseInputTest() {
    override val input = CoordinatesInput

    @Test
    fun uriPattern_supportedUrl() {
        assertTrue(doesUriPatternMatch("50.21972°\u00a0N, 0.68453°\u00a0W"))
        assertTrue(doesUriPatternMatch("31°57′N 35°56′E"))
    }

    @Test
    fun uriPattern_unknownPath() {
        assertFalse(doesUriPatternMatch("spam"))
    }

    @Test
    fun parseUri_unknownPath() = runTest {
        assertNull(parseUri(""))
        assertNull(parseUri("spam"))
    }

    @Test
    fun parseUri_decimal() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.40338, 2.17403)),
            parseUri("41.40338, 2.17403"),
        )
    }

    @Test
    fun parseUri_decimalDegreeSign() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.21972, -0.68453)),
            parseUri("50.21972°\u00a0N, 0.68453°\u00a0W"),
        )
    }

    @Test
    fun parseUri_decimalNorthEastAfter() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.996601, 6.122383)),
            parseUri("41.9966006N, 6.1223825E"),
        )
    }

    @Test
    fun parseUri_decimalNorthEastBefore() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.40338, 2.17403)),
            parseUri("N 41.40338, E 2.17403"),
        )
    }

    @Test
    fun parseUri_decimalNegativeNorthEastBefore() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -68.648556, -152.775879)),
            parseUri("N -68.648556 E -152.775879"),
        )
    }

    @Test
    fun parseUri_decimalSouthWestBefore() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -68.648556, -152.775879)),
            parseUri("S 68.648556 W 152.775879"),
        )
    }

    @Test
    fun parseUri_decimalSouthEastAfter() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -41.996601, -6.122383)),
            parseUri("41.9966006S, 6.1223825W"),
        )
    }

    @Test
    fun parseUri_degreesMinutesSecondsTypographic() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 31.0, 36.5)),
            parseUri("""31° 0′ 0″ N, 36° 30′ 0″ E"""),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 31.95, 35.933333)),
            parseUri("""31°57′N 35°56′E"""),
        )
    }

    @Test
    fun parseUri_degreesMinutesSecondsNorthEastAfter() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.403389, 2.174028)),
            parseUri("""41°24'12.2"N 2°10'26.5"E"""),
        )
    }

    @Test
    fun parseUri_degreesMinutesSecondsSouthWestAfter() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -68.648556, -152.775879)),
            parseUri("""68°38'54.8016S 152°46'33.1644W"""),
        )
    }

    @Test
    fun parseUri_degreesMinutesSecondsNegativeNorthEastBefore() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -68.648556, -152.775879)),
            parseUri("""N -68° 38' 54.8016 E -152° 46' 33.1644"""),
        )
    }

    @Test
    fun parseUri_degreesMinutes() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 41.40338, 2.17403)),
            parseUri("41 24.2028, 2 10.4418"),
        )
    }

    @Test
    fun parseUri_degreesMinutesWhole() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 31.95, 35.933333)),
            parseUri("31°57′N 35°56′E"),
        )
    }

    @Test
    fun parseUri_degreesMinutesNegative() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -68.648556, -152.775879)),
            parseUri("-68 38.913360, -152 46.552740"),
        )
    }
}
