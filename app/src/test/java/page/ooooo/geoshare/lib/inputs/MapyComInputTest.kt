package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point

class MapyComInputTest : BaseInputTest() {
    override val input = MapyComInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            getUri("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            getUri("https://hapticke.mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://mapy.cz?x=14.0184810&y=50.0525078&z=9",
            getUri("https://mapy.cz?x=14.0184810&y=50.0525078&z=9")
        )
        assertEquals(
            "https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9",
            getUri("https://mapy.cz/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
        @Suppress("SpellCheckingInspection") assertEquals(
            "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            getUri("mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")
        )
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://mapy.com/s/jakuhelasu", getUri("https://mapy.com/s/jakuhelasu"))
        assertEquals("https://mapy.cz/s/jakuhelasu", getUri("https://mapy.cz/s/jakuhelasu"))
        @Suppress("SpellCheckingInspection") assertEquals("mapy.com/s/jakuhelasu", getUri("mapy.com/s/jakuhelasu"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            @Suppress("SpellCheckingInspection") "mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9",
            input.uriPattern.find("ftp://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9")?.value,
        )
    }

    @Test
    fun uriPattern_matchesCoordinatesInText() {
        assertEquals(
            "41.9966006N, 6.1223825W",
            @Suppress("SpellCheckingInspection") getUri(uriString = "Vega de Tera Calle Barrio de Abajo 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha"),
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://mapy.com"))
        assertEquals(ParseUriResult(), parseUri("https://mapy.com/en"))
        assertEquals(ParseUriResult(), parseUri("https://mapy.com/en/"))
        assertEquals(ParseUriResult(), parseUri("https://mapy.com/en/zakladni"))
        assertEquals(ParseUriResult(), parseUri("https://mapy.com/en/zakladni?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            parseUri("https://mapy.com/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesCsLanguage() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            parseUri("https://mapy.com/cs/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesCzDomain() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            parseUri("https://mapy.cz/en/zakladni?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesOutdoorType() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            parseUri("https://mapy.com/en/turisticka?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_coordinatesMissingType() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0525078, 14.0184810, z = 9.0, source = Source.URI))),
            parseUri("https://mapy.com/?x=14.0184810&y=50.0525078&z=9"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.0992553, 14.4336590, z = 19.0, source = Source.URI))),
            parseUri("https://mapy.com/en/zakladni?source=firm&id=13362491&x=14.4336590&y=50.0992553&z=19"),
        )
    }

    @Test
    fun parseUri_textCoordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(41.9966006, -6.1223825, source = Source.TEXT))),
            parseUri(uriString = "41.9966006N, 6.1223825W"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-41.9966006, 6.1223825, source = Source.TEXT))),
            parseUri(uriString = "41.9966006S, 6.1223825E"),
        )
    }

    @Test
    fun shortUriPattern_correct() {
        assertNotNull(getShortUri("https://mapy.com/s/jakuhelasu"))
        assertNotNull(getShortUri("https://www.mapy.com/s/jakuhelasu"))
        assertNotNull(getShortUri("https://mapy.cz/s/jakuhelasu"))
    }

    @Test
    fun shortUriPattern_wrongPath() {
        assertNull(getShortUri("https://mapy.com/"))
        assertNull(getShortUri("https://mapy.com/s"))
        assertNull(getShortUri("https://mapy.com/s/"))
    }

    @Test
    fun shortUriPattern_unknownDomain() {
        assertNull(getShortUri("https://www.example.com/foo"))
    }
}
