package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GeoInputTest : BaseInputTest() {
    override val input: Input = GeoInput

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
        assertNull(
            parseUri("geo:"),
        )
        assertNull(
            parseUri("geo:?spam=1"),
        )
    }

    @Test
    fun parseUri_returnsAllCoordsAndParams() {
        assertEquals(
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4),
            parseUri("geo:50.123456,-11.123456?q=foo%20bar&z=3.4"),
        )
    }

    @Test
    fun parseUri_returnsQOnly() {
        assertEquals(
            Position(q = "foo bar"),
            parseUri("geo:?q=foo%20bar"),
        )
    }
}
