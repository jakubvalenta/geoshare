package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OpenStreetMapUriInputTest : InputTest {
    private val input = OpenStreetMapUriInput(OpenStreetMapApiInput())

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://www.openstreetmap.org/#map=16/51.49/-0.13",
            input.match("https://www.openstreetmap.org/#map=16/51.49/-0.13")
        )
        assertEquals(
            "www.openstreetmap.org/#map=16/51.49/-0.13",
            input.match("www.openstreetmap.org/#map=16/51.49/-0.13")
        )
        assertEquals("openstreetmap.org/#map=16/51.49/-0.13", input.match("openstreetmap.org/#map=16/51.49/-0.13"))
        assertEquals(
            "https://www.openstreetmap.org/directions?to=51.0528,13.7364",
            input.match("https://www.openstreetmap.org/directions?to=51.0528,13.7364")
        )
        assertEquals(
            "https://www.openstreetmap.org/node/6284640534",
            input.match("https://www.openstreetmap.org/node/6284640534")
        )
        assertEquals(
            "https://www.openstreetmap.org/relation/910699",
            input.match("https://www.openstreetmap.org/relation/910699")
        )
        assertEquals(
            "https://www.openstreetmap.org/way/596674456",
            input.match("https://www.openstreetmap.org/way/596674456")
        )
        assertEquals("https://osm.org/#map=16/51.49/-0.13", input.match("https://osm.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://osm.org/go/0EEQjE--", input.match("https://osm.org/go/0EEQjE--"))
        assertEquals("https://openstreetmap.org/go/0EEQjE--", input.match("https://openstreetmap.org/go/0EEQjE--"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/#map=16/51.49/-0.13"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "www.openstreetmap.org/#map=16/51.49/-0.13",
            input.match("ftp://www.openstreetmap.org/#map=16/51.49/-0.13"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://www.openstreetmap.org/?q=foobar",
            input.match("https://www.openstreetmap.org/?q=foobar ")
        )
        assertEquals(
            "https://www.openstreetmap.org/?q=foo bar",
            input.match("https://www.openstreetmap.org/?q=foo bar ")
        )
        assertEquals(
            "https://www.openstreetmap.org/?q=foo",
            input.match("https://www.openstreetmap.org/?q=foo  bar")
        )
        assertEquals(
            "https://www.openstreetmap.org/?q=foo",
            input.match("https://www.openstreetmap.org/?q=foo\tbar")
        )
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0, source = Source.URI))),
            input.parse("https://www.openstreetmap.org/?lat=51.49&lon=-0.13&zoom=16"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0, source = Source.URI))),
            input.parse("https://www.openstreetmap.org/?lat=51.49&lon=-0.13&z=16"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.49, -0.13, source = Source.URI))),
            input.parse("https://www.openstreetmap.org/?lat=51.49&lon=-0.13"),
        )
    }

    @Test
    fun parse_mapCenter() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0, source = Source.MAP_CENTER))),
            input.parse("https://www.openstreetmap.org/#map=16/51.49/-0.13"),
        )
    }

    @Test
    fun parse_coordinatesEncoded() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0, source = Source.MAP_CENTER))),
            input.parse("https://www.openstreetmap.org/#map%3D16%2F51.49%2F-0.13"),
        )
    }

    @Test
    fun parse_directions() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(51.0528, 13.7364, source = Source.URI))),
            input.parse("https://www.openstreetmap.org/directions?to=51.0528,13.7364"),
        )
    }

    @Test
    fun parse_element() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep.NextInput(
                    OpenStreetMapApiInput(),
                    "https://www.openstreetmap.org/api/0.6/node/6284640534.json"
                )
            ),
            input.parse("https://www.openstreetmap.org/node/6284640534"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep.NextInput(
                    OpenStreetMapApiInput(),
                    "https://www.openstreetmap.org/api/0.6/relation/910699/full.json"
                )
            ),
            input.parse("https://www.openstreetmap.org/relation/910699"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep.NextInput(
                    OpenStreetMapApiInput(),
                    "https://www.openstreetmap.org/api/0.6/way/596674456/full.json"
                )
            ),
            input.parse("https://www.openstreetmap.org/way/596674456"),
        )
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        51.510772705078125, 0.054931640625,
                        z = 9.0,
                        source = Source.HASH,
                    )
                )
            ),
            input.parse("https://osm.org/go/0EEQjE--"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        51.510772705078125, 0.054931640625,
                        z = 9.0,
                        source = Source.HASH,
                    )
                )
            ),
            input.parse("https://openstreetmap.org/go/0EEQjE--"),
        )
    }

    @Test
    fun parse_shortLinkNegative() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        -16.23152732849121, -49.08348083496094,
                        z = 11.0,
                        source = Source.HASH,
                    )
                )
            ),
            input.parse("https://osm.org/go/NuJWxJh-"),
        )
    }
}
