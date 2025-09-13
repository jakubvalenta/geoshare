package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter

@Suppress("SpellCheckingInspection")
class MagicEarthUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = MagicEarthUrlConverter()

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(isSupportedUrl("ftp://?drive_to&lat=48.85649&lon=2.35216."))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl("https://www.example.com/"))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl("magicearth://?drive_to&lat=48.85649&lon=2.35216"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("magicearth://"))
        assertNull(parseUrl("magicearth://?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("48.85649", "2.35216"),
            parseUrl("magicearth://?drive_to&lat=48.85649&lon=2.35216")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position(q = "Central Park"),
            parseUrl("magicearth://?name=Central Park")
        )
    }

    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(q = "Paris", z = "5"),
            parseUrl("magicearth://?q=Paris&mapmode=standard&z=5")
        )
    }

    @Test
    fun parseUrl_destinationAddress() {
        assertEquals(
            Position(q = "CH1 6BJ United Kingdom"),
            parseUrl("magicearth://?daddr=CH1+6BJ+United+Kingdom")
        )
    }

    @Test
    fun parseUrl_parametersLatAndLonTakePrecedenceOverQ() {
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl("magicearth://?lat=-17.2165721&lon=-149.9470294&q=Central Park")
        )
    }

    @Test
    fun parseUrl_parameterDestinationAddressTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl("magicearth://?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz")
        )
    }

    @Test
    fun parseUrl_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz"),
            parseUrl("magicearth://?name=Reuterplatz&q=Central%20Park")
        )
    }
}
