package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class MagicEarthInputTest : BaseInputTest() {
    override val input = MagicEarthInput

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
        assertTrue(doesUriPatternMatch("magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
        assertTrue(doesUriPatternMatch("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345",
            input.uriPattern.find("ftp://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345")?.value,
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertTrue(parseUri("https://magicearth.com/") is ParseUriResult.Failed)
        assertTrue(parseUri("https://magicearth.com/?spam=1") is ParseUriResult.Failed)
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(48.85649, 2.35216))),
            parseUri("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Central Park"))),
            parseUri("https://magicearth.com/?name=Central Park"),
        )
    }

    @Test
    fun parseUri_search() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Paris", z = 5.0))),
            parseUri("https://magicearth.com/?q=Paris&mapmode=standard&z=5"),
        )
    }

    @Test
    fun parseUri_destinationAddress() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "CH1 6BJ United Kingdom"))),
            parseUri("https://magicearth.com/?daddr=CH1+6BJ+United+Kingdom"),
        )
    }

    @Test
    fun parseUri_parametersLatAndLonTakePrecedenceOverQ() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-17.2165721, -149.9470294, name = "Central Park"))),
            parseUri("https://magicearth.com/?lat=-17.2165721&lon=-149.9470294&q=Central Park"),
        )
    }

    @Test
    fun parseUri_parameterDestinationAddressTakesPrecedenceOverQ() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz 3, 12047 Berlin, Germany"))),
            parseUri("https://magicearth.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Test
    fun parseUri_parameterNameTakesPrecedenceOverQ() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection") ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz"))),
            parseUri("https://magicearth.com/?name=Reuterplatz&q=Central%20Park"),
        )
    }

    @Test
    fun parseUri_customScheme() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4, name = "foo bar"))),
            parseUri("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4"),
        )
    }
}
