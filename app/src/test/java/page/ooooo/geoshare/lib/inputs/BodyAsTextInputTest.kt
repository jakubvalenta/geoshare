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
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.NetworkException
import java.net.MalformedURLException

class BodyAsTextInputTest {
    val input = object : BodyAsTextInput {
        override val permissionTitleResId get() = throw NotImplementedError()
        override val loadingIndicatorTitleResId get() = throw NotImplementedError()

        override suspend fun parse(
            data: String,
            match: String,
            prevResult: ParseResult?,
            uriQuote: UriQuote,
            log: Log,
        ) = throw NotImplementedError()
    }
    private val log = FakeLog
    private val engine = MockEngine { request ->
        if (request.method == HttpMethod.Get && request.url.toString() == "https://maps.google.com/foo") {
            respond("test data")
        } else {
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
    fun whenMatchHasScheme_makesGetRequestWithFollowRedirectsAndReturnsResponse() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput(DebugWebViewInput()), "test data")),
            input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(
                        DebugUriInput(DebugWebViewInput()),
                        data
                    ) // Store data in nextStep, so we can test it
                )
            }
        )
        val lastRequest = engine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsResponse() = runTest {
        val match = "maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput(DebugWebViewInput()), "test data")),
            input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(
                        DebugUriInput(DebugWebViewInput()),
                        data
                    ) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test(expected = NetworkException::class)
    fun whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        input.withData(match, engine, log, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }
}
