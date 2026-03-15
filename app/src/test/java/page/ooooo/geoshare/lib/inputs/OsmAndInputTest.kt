package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

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
        assertEquals(
            "osmand.net/map?pin=52.51628,13.37771",
            input.uriPattern.find("ftp://osmand.net/map?pin=52.51628,13.37771")?.value,
        )
    }

    @Test
    fun parseUri_pin() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.51628, 13.37771))),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_directions() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.5, 13.5), WGS84Point(52.51628, 13.37771))),
            parseUri("https://osmand.net/map?start=52.5,13.5&finish=52.51628,13.37771"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.5, 13.5))),
            parseUri("https://osmand.net/map?start=52.5,13.5"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.51628, 13.37771))),
            parseUri("https://osmand.net/map?finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_directionsTakesPrecedenceOverPin() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.5, 13.5), WGS84Point(52.51628, 13.37771))),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771&start=52.5,13.5&finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_fragment() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-53.347932, -13.2347, z = 12.5))),
            parseUri("https://osmand.net/map#12.5/-53.347932/-13.2347"),
        )
    }

    @Test
    fun parseUri_parameterPinTakesPrecedenceOverFragment() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.51628, 13.37771, z = 12.5))),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347"),
        )
    }
}
