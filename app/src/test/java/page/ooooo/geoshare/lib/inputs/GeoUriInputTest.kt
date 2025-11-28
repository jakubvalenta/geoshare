package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GeoUriInputTest : BaseInputTest() {
    override val input: Input = GeoUriInput

    @Test
    fun uriPattern_geoUri() {
        assertTrue(doesUriPatternMatch("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"))
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
            )
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() {
        assertEquals(
            Position() to null,
            parseUri("geo:"),
        )
        assertEquals(
            Position() to null,
            parseUri("geo:?spam=1"),
        )
    }

    @Test
    fun parseUri_coordsAndQAndZ() {
        assertEquals(
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4) to null,
            parseUri("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoords() {
        assertEquals(
            Position(Srs.WGS84, 50.123456, -11.123456) to null,
            parseUri("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
        )
    }

    @Test
    fun parseUri_coordsAndQCoordsDiffer_qCoordsTakePrecedence() {
        assertEquals(
            Position(Srs.WGS84, 40.7127400, -74.0059965) to null,
            parseUri("geo:50.123456,-11.123456?q=40.7127400,-74.0059965"),
        )
    }

    @Test
    fun parseUri_coordsAndName() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque") to null,
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?z=9.0&q=40.7127400,-74.0059965(Nova%20Iorque)"),
        )
    }

    @Test
    fun parseUri_coordsAndNameInSeparateQueryParam() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127400, -74.0059965, name = "Nova Iorque") to null,
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque") to null,
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&z=9.0&(Nova%20Iorque)"),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(Srs.WGS84, 40.7127400, -74.0059965, z = 9.0, name = "Nova Iorque") to null,
            @Suppress("SpellCheckingInspection")
            parseUri("geo:40.7127400,-74.0059965?q=40.7127400,-74.0059965&(Nova%20Iorque)&z=9.0"),
        )
    }

    @Test
    fun parseUri_qOnly() {
        assertEquals(
            Position(q = "foo bar") to null,
            parseUri("geo:?q=foo%20bar"),
        )
    }
}
