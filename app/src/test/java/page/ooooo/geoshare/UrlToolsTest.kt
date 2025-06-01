package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.getUrlQueryParams
import java.net.URL

class UrlToolsTest {

    private val uriQuote = FakeUriQuote()

    @Test
    fun getUrlQueryParams_missingQuery_returnsEmptyMap() {
        assertEquals(
            emptyMap<String, String>(),
            getUrlQueryParams(URL("https://www.example.com/"), uriQuote),
        )
        assertEquals(
            emptyMap<String, String>(),
            getUrlQueryParams(URL("https://www.example.com/?"), uriQuote),
        )
    }

    @Test
    fun getUrlQueryParams_severalParameters_returnsMap() {
        assertEquals(
            mapOf<String, String>("foo" to "bar", "baz" to "1"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar&baz=1"), uriQuote),
        )
    }

    @Test
    fun getUrlQueryParams_urlEncodeParameter_returnsMapWithUrlDecodedValue() {
        assertEquals(
            mapOf<String, String>("foo" to "bar baz"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar%20baz"), uriQuote),
        )
    }

    @Test
    fun getUrlQueryParams_parameterWithoutValue_returnsMapWithEmptyStringsAsTheParameterValue() {
        assertEquals(
            mapOf<String, String>("foo" to "bar", "spam" to "", "baz" to "1"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar&spam&baz=1"), uriQuote),
        )
        assertEquals(
            mapOf<String, String>("foo" to "bar", "spam" to "", "baz" to "1"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar&spam=&baz=1"), uriQuote),
        )
    }

    @Test
    fun getUrlQueryParams_parameterWithoutNameOrValue_returnsMapWithEmptyStringsAsTheParameterNameAndValue() {
        assertEquals(
            mapOf<String, String>("foo" to "bar", "" to "", "baz" to "1"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar&=&baz=1"), uriQuote),
        )
        assertEquals(
            mapOf<String, String>("foo" to "bar", "" to "", "baz" to "1"),
            getUrlQueryParams(URL("https://www.example.com/?foo=bar&&baz=1"), uriQuote),
        )
    }
}
