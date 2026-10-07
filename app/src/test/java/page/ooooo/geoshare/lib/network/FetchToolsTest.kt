package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.respondRedirect
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.util.AttributeKey
import io.ktor.utils.io.readLine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.uri.FakeUriQuote
import java.net.MalformedURLException

class FetchToolsTest {
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            // Last hop

            "https://maps.google.com/hop-one" if request.method == HttpMethod.Get ->
                respondRedirect("https://maps.google.com/hop-two")

            "https://maps.google.com/hop-two" if request.method == HttpMethod.Get ->
                respondRedirect("https://maps.google.com/hop-final")

            "https://maps.google.com/hop-final" if request.method == HttpMethod.Get ->
                respondOk()

            // Location header

            "https://maps.google.com/redirects-to-absolute-url" if request.method == HttpMethod.Head ->
                respond("", HttpStatusCode.MovedPermanently, headers {
                    append(HttpHeaders.Location, "https://maps.google.com/redirects-to-absolute-url-location")
                })

            "https://maps.google.com/redirects-to-relative-url" if request.method == HttpMethod.Head ->
                respond("", HttpStatusCode.MovedPermanently, headers {
                    append(HttpHeaders.Location, "redirects-to-relative-url-location")
                })

            // Body

            "https://maps.google.com/body" if request.method == HttpMethod.Get ->
                respond("test data")

            // Default

            else ->
                respondError(HttpStatusCode.NotFound)
        }
    }
    private val fetchTools = DefaultFetchTools
    private val log = FakeLog
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun getLastHopUrl_whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        fetchTools.getLastHopUrl(match, engine, log, uriQuote)
    }

    @Test
    fun getLastHopUrl_whenMatchHasScheme_makesGetRequestWithFollowRedirectTrueAndReturnsRequestUrl() = runTest {
        val match = "https://maps.google.com/hop-one"
        assertEquals(
            "https://maps.google.com/hop-final",
            fetchTools.getLastHopUrl(match, engine, log, uriQuote).toString()
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getLastHopUrl_whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsRequestUrl() = runTest {
        val match = "maps.google.com/hop-one"
        assertEquals(
            "https://maps.google.com/hop-final",
            fetchTools.getLastHopUrl(match, engine, log, uriQuote).toString()
        )
    }

    @Test
    fun getLastHopUrl_whenHttpClientRespondsRequestUrlAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/hop-one"
        assertEquals(
            "https://maps.google.com/hop-final",
            fetchTools.getLastHopUrl(match, engine, log, uriQuote).toString()
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun getLastHopUrl_whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        fetchTools.getLastHopUrl(match, engine, log, uriQuote)
    }

    @Test(expected = MalformedURLException::class)
    fun headLocationHeader_whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        fetchTools.headLocationHeader(match, engine, log, uriQuote)
    }

    @Test
    fun headLocationHeader_whenMatchHasScheme_makesHeadRequestWithRedirectsFalseAndReturnsLocationHeader() = runTest {
        val match = "https://maps.google.com/redirects-to-absolute-url"
        assertEquals(
            "https://maps.google.com/redirects-to-absolute-url-location",
            fetchTools.headLocationHeader(match, engine, log, uriQuote).toString()
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun headLocationHeader_whenMatchHasNoScheme_makesHeadRequestToUrlWithHttpsSchemeAndReturnsLocationHeader() = runTest {
        val match = "maps.google.com/redirects-to-absolute-url"
        assertEquals(
            "https://maps.google.com/redirects-to-absolute-url-location",
            fetchTools.headLocationHeader(match, engine, log, uriQuote).toString()
        )
    }

    @Test
    fun headLocationHeader_whenHttpClientRespondsLocationHeaderAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/redirects-to-relative-url"
        assertEquals(
            "https://maps.google.com/redirects-to-relative-url/redirects-to-relative-url-location",
            fetchTools.headLocationHeader(match, engine, log, uriQuote).toString()
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun headLocationHeader_whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        fetchTools.headLocationHeader(match, engine, log, uriQuote)
    }

    @Test(expected = MalformedURLException::class)
    fun getBodyAsChannel_whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        fetchTools.getBodyAsChannel(match, engine, log, uriQuote) {}
    }

    @Test
    fun getBodyAsChannel_whenMatchHasScheme_makesGetRequestWithFollowRedirectsAndReturnsResponse() = runTest {
        val match = "https://maps.google.com/body"
        assertEquals(
            "test data",
            fetchTools.getBodyAsChannel(match, engine, log, uriQuote) { it.readLine()!! }
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getBodyAsChannel_whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsResponse() = runTest {
        val match = "maps.google.com/body"
        assertEquals(
            "test data",
            fetchTools.getBodyAsChannel(match, engine, log, uriQuote) { it.readLine()!! }
        )
    }

    @Test(expected = NetworkException::class)
    fun getBodyAsChannel_whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        fetchTools.getBodyAsChannel(match, engine, log, uriQuote) {}
    }

    @Test(expected = MalformedURLException::class)
    fun getBodyAsText_whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        fetchTools.getBodyAsText(match, engine, log, uriQuote) {}
    }

    @Test
    fun getBodyAsText_whenMatchHasScheme_makesGetRequestWithFollowRedirectsAndReturnsResponse() = runTest {
        val match = "https://maps.google.com/body"
        assertEquals(
            "test data",
            fetchTools.getBodyAsText(match, engine, log, uriQuote) { it }
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getBodyAsText_whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsResponse() = runTest {
        val match = "maps.google.com/body"
        assertEquals(
            "test data",
            fetchTools.getBodyAsText(match, engine, log, uriQuote) { it }
        )
    }

    @Test(expected = NetworkException::class)
    fun getBodyAsText_whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        fetchTools.getBodyAsText(match, engine, log, uriQuote) {}
    }
}
