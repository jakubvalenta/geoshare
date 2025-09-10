package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.getUrlQueryParams

class UrlToolsTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun getUrlQueryParams_missingQuery_returnsEmptyMap() {
        assertEquals(
            emptyMap<String, String>(),
            getUrlQueryParams("", uriQuote)
        )
    }

    @Test
    fun getUrlQueryParams_severalParameters_returnsMap() {
        assertEquals(
            mapOf("foo" to "bar", "baz" to "1"),
            getUrlQueryParams("foo=bar&baz=1", uriQuote)
        )
    }

    @Test
    fun getUrlQueryParams_urlEncodeParameter_returnsMapWithUrlDecodedValue() {
        assertEquals(
            mapOf("foo" to "bar baz"),
            getUrlQueryParams("foo=bar%20baz", uriQuote)
        )
    }

    @Test
    fun getUrlQueryParams_parameterWithoutValue_returnsMapWithEmptyStringsAsTheParameterValue() {
        assertEquals(
            mapOf("foo" to "bar", "spam" to "", "baz" to "1"),
            getUrlQueryParams("foo=bar&spam&baz=1", uriQuote)
        )
        assertEquals(
            mapOf("foo" to "bar", "spam" to "", "baz" to "1"),
            getUrlQueryParams("foo=bar&spam=&baz=1", uriQuote)
        )
    }

    @Test
    fun getUrlQueryParams_parameterWithoutNameOrValue_returnsMapWithEmptyStringsAsTheParameterNameAndValue() {
        assertEquals(
            mapOf("foo" to "bar", "" to "", "baz" to "1"),
            getUrlQueryParams("foo=bar&=&baz=1", uriQuote)
        )
        assertEquals(
            mapOf("foo" to "bar", "" to "", "baz" to "1"),
            getUrlQueryParams("foo=bar&&baz=1", uriQuote)
        )
    }
}
