package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before

import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.ParseHtmlResult
import page.ooooo.geoshare.lib.converters.ParseUrlResult
import java.net.URL

class AppleMapsUrlConverterTest {

    private lateinit var appleMapsUrlConverter: AppleMapsUrlConverter

    @Before
    fun before() {
        appleMapsUrlConverter = AppleMapsUrlConverter(FakeLog(), FakeUriQuote())
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(appleMapsUrlConverter.isSupportedUrl(URL("ftp://maps.apple.com/?ll=50.894967,4.341626")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(appleMapsUrlConverter.isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com")))
        assertNull(appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/")))
        assertNull(appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?spam=1")))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(appleMapsUrlConverter.isSupportedUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            "geo:50.894967,4.341626",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            "geo:52.4890246,13.4295963",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_view() {
        assertEquals(
            "geo:52.49115540927951,13.42595574770533",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }


    @Test
    fun parseUrl_search() {
        assertEquals(
            "geo:0,0?q=Central%20Park",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?q=Central+Park")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchLocation() {
        assertEquals(
            "geo:50.894967,4.341626?q=Central%20Park&z=10",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchLocationWithInvalidZoom() {
        assertEquals(
            "geo:50.894967,4.341626?q=Central%20Park",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterLlTakesPrecedence() {
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterAddressTakesPrecedence() {
        assertEquals(
            "geo:0,0?q=Reuterplatz%203%2C%2012047%20Berlin%2C%20Germany",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:0,0?q=Reuterplatz%203%2C%2012047%20Berlin%2C%20Germany",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterNamesTakesPrecedenceOverQ() {
        assertEquals(
            "geo:0,0?q=Reuterplatz",
            (appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?name=Reuterplatz&q=Central%20Park")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_AuidOnly() {
        assertTrue(
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618")) is ParseUrlResult.RequiresHtmlParsing
        )
    }

    @Test
    fun parseUrl_placeIdOnly() {
        assertTrue(
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902")) is ParseUrlResult.RequiresHtmlParsing
        )
    }

    @Test
    fun parseUrl_placeIdAndQuery() {
        assertTrue(
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park")) is ParseUrlResult.RequiresHtmlParsingToGetCoords
        )
    }

    @Test
    fun parseHtml_success() {
        val html =
            this.javaClass.classLoader!!.getResource("I3B04EDEB21D5F86.html")!!
                .readText()
        assertEquals(
            "geo:52.4735927,13.4050798",
            (appleMapsUrlConverter.parseHtml(html) as ParseHtmlResult.Parsed).geoUriBuilder.toString()
        )
    }

    @Test
    fun parseHtml_failure() {
        assertNull(appleMapsUrlConverter.parseHtml("spam"))
    }

    @Test
    fun isShortUrl_returnsFalse() {
        assertFalse(appleMapsUrlConverter.isShortUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")))
    }
}
