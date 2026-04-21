package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GeoUriInputTest : InputTest {
    override val input: Input = GeoUriInput

    @Test
    fun uriPattern_geoUri() {
        assertEquals("geo:50.123456,-11.123456?q=foo%20bar&z=3.4", getUri("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_geoUriWithSpaceInQ() = runTest {
        assertEquals("geo:0,0?q=45.4786785, 9.2473799", getUri("geo:0,0?q=45.4786785, 9.2473799"))
    }

    @Test
    fun uriPattern_noPath() {
        assertEquals("geo:?q=foo", getUri("geo:?q=foo"))
    }

    @Test
    fun uriPattern_noScheme() {
        assertNull(getUri("50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_nonGeoScheme() {
        assertNull(getUri("ftp:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_unknownPath() {
        assertEquals("geo:example?q=foo%20bar&z=3.4", getUri("geo:example?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_replacement() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)",
            getUri(
                @Suppress("SpellCheckingInspection")
                "geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)\n" +
                    "https://omaps.app/Umse5f0H8a/Nova_Iorque"
            ),
        )
    }

    @Test
    fun uriPattern_spaces() {
        assertEquals(
            "https://maps.apple.com/?q=foobar",
            getUri("https://maps.apple.com/?q=foobar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo bar",
            getUri("https://maps.apple.com/?q=foo bar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            getUri("https://maps.apple.com/?q=foo  bar")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            getUri("https://maps.apple.com/?q=foo\tbar")
        )
    }

    @Test
    fun parseUri_unknownPathOrParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("geo:"))
        assertEquals(ParseUriResult(), parseUri("geo:?spam=1"))
    }

    @Test
    fun parseUri_coordsAndQAndZ() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        50.123456, -11.123456,
                        name = "foo bar",
                        z = 3.4,
                        source = Source.URI,
                    )
                )
            ),
            parseUri("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoords() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.123456, -11.123456, source = Source.URI))),
            parseUri("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoordsDiffer_qCoordsTakePrecedence() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(40.7127400, -74.0059965, source = Source.URI))),
            parseUri("geo:50.123456,-11.123456?q=40.7127400,-74.0059965"),
        )
    }

    @Test
    fun parseUri_coordsAndName() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
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
            parseUri("geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)"),
        )
    }

    @Test
    fun parseUri_coordsAndNameInSeparateQueryParam() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        40.7127400, -74.0059965,
                        name = "Nova Iorque",
                        source = Source.URI,
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
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
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&z=9.0&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
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
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)&z=9.0"),
        )
    }

    @Test
    fun parseUri_qOnly() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "foo bar", source = Source.URI))),
            parseUri("geo:?q=foo%20bar"),
        )
    }

    @Test
    fun parseUri_coordsInQWithSpace() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.4786785, 9.2473799, source = Source.URI))),
            parseUri("geo:0,0?q=45.4786785,%209.2473799"),
        )
    }
}
