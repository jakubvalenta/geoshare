package page.ooooo.geoshare.lib.inputs

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.util.AttributeKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.HttpClient
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException

class HeadLocationHeaderUriInputTest {
    val input = object : HeadLocationHeaderInput {
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
        if (request.method == HttpMethod.Head && request.url.toString() == "https://maps.google.com/foo") {
            respond("", HttpStatusCode.MovedPermanently, headers {
                append(HttpHeaders.Location, "https://maps.google.com/redirected")
            })
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
    fun whenMatchHasScheme_makesHeadRequestWithRedirectsFalseAndReturnsLocationHeader() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/redirected")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun whenMatchHasNoScheme_makesHeadRequestToUrlWithHttpsSchemeAndReturnsLocationHeader() = runTest {
        val match = "maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/redirected")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun whenHttpClientRespondsLocationHeaderAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/foo"
        val engine = MockEngine { request ->
            if (request.method == HttpMethod.Head && request.url.toString() == "https://maps.google.com/foo") {
                respond("", HttpStatusCode.MovedPermanently, headers {
                    append(HttpHeaders.Location, "redirected")
                })
            } else {
                respondError(HttpStatusCode.NotFound)
            }
        }
        val httpClient = HttpClient(engine, log)
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://maps.google.com/foo/redirected")),
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
