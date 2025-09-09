package page.ooooo.geoshare

import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter
import org.junit.Assert.*
import page.ooooo.geoshare.lib.Position
import java.net.URL

@Suppress("SpellCheckingInspection")
class MagicEarthUrlConverterTest : BaseUrlConverterTest() {
    @Before
    fun before2() {
        urlConverter = MagicEarthUrlConverter()
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertTrue(isSupportedUrl(URL("ftp://?drive_to&lat=48.85649&lon=2.35216.")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl(URL("magicearth://?drive_to&lat=48.85649&lon=2.35216")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl(URL("magicearth://")))
        assertNull(parseUrl(URL("magicearth://?spam=1")))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("48.85649", "2.35216"),
            parseUrl(URL("magicearth://?drive_to&lat=48.85649&lon=2.35216"))
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position(q = "Central Park"),
            parseUrl(URL("magicearth://?name=Central Park"))
        )
    }

    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(q = "Paris", z = "5"),
            parseUrl(URL("magicearth://?q=Paris&mapmode=standard&z=5"))
        )
    }

    @Test
    fun parseUrl_destinationAddress() {
        assertEquals(
            Position(q = "CH1 6BJ United Kingdom"),
            parseUrl(URL("magicearth://?daddr=CH1+6BJ+United+Kingdom"))
        )
    }

    @Test
    fun parseUrl_parametersLatAndLonTakePrecedenceOverQ() {
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl(URL("magicearth://?lat=-17.2165721&lon=-149.9470294&q=Central Park"))
        )
    }

    @Test
    fun parseUrl_parameterDestinationAddressTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl(URL("magicearth://?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"))
        )
    }

    @Test
    fun parseUrl_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            Position(q = "Reuterplatz"),
            parseUrl(URL("magicearth://?name=Reuterplatz&q=Central%20Park"))
        )
    }

    @Test
    fun parseHtml_alwaysReturnsNull() {
        assertNull(parseHtml("<html></html>"))
    }

    @Test
    fun isShortUrl_alwaysReturnsFalse() {
        assertFalse(isShortUrl(URL("magicearth://?drive_to&lat=48.85649&lon=2.35216")))
    }
}
