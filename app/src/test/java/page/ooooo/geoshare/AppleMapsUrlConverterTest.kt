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
    fun isSupportedUrl_supportedUrl() {
        assertTrue(appleMapsUrlConverter.isSupportedUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")))
    }

    @Test
    fun parseUrl_coorinates() {
        assertEquals(
            "geo:50.894967,4.341626",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626"))
                .toString()
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            "geo:52.4890246,13.4295963",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"))
                .toString()
        )
    }

    @Test
    fun parseUrl_view() {
        assertEquals(
            "geo:52.49115540927951,13.42595574770533",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"))
                .toString()
        )
    }


    @Test
    fun parseUrl_search() {
        assertEquals(
            "geo:0,0?q=Central%20Park",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?q=Central+Park"))
                .toString()
        )
    }

    @Test
    fun parseUrl_searchLocation() {
        assertEquals(
            "geo:50.894967,4.341626?q=Central%20Park&z=10",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"))
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterLlTakesPrecedence() {
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"))
                .toString()
        )
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"))
                .toString()
        )
        assertEquals(
            "geo:-17.2165721,-149.9470294",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"))
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterAddressTakesPrecedence() {
        assertEquals(
            "geo:0,0?q=Reuterplatz%203%2C%2012047%20Berlin%2C%20Germany",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"))
                .toString()
        )
        assertEquals(
            "geo:0,0?q=Reuterplatz%203%2C%2012047%20Berlin%2C%20Germany",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz"))
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterNamesTakesPrecedenceOverQ() {
        assertEquals(
            "geo:0,0?q=Reuterplatz",
            appleMapsUrlConverter.parseUrl(URL("https://maps.apple.com/?name=Reuterplatz&q=Central%20Park"))
                .toString()
        )
    }

    @Test
    fun parseHtml_returnsNull() {
        assertNull(appleMapsUrlConverter.parseHtml("<html></html>"))
    }

    @Test
    fun isShortUrl_returnsFalse() {
        assertFalse(appleMapsUrlConverter.isShortUrl(URL("https://maps.apple.com/?ll=50.894967,4.341626")))
    }
}
