package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondRedirect
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.util.AttributeKey
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.test.runTest
import kotlinx.io.EOFException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import java.net.SocketTimeoutException
import java.net.URL

class HttpClientTest {
    private val log = FakeLog

    @Test
    fun headLocationHeader_whenResponseIs3xxWithLocationHeader_returnsLocationHeader() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.MovedPermanently,
            HttpStatusCode.Found,
        )) {
            val engine = MockEngine { request ->
                if (request.method == HttpMethod.Head && request.url.toString() == "https://maps.google.com/foo") {
                    respond("", status, headers {
                        append(HttpHeaders.Location, "https://maps.google.com/redirected")
                    })
                } else {
                    throw NotImplementedError()
                }
            }
            val httpClient = HttpClient(engine, log)
            assertEquals(
                "https://maps.google.com/redirected",
                httpClient.headLocationHeader(url),
            )
        }
    }

    @Test(expected = MissingHeaderNetworkException::class)
    fun headLocationHeader_whenResponseIs3xxWithoutLocationHeader_throwsMissingHeaderNetworkException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.MovedPermanently,
            HttpStatusCode.Found,
        )) {
            val engine = MockEngine { request ->
                if (request.method == HttpMethod.Head && request.url.toString() == "https://maps.google.com/foo") {
                    respond("", status)
                } else {
                    throw NotImplementedError()
                }
            }
            val httpClient = HttpClient(engine, log)
            httpClient.headLocationHeader(url)
        }
    }

    @Test
    fun headLocationHeader_whenResponseIs4xxOr5xx_throwsException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.NotFound,
            HttpStatusCode.InternalServerError,
        )) {
            val engine = MockEngine { request ->
                if (request.method == HttpMethod.Head && request.url.toString() == "https://maps.google.com/foo") {
                    respond("", status)
                } else {
                    throw NotImplementedError()
                }
            }
            val httpClient = HttpClient(engine, log)
            var threw: Exception? = null
            try {
                httpClient.headLocationHeader(url)
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is NetworkException)
        }
    }

    @OptIn(InternalAPI::class)
    @Test
    fun getRedirectUrlString_whenResponseIs2xx_returnsRequestUrl() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.OK,
            HttpStatusCode.Created,
        )) {
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://maps.google.com/foo" if request.method == HttpMethod.Get ->
                        respondRedirect("https://maps.google.com/bar")

                    "https://maps.google.com/bar" if request.method == HttpMethod.Get ->
                        respondRedirect("https://maps.google.com/redirected")

                    "https://maps.google.com/redirected" if request.method == HttpMethod.Get ->
                        respond("", status)

                    else ->
                        respondError(HttpStatusCode.NotFound)
                }
            }
            val httpClient = HttpClient(engine, log)
            assertEquals(
                "https://maps.google.com/redirected",
                httpClient.getLastHopUrlString(url),
            )
        }
    }

    @Test
    fun httpClient_whenResponseIs2xx_returnsResponseIncludingLocationHeader() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.OK,
            HttpStatusCode.Created,
        )) {
            val engine = MockEngine {
                respond("test content", status, headers {
                    append(HttpHeaders.Location, "https://maps.google.com/redirected")
                })
            }
            val httpClient = HttpClient(engine, log)
            assertEquals(
                "https://maps.google.com/redirected",
                httpClient
                    .get(url)
                    .headers[HttpHeaders.Location]
            )
        }
    }

    @Test
    fun httpClient_whenResponseIs2xx_andFollowRedirectsIsFalse_returnsResponseIncludingLocationHeader() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.OK,
            HttpStatusCode.Created,
        )) {
            val engine = MockEngine {
                respond("test content", status, headers {
                    append(HttpHeaders.Location, "https://maps.google.com/redirected")
                })
            }
            val httpClient = HttpClient(engine, log)
            assertEquals(
                "https://maps.google.com/redirected",
                httpClient
                    .config { followRedirects = false }
                    .get(url)
                    .headers[HttpHeaders.Location],
            )
            val lastRequest = engine.requestHistory.last()
            val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
            assertFalse(clientConfig.followRedirects)
        }
    }

    @Test
    fun httpClient_whenResponseIs3xxAndFollowRedirectsIsDefault_throwsUnrecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.MovedPermanently,
            HttpStatusCode.Found,
        )) {
            val engine = MockEngine { respond("test content", status) }
            val httpClient = HttpClient(engine, log)
            var threw: Exception? = null
            try {
                httpClient.get(url)
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is UnrecoverableNetworkException)
            assertTrue(threw is ResponseNetworkException)
            assertTrue(threw?.cause is RedirectResponseException)
            val lastRequest = engine.requestHistory.last()
            val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
            assertTrue(clientConfig.followRedirects)
        }
    }

    @Test
    fun httpClient_whenResponseIs3xxAndFollowRedirectsIsFalse_throwsUnrecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.MovedPermanently,
            HttpStatusCode.Found,
        )) {
            val engine = MockEngine {
                respond("test content", status, headers {
                    append(HttpHeaders.Location, "https://maps.google.com/redirected")
                })
            }
            val httpClient = HttpClient(engine, log)
            var threw: Exception? = null
            try {
                httpClient
                    .config { followRedirects = false }
                    .get(url)
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is UnrecoverableNetworkException)
            assertTrue(threw is ResponseNetworkException)
            assertTrue(threw?.cause is RedirectResponseException)
            val lastRequest = engine.requestHistory.last()
            val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
            assertFalse(clientConfig.followRedirects)
        }
    }

    @Test
    fun httpClient_whenResponseIs4xx_throwsUnrecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.BadRequest,
            HttpStatusCode.NotFound,
            HttpStatusCode.TooManyRequests,
        )) {
            val engine = MockEngine { respond("test content", status) }
            val httpClient = HttpClient(engine, log)
            var threw: Exception? = null
            try {
                httpClient.get(url)
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is UnrecoverableNetworkException)
            assertTrue(threw is ResponseNetworkException)
            assertTrue(threw?.cause is ResponseException)
            assertFalse(threw?.cause is ServerResponseException)
        }
    }

    @Test
    fun httpClient_whenResponseIs5xx_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.BadGateway,
        )) {
            val engine = MockEngine { respond("test content", status) }
            val httpClient = HttpClient(engine, log)
            var threw: Exception? = null
            try {
                httpClient.get(url)
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is ServerResponseNetworkException)
            assertTrue(threw?.cause is ServerResponseException)
        }
    }

    @Test
    fun httpClient_whenRequestThrowsUnresolvedAddressException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw UnresolvedAddressException() }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is UnresolvedAddressNetworkException)
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun httpClient_whenRequestThrowsHttpRequestTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { request -> throw HttpRequestTimeoutException(request) }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is RequestTimeoutNetworkException)
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun httpClient_whenRequestThrowsSocketTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw SocketTimeoutException() }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is SocketTimeoutNetworkException)
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun httpClient_whenRequestThrowsConnectTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw ConnectTimeoutException("Connect timeout") }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is ConnectTimeoutNetworkException)
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun httpClient_whenRequestThrowsEOFException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw kotlinx.io.EOFException() }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is ConnectionClosedNetworkException)
        assertTrue(threw?.cause is EOFException)
    }

    @Test
    fun httpClient_whenRequestThrowsUnknownException_throwsUnrecoverableException() = runTest {
        class MyException(message: String) : Exception(message)

        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw MyException("Unknown exception") }
        val httpClient = HttpClient(engine, log)
        var threw: Exception? = null
        try {
            httpClient.get(url)
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertTrue(threw is UnknownNetworkException)
        assertTrue(threw?.cause is MyException)
    }
}
