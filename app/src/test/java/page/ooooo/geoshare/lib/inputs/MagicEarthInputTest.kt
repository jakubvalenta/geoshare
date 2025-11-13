package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

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
        assertFalse(doesUriPatternMatch("ftp://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() {
        assertNull(parseUri("https://magicearth.com/"))
        assertNull(parseUri("https://magicearth.com/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() {
        assertEquals(
            Position(Srs.WGS84, 48.85649, 2.35216),
            parseUri("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345")
        )
    }

    @Test
    fun parseUri_place() {
        assertEquals(
            Position(q = "Central Park"),
            parseUri("https://magicearth.com/?name=Central Park")
        )
    }

    @Test
    fun parseUri_search() {
        assertEquals(
            Position(q = "Paris", z = 5.0),
            parseUri("https://magicearth.com/?q=Paris&mapmode=standard&z=5")
        )
    }

    @Test
    fun parseUri_destinationAddress() {
        assertEquals(
            Position(q = "CH1 6BJ United Kingdom"),
            parseUri("https://magicearth.com/?daddr=CH1+6BJ+United+Kingdom")
        )
    }

    @Test
    fun parseUri_parametersLatAndLonTakePrecedenceOverQ() {
        assertEquals(
            Position(Srs.WGS84, -17.2165721, -149.9470294),
            parseUri("https://magicearth.com/?lat=-17.2165721&lon=-149.9470294&q=Central Park")
        )
    }

    @Test
    fun parseUri_parameterDestinationAddressTakesPrecedenceOverQ() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUri("https://magicearth.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz")
        )
    }

    @Test
    fun parseUri_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "Reuterplatz"),
            parseUri("https://magicearth.com/?name=Reuterplatz&q=Central%20Park")
        )
    }

    @Test
    fun parseUri_customScheme() {
        assertEquals(
            Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4),
            parseUri("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4")
        )
    }
}
