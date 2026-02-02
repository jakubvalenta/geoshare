package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class GeoUriInputTest : BaseInputTest() {
    override val input: Input = GeoUriInput

    @Test
    fun uriPattern_geoUri() {
        assertTrue(doesUriPatternMatch("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_geoUriWithSpaceInQ() = runTest {
        assertTrue(doesUriPatternMatch("geo:0,0?q=45.4786785, 9.2473799"))
    }

    @Test
    fun uriPattern_noPath() {
        assertTrue(doesUriPatternMatch("geo:?q=foo"))
    }

    @Test
    fun uriPattern_noScheme() {
        assertFalse(doesUriPatternMatch("50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_nonGeoScheme() {
        assertFalse(doesUriPatternMatch("ftp:50.123456,-11.123456?q=foo%20bar&z=3.4"))
    }

    @Test
    fun uriPattern_unknownPath() {
        assertTrue(doesUriPatternMatch("geo:example?q=foo%20bar&z=3.4"))
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
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertTrue(parseUri("geo:") is ParseUriResult.Failed)
        assertTrue(parseUri("geo:?spam=1") is ParseUriResult.Failed)
    }

    @Test
    fun parseUri_coordsAndQAndZ() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4))),
            parseUri("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoords() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.123456, -11.123456))),
            parseUri("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoordsDiffer_qCoordsTakePrecedence() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(40.7127400, -74.0059965))),
            parseUri("geo:50.123456,-11.123456?q=40.7127400,-74.0059965"),
        )
    }

    @Test
    fun parseUri_coordsAndName() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(
                persistentListOf(
                    WGS84Point(
                        40.7127400,
                        -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque"
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
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(40.7127400, -74.0059965, name = "Nova Iorque"))),
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(
                persistentListOf(
                    WGS84Point(
                        40.7127400,
                        -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque"
                    )
                )
            ),
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&z=9.0&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(
                persistentListOf(
                    WGS84Point(
                        40.7127400,
                        -74.0059965,
                        z = 9.0,
                        name = "Nova Iorque"
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
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "foo bar"))),
            parseUri("geo:?q=foo%20bar"),
        )
    }

    @Test
    fun parseUri_coordsInQWithSpace() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(45.4786785, 9.2473799))),
            parseUri("geo:0,0?q=45.4786785,%209.2473799"),
        )
    }
}
