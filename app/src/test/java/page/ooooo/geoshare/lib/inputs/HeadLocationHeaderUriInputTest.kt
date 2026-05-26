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
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException

class HeadLocationHeaderUriInputTest {
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            "https://maps.google.com/foo" if request.method == HttpMethod.Head ->
                respond("", HttpStatusCode.MovedPermanently, headers {
                    append(HttpHeaders.Location, "https://maps.google.com/redirected")
                })

            "https://maps.google.com/respond-relative" if request.method == HttpMethod.Head ->
                respond("", HttpStatusCode.MovedPermanently, headers {
                    append(HttpHeaders.Location, "redirected")
                })

            else ->
                respondError(HttpStatusCode.NotFound)
        }
    }
    val input = object : HeadLocationHeaderInput {
        override val engine = this@HeadLocationHeaderUriInputTest.engine
        override val log = FakeLog
        override val uriQuote = FakeUriQuote

        override val pattern get() = throw NotImplementedError()
        override val permissionTitleResId get() = throw NotImplementedError()
        override val loadingIndicatorTitleResId get() = throw NotImplementedError()

        override suspend fun parse(
            data: Uri,
            match: String,
        ) = throw NotImplementedError()
    }
    private val nextInput = FakeInputRepository.osmAndUriInput

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        input.fetch(match) {
            ParseResult()
        }
    }

    @Test
    fun whenMatchHasScheme_makesHeadRequestWithRedirectsFalseAndReturnsLocationHeader() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/redirected")),
            input.fetch(match) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
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
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/redirected")),
            input.fetch(match) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun whenHttpClientRespondsLocationHeaderAsRelativeUrl_returnsItAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/respond-relative"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, "https://maps.google.com/respond-relative/redirected")),
            input.fetch(match) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun whenHttpClientRespondsError_throwsNetworkException() = runTest {
        val match = "https://maps.google.com/not-found"
        input.fetch(match) {
            ParseResult()
        }
    }
}
