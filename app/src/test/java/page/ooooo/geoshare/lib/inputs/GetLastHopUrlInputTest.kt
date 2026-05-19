package page.ooooo.geoshare.lib.inputs

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.respondRedirect
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException

class GetLastHopUrlInputTest {
    val input = object : GetLastHopUrlInput {
        override val pattern get() = throw NotImplementedError()
        override val permissionTitleResId get() = throw NotImplementedError()
        override val loadingIndicatorTitleResId get() = throw NotImplementedError()

        override suspend fun parse(
            data: Uri,
            match: String,
            prevResult: ParseResult?,
            uriQuote: UriQuote,
            log: Log,
        ) = throw NotImplementedError()
    }
    private val nextInput = OsmAndUriInput()
    private val log = FakeLog
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            "https://maps.google.com/foo" if request.method == HttpMethod.Get ->
                respondRedirect("https://maps.google.com/bar")

            "https://maps.google.com/bar" if request.method == HttpMethod.Get ->
                respondRedirect("https://maps.google.com/redirected")

            "https://maps.google.com/redirected" if request.method == HttpMethod.Get ->
                respondOk()

            else ->
                respondError(HttpStatusCode.NotFound)
        }
    }
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }

    @Test
    fun whenMatchHasScheme_makesGetRequestWithFollowRedirectTrueAndReturnsRequestUrl() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/redirected")),
            input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsRequestUrl() = runTest {
        val match = "maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/redirected")),
            input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun whenHttpClientRespondsRequestUrlAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/redirected")),
            input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }
}
