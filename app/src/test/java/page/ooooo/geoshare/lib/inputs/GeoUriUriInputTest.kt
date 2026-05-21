package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GeoUriUriInputTest : InputTest {
    private val input = FakeInputRepository.geoUriInput

    @Test
    fun match_geoUri() {
        assertEquals(
            "geo:50.123456,-11.123456?q=foo%20bar&z=3.4",
            input.match("geo:50.123456,-11.123456?q=foo%20bar&z=3.4")
        )
        assertEquals(
            "geo:52.47254,13.4345?q=52.47254,13.4345(My%20place)",
            input.match("geo:52.47254,13.4345?q=52.47254,13.4345(My%20place)")
        )
    }

    @Test
    fun match_geoUriWithSpaceInQ() = runTest {
        assertEquals("geo:0,0?q=45.4786785, 9.2473799", input.match("geo:0,0?q=45.4786785, 9.2473799"))
    }

    @Test
    fun match_noPath() {
        assertEquals("geo:?q=foo", input.match("geo:?q=foo"))
    }

    @Test
    fun match_noScheme() {
        assertNull(input.match("50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun match_nonGeoScheme() {
        assertNull(input.match("ftp:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun match_unknownPath() {
        assertEquals("geo:example?q=foo%20bar&z=3.4", input.match("geo:example?q=foo%20bar&z=3.4"))
    }

    @Test
    fun match_replacement() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)",
            input.match(
                @Suppress("SpellCheckingInspection")
                "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque"
            ),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "geo:1,2?q=foobar",
            input.match("geo:1,2?q=foobar ")
        )
        assertEquals(
            "geo:1,2?q=foo bar",
            input.match("geo:1,2?q=foo bar ")
        )
        assertEquals(
            "geo:1,2?q=foo",
            input.match("geo:1,2?q=foo  bar")
        )
        assertEquals(
            "geo:1,2?q=foo",
            input.match("geo:1,2?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("geo:"))
        assertEquals(ParseResult(), input.parse("geo:?spam=1"))
    }

    @Test
    fun parse_coordsAndQAndZ() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        50.123456, -11.123456,
                        name = "foo bar",
                        z = 3.4,
                        source = Source.URI,
                    )
                )
            ),
            input.parse("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parse_coordsAndQCoords() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.123456, -11.123456, source = Source.URI))),
            input.parse("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
        )
    }

    @Test
    fun parse_coordsAndQCoordsDiffer_qCoordsTakePrecedence() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(40.7127400, -74.0059965, source = Source.URI))),
            input.parse("geo:50.123456,-11.123456?q=40.7127400,-74.0059965"),
        )
    }

    @Test
    fun parse_coordsAndName() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)"),
        )
    }

    @Test
    fun parse_coordsAndNameInSeparateQueryParam() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&z=9.0&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)&z=9.0"),
        )
    }

    @Test
    fun parse_qOnly() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "foo bar", source = Source.URI))),
            input.parse("geo:?q=foo%20bar"),
        )
    }

    @Test
    fun parse_coordsInQWithSpace() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.4786785, 9.2473799, source = Source.URI))),
            input.parse("geo:0,0?q=45.4786785,%209.2473799"),
        )
    }
}
