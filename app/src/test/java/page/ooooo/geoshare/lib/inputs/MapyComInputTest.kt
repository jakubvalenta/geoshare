package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class MapyComInputTest : BaseInputTest() {
    override val input = MapyComInput

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://mapy.cz?x=14.0184810&y=50.0525078&z=9"))
        assertTrue(doesUriPatternMatch("https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9"))
        @Suppress("SpellCheckingInspection") assertTrue(doesUriPatternMatch("mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://mapy.com/s/jakuhelasu"))
        assertTrue(doesUriPatternMatch("https://mapy.cz/s/jakuhelasu"))
        @Suppress("SpellCheckingInspection") assertTrue(doesUriPatternMatch("mapy.com/s/jakuhelasu"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.uriPattern.find("ftp://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")?.value,
        )
    }

    @Test
    fun uriPattern_matchersCoordinatesInText() {
        assertEquals(
            "41.9966006N, 6.1223825W",
            @Suppress("SpellCheckingInspection") getUri(uriString = "Vega de Tera Calle Barrio de Abajo 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha"),
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertTrue(parseUri("https://mapy.com") is ParseUriResult.Failed)
        assertTrue(parseUri("https://mapy.com/en") is ParseUriResult.Failed)
        assertTrue(parseUri("https://mapy.com/en/") is ParseUriResult.Failed)
        assertTrue(parseUri("https://mapy.com/en/zakladni") is ParseUriResult.Failed)
        assertTrue(parseUri("https://mapy.com/en/zakladni?spam=1") is ParseUriResult.Failed)
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0))),
            parseUri("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesCsLanguage() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0))),
            parseUri("https://mapy.com/cs/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesCzDomain() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0))),
            parseUri("https://mapy.cz/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesOutdoorType() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0))),
            parseUri("https://mapy.com/en/turisticka?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesMissingType() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0))),
            parseUri("https://mapy.com/?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.0992553, 14.4336590, z = 19.0))),
            parseUri("https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19"),
        )
    }

    @Test
    fun parseUri_textCoordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(41.9966006, -6.1223825))),
            parseUri(uriString = "41.9966006N, 6.1223825W"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-41.9966006, 6.1223825))),
            parseUri(uriString = "41.9966006S, 6.1223825E"),
        )
    }

    @Test
    fun isShortUri_correct() {
        assertTrue(isShortUri("https://mapy.com/s/jakuhelasu"))
        assertTrue(isShortUri("https://www.mapy.com/s/jakuhelasu"))
        assertTrue(isShortUri("https://mapy.cz/s/jakuhelasu"))
    }

    @Test
    fun isShortUri_wrongPath() {
        assertFalse(isShortUri("https://mapy.com/"))
        assertFalse(isShortUri("https://mapy.com/s"))
        assertFalse(isShortUri("https://mapy.com/s/"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUri("https://www.example.com/foo"))
    }
}
