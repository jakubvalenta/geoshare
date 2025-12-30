package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class OsmAndInputTest : BaseInputTest() {
    override val input = OsmAndInput

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://osmand.net/map?pin=52.51628,13.37771"))
        assertTrue(doesUriPatternMatch("osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun parseUri_pin() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 52.51628, 13.37771)),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_fragment() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -53.347932, -13.2347, z = 12.5)),
            parseUri("https://osmand.net/map#12.5/-53.347932/-13.2347"),
        )
    }

    @Test
    fun parseUri_parameterPinTakesPrecedenceOverFragment() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 52.51628, 13.37771, z = 12.5)),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347"),
        )
    }
}
