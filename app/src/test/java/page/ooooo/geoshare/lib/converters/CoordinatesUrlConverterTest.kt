package page.ooooo.geoshare.lib.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.Position

class CoordinatesUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = CoordinatesUrlConverter()

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
    fun parseUrl_unknownPath() {
        assertNull(parseUrl(""))
        assertNull(parseUrl("spam"))
    }

    @Test
    fun parseUrl_decimal() {
        assertEquals(
            Position(41.40338, 2.17403),
            parseUrl("41.40338, 2.17403")
        )
    }

    @Test
    fun parseUrl_decimalDegreeSign() {
        assertEquals(
            Position(50.21972, -0.68453),
            parseUrl("50.21972°\u00a0N, 0.68453°\u00a0W")
        )
    }

    @Test
    fun parseUrl_decimalNorthEastAfter() {
        assertEquals(
            Position(41.996601, 6.122383),
            parseUrl("41.9966006N, 6.1223825E")
        )
    }

    @Test
    fun parseUrl_decimalNorthEastBefore() {
        assertEquals(
            Position(41.40338, 2.17403),
            parseUrl("N 41.40338, E 2.17403")
        )
    }

    @Test
    fun parseUrl_decimalNegativeNorthEastBefore() {
        assertEquals(
            Position(-68.648556, -152.775879),
            parseUrl("N -68.648556 E -152.775879")
        )
    }

    @Test
    fun parseUrl_decimalSouthWestBefore() {
        assertEquals(
            Position(-68.648556, -152.775879),
            parseUrl("S 68.648556 W 152.775879")
        )
    }

    @Test
    fun parseUrl_decimalSouthEastAfter() {
        assertEquals(
            Position(-41.996601, -6.122383),
            parseUrl("41.9966006S, 6.1223825W")
        )
    }

    @Test
    fun parseUrl_degreesMinutesSecondsTypographic() {
        assertEquals(
            Position(31.0, 36.5),
            parseUrl("""31° 0′ 0″ N, 36° 30′ 0″ E""")
        )
        assertEquals(
            Position(31.95, 35.933333),
            parseUrl("""31°57′N 35°56′E""")
        )
    }

    @Test
    fun parseUrl_degreesMinutesSecondsNorthEastAfter() {
        assertEquals(
            Position(41.403389, 2.174028),
            parseUrl("""41°24'12.2"N 2°10'26.5"E""")
        )
    }

    @Test
    fun parseUrl_degreesMinutesSecondsSouthWestAfter() {
        assertEquals(
            Position(-68.648556, -152.775879),
            parseUrl("""68°38'54.8016S 152°46'33.1644W""")
        )
    }

    @Test
    fun parseUrl_degreesMinutesSecondsNegativeNorthEastBefore() {
        assertEquals(
            Position(-68.648556, -152.775879),
            parseUrl("""N -68° 38' 54.8016 E -152° 46' 33.1644""")
        )
    }

    @Test
    fun parseUrl_degreesMinutes() {
        assertEquals(
            Position(41.40338, 2.17403),
            parseUrl("41 24.2028, 2 10.4418")
        )
    }

    @Test
    fun parseUrl_degreesMinutesWhole() {
        assertEquals(
            Position(31.95, 35.933333),
            parseUrl("31°57′N 35°56′E")
        )
    }

    @Test
    fun parseUrl_degreesMinutesNegative() {
        assertEquals(
            Position(-68.648556, -152.775879),
            parseUrl("-68 38.913360, -152 46.552740")
        )
    }
}
