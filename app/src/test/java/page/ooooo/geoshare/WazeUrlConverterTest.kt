package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.WazeUrlConverter

class WazeUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = WazeUrlConverter()

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(isSupportedUrl("ftp://waze.com/ul?ll=45.6906304,-120.810983&z=10"))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl("https://www.example.com/"))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl("https://waze.com/ul?ll=45.6906304,-120.810983&z=10"))
        assertTrue(isSupportedUrl("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983"))
        assertTrue(isSupportedUrl("https://ul.waze.com/ul?venue_id=183894452.1839010060.260192&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"))
    }

    @Test
    fun isSupportedUrl_shortUrl() {
        assertTrue(isSupportedUrl("https://waze.com/ul/hu00uswvn3"))
        assertTrue(isSupportedUrl("https://www.waze.com/ul/hu00uswvn3"))
        assertTrue(isSupportedUrl("https://www.waze.com/live-map?h=u00uswvn3"))
    }

    @Test
    fun isSupportedUrl_replacement() {
        assertEquals(
            "https://waze.com/ul/hu00uswvn3",
            getUri("Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3")
        )
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://waze.com"))
        assertNull(parseUrl("https://waze.com/"))
        assertNull(parseUrl("https://waze.com/ul"))
        assertNull(parseUrl("https://waze.com/ul/?spam=1"))
        assertNull(parseUrl("https://waze.com/live-map"))
        assertNull(parseUrl("https://waze.com/live-map/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("45.6906304", "-120.810983", z = "10"),
            parseUrl("https://waze.com/ul?ll=45.6906304,-120.810983&z=10")
        )
        assertEquals(
            Position("45.69063040", "-120.81098300"),
            parseUrl("https://ul.waze.com/ul?ll=45.69063040%2C-120.81098300&navigate=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
    }

    @Test
    fun parseUrl_directions() {
        assertEquals(
            Position("45.6906304", "-120.810983"),
            parseUrl("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position(),
            parseUrl("https://ul.waze.com/ul?venue_id=183894452.1839010060.260192&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
    }

    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(q = "66 Acacia Avenue"),
            parseUrl("https://waze.com/ul?q=66%20Acacia%20Avenue")
        )
    }

    @Test
    fun isShortUrl_correct() {
        assertTrue(isShortUrl("https://waze.com/ul/hu00uswvn3"))
        assertTrue(isShortUrl("https://www.waze.com/ul/hu00uswvn3"))
        assertTrue(isShortUrl("https://www.waze.com/live-map?h=u00uswvn3"))
    }

    @Test
    fun isShortUrl_replacement() {
        assertEquals(
            "https://www.waze.com/live-map?h=u00uswvn3",
            getShortUri("https://waze.com/ul/hu00uswvn3")
        )
        assertEquals(
            "https://www.waze.com/live-map?h=u00uswvn3",
            getShortUri("https://www.waze.com/live-map?h=u00uswvn3")
        )
    }

    @Test
    fun isShortUri_wrongPath() {
        assertFalse(isShortUrl("https://waze.com/"))
        assertFalse(isShortUrl("https://waze.com/foo"))
        assertFalse(isShortUrl("https://waze.com/ul"))
        assertFalse(isShortUrl("https://waze.com/ul/"))
        assertFalse(isShortUrl("https://waze.com/live-map"))
        assertFalse(isShortUrl("https://waze.com/live-map/"))
        assertFalse(isShortUrl("https://waze.com/live-map/?spam=1"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUrl("https://www.example.com/foo"))
    }
}
