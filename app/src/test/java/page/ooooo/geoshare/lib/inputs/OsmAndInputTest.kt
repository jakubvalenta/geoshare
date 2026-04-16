package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OsmAndInputTest : InputTest {
    override val input = OsmAndInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://osmand.net/map?pin=52.51628,13.37771",
            getUri("https://osmand.net/map?pin=52.51628,13.37771")
        )
        assertEquals("osmand.net/map?pin=52.51628,13.37771", getUri("osmand.net/map?pin=52.51628,13.37771"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "osmand.net/map?pin=52.51628,13.37771",
            getUri("ftp://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_pin() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.51628, 13.37771, source = Source.URI))),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_directions() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(52.5, 13.5, source = Source.URI),
                    WGS84Point(52.51628, 13.37771, source = Source.URI),
                )
            ),
            parseUri("https://osmand.net/map?start=52.5,13.5&finish=52.51628,13.37771"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.5, 13.5, source = Source.URI))),
            parseUri("https://osmand.net/map?start=52.5,13.5"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.51628, 13.37771, source = Source.URI))),
            parseUri("https://osmand.net/map?finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_directionsTakesPrecedenceOverPin() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(52.5, 13.5, source = Source.URI),
                    WGS84Point(52.51628, 13.37771, source = Source.URI),
                )
            ),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771&start=52.5,13.5&finish=52.51628,13.37771"),
        )
    }

    @Test
    fun parseUri_fragment() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-53.347932, -13.2347, z = 12.5, source = Source.MAP_CENTER))),
            parseUri("https://osmand.net/map#12.5/-53.347932/-13.2347"),
        )
    }

    @Test
    fun parseUri_parameterPinTakesPrecedenceOverFragment() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.51628, 13.37771, z = 12.5, source = Source.URI))),
            parseUri("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347"),
        )
    }
}
