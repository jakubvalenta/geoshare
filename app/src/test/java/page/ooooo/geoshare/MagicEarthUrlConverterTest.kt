package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter

class MagicEarthUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = MagicEarthUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
        assertTrue(doesUriPatternMatch("magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345"))
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
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://magicearth.com/"))
        assertNull(parseUrl("https://magicearth.com/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("48.85649", "2.35216"),
            parseUrl("https://magicearth.com/?show_on_map&lat=48.85649&lon=2.35216&name=48.85649,+2.35216&img_id=12345")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position(q = "Central Park"),
            parseUrl("https://magicearth.com/?name=Central Park")
        )
    }

    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(q = "Paris", z = "5"),
            parseUrl("https://magicearth.com/?q=Paris&mapmode=standard&z=5")
        )
    }

    @Test
    fun parseUrl_destinationAddress() {
        assertEquals(
            Position(q = "CH1 6BJ United Kingdom"),
            parseUrl("https://magicearth.com/?daddr=CH1+6BJ+United+Kingdom")
        )
    }

    @Test
    fun parseUrl_parametersLatAndLonTakePrecedenceOverQ() {
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl("https://magicearth.com/?lat=-17.2165721&lon=-149.9470294&q=Central Park")
        )
    }

    @Test
    fun parseUrl_parameterDestinationAddressTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl("https://magicearth.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz")
        )
    }

    @Test
    fun parseUrl_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz"),
            parseUrl("https://magicearth.com/?name=Reuterplatz&q=Central%20Park")
        )
    }
}
