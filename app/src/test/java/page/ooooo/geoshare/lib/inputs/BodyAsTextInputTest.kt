package page.ooooo.geoshare.lib.inputs

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.HttpClient
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
    private val httpClient = HttpClient(
        MockEngine { request ->
            if (request.method == HttpMethod.Get && request.url.equals("https://maps.google.com/foo")) {
                respond("test data")
            }
            throw NotImplementedError()
        },
        log = log,
    )
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }

    @Test
    fun whenMatchHasScheme_makesGetRequestAndReturnsResponse() = runTest {
        // TODO Test followRedirects
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "test data")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun whenMatchHasNoScheme_makesGetRequestToUrlWithHttpsSchemeAndReturnsResponse() = runTest {
        val match = "maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, "https://${match}-data")),
            input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test(expected = NetworkException::class)
    fun whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/foo"
        val httpClient = HttpClient(
            MockEngine {
                respondError(HttpStatusCode.NotFound)
            },
            log = log,
        )
        input.withData(match, log, httpClient, uriQuote, coroutineContext = testScheduler) {
            ParseResult()
        }
    }
}
