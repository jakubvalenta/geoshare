package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import java.net.URL

class AppleMapsUrlConverterTest : BaseUrlConverterTest() {
    @Before
    fun before2() {
        urlConverter = AppleMapsUrlConverter()
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertTrue(isSupportedUrl(URL("ftp://maps.apple.com/?ll=50.894967,4.341626")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")))
    }

    @Test
    fun isSupportedUrl_shortUrl() {
        assertTrue(isSupportedUrl(URL("https://maps.apple/p/7E-Brjrk_THN14")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl(URL("https://maps.apple.com")))
        assertNull(parseUrl(URL("https://maps.apple.com/")))
        assertNull(parseUrl(URL("https://maps.apple.com/?spam=1")))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("50.894967", "4.341626"),
            parseUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626"))
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position("52.4890246", "13.4295963"),
            parseUrl(URL("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"))
        )
    }

    @Test
    fun parseUrl_view() {
        assertEquals(
            Position("52.49115540927951", "13.42595574770533"),
            parseUrl(URL("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"))
        )
    }


    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(null, null, q = "Central Park"),
            parseUrl(URL("https://maps.apple.com/?q=Central+Park"))
        )
    }

    @Test
    fun parseUrl_searchLocation() {
        assertEquals(
            Position("50.894967", "4.341626", q = "Central Park", z = "10"),
            parseUrl(URL("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"))
        )
    }

    @Test
    fun parseUrl_searchLocationWithInvalidZoom() {
        assertEquals(
            Position("50.894967", "4.341626", q = "Central Park"),
            parseUrl(URL("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s"))
        )
    }

    @Test
    fun parseUrl_parameterLlTakesPrecedence() {
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"))
        )
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"))
        )
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"))
        )
    }

    @Test
    fun parseUrl_parameterAddressTakesPrecedence() {
        assertEquals(
            Position(null, null, q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"))
        )
        assertEquals(
            Position(null, null, q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz"))
        )
    }

    @Test
    fun parseUrl_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            Position(null, null, q = "Reuterplatz"),
            parseUrl(URL("https://maps.apple.com/?name=Reuterplatz&q=Central%20Park"))
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUrl_AuidOnly() {
        assertEquals(
            Position(),
            parseUrl(URL("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"))
        )
    }

    @Test
    fun parseUrl_placeIdOnly() {
        assertEquals(
            Position(),
            parseUrl(URL("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"))
        )
    }

    @Test
    fun parseUrl_placeIdAndQuery() {
        assertEquals(
            Position(q = "Central Park"),
            parseUrl(URL("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"))
        )
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            Position(),
            parseUrl(URL("https://maps.apple/p/7E-Brjrk_THN14"))
        )
    }

    @Test
    fun parseHtml_success() {
        val html =
            this.javaClass.classLoader!!.getResource("I3B04EDEB21D5F86.html")!!
                .readText()
        assertEquals(
            Position("52.4735927", "13.4050798"),
            parseHtml(html)
        )
    }

    @Test
    fun parseHtml_failure() {
        assertNull(parseHtml("spam"))
    }

    @Test
    fun isShortUrl_alwaysReturnsFalse() {
        assertFalse(isShortUrl(URL("https://maps.apple/p/7E-Brjrk_THN14")))
    }
}
