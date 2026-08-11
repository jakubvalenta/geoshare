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
    fun match_uri() {
        assertEquals(
            "geo:50.123456,-120.123456?q=foo%20bar&z=3.4",
            input.match("geo:50.123456,-120.123456?q=foo%20bar&z=3.4")
        )
        assertEquals(
            "geo:52.47254,13.4345?q=52.47254,13.4345(My%20place)",
            input.match("geo:52.47254,13.4345?q=52.47254,13.4345(My%20place)")
        )
    }

    @Test
    fun match_uriInText() {
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)",
            input.match(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque"
            ),
        )
        assertEquals(
            "geo:52.0553846,-2.7151898 into your maps app to see this location.",
            input.match(
                "Follow this link https://comaps.at/ItdwBgeWW1/Hereford or paste these coordinates " +
                    "geo:52.0553846,-2.7151898 into your maps app to see this location."
            ),
        )
    }

    @Test
    fun match_queryParamWithSpace() = runTest {
        assertEquals(
            "geo:0,0?q=45.4786785, 9.2473799",
            input.match("geo:0,0?q=45.4786785, 9.2473799"),
        )
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
    fun match_noPath() {
        assertEquals("geo:?q=foo", input.match("geo:?q=foo"))
    }

    @Test
    fun match_noScheme() {
        assertNull(input.match("50.123456,-120.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun match_unknownPath() {
        assertEquals("geo:example?q=foo%20bar&z=3.4", input.match("geo:example?q=foo%20bar&z=3.4"))
    }

    @Test
    fun match_unknownScheme() {
        assertNull(input.match("ftp:50.123456,-120.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("geo:"))
        assertEquals(ParseResult(), input.parse("geo:?spam=1"))
    }

    @Test
    fun parse_coordsAndQueryAndZoom() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        50.123456, -120.123456,
                        name = "foo bar",
                        z = 3.4,
                        source = Source.URI,
                    )
                )
            ),
            input.parse("geo:50.123456,-120.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parse_queryOnly() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "foo bar", source = Source.URI))),
            input.parse("geo:?q=foo%20bar"),
        )
    }

    @Test
    fun parse_pinWithoutName() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(40.7127400, -74.0059965, source = Source.URI))),
            input.parse("geo:50.123456,-120.123456?q=40.7127400,-74.0059965"),
        )
    }

    @Test
    fun parse_pinWithoutNameAndWithSpace() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.4786785, 9.2473799, source = Source.URI))),
            input.parse("geo:0,0?q=45.4786785,%209.2473799"),
        )
    }

    @Test
    fun parse_pinWithoutNameAndWithTrailingGarbage() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.0553846, -2.7151898, source = Source.URI))),
            input.parse("geo:52.0553846,-2.7151898 into your maps app to see this location."),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.0553846, -2.7151898, source = Source.URI))),
            input.parse("geo:0,0?q=52.0553846,-2.7151898 into your maps app to see this location."),
        )
    }

    @Test
    fun parse_pinWithName() = runTest {
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
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
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        25.0016, 121.50005,
                        name = "麥當勞 中和中心 McDonald's S99",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("geo:25.0016,121.50005?q=25.0016%2C121.50005%20(%E9%BA%A5%E7%95%B6%E5%8B%9E%20%E4%B8%AD%E5%92%8C%E4%B8%AD%E5%BF%83%20McDonald's%20S99)&mode=n")
        )
    }

    @Test
    fun parse_pinWithNameInSeparateQueryParam() = runTest {
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
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
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&z=9.0&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
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
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            input.parse("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)&z=9.0"),
        )
    }
}
