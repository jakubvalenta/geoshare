package page.ooooo.geoshare.lib.inputs

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
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
import page.ooooo.geoshare.lib.network.HttpClient
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException

class GetRedirectUrlInputTest {
    val input = object : GetRedirectUrlInput {
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
    private val log = FakeLog
    private val engine = MockEngine { request ->
        // TODO Test request URL after redirects
        if (request.method == HttpMethod.Get && request.url.toString() == "https://maps.google.com/foo") {
            respond("")
        } else {
            respondError(HttpStatusCode.NotFound)
        }
    }
    private val httpClient = HttpClient(engine, log)
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }

    @Test
    fun whenMatchHasScheme_makesGetRequestWithFollowRedirectTrueAndReturnsRequestUrl() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/foo")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
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
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/foo")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun whenHttpClientRespondsRequestUrlAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/foo")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }
}
