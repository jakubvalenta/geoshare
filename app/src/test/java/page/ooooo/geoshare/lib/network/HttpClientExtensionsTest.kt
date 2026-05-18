package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
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

class HttpClientExtensionsTest {
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
            val header = HttpClient(engine) {
                expectSuccess = true
            }.use { client ->
                client.headLocationHeader(url)
            }
            assertEquals(
                "https://maps.google.com/redirected",
                header,
            )
        }
    }

    @Test
    fun headLocationHeader_whenResponseIs3xxWithLocationHeaderAndExceptionsAreRethrownAsNetworkException_returnsLocationHeader() =
        runTest {
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
                val header = HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    client.headLocationHeader(url)
                }
                assertEquals(
                    "https://maps.google.com/redirected",
                    header,
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
            HttpClient(engine) {
                expectSuccess = true
            }.use { client ->
                client.headLocationHeader(url)
            }
        }
    }

    @Test(expected = MissingHeaderNetworkException::class)
    fun headLocationHeader_whenResponseIs3xxWithoutLocationHeaderAndExceptionsAreRethrownAsNetworkException_throwsMissingHeaderNetworkException() =
        runTest {
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
                HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    client.headLocationHeader(url)
                }
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
            val threw = HttpClient(engine) {
                expectSuccess = true
            }.use { client ->
                try {
                    client.headLocationHeader(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is ResponseException)
        }
    }

    @Test
    fun headLocationHeader_whenResponseIs4xxOr5xxAndExceptionsAreRethrownAsNetworkException_throwsException() =
        runTest {
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
                val threw = HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    try {
                        client.headLocationHeader(url)
                        null
                    } catch (tr: Exception) {
                        tr
                    }
                }
                assertTrue(threw is NetworkException)
            }
        }

    @OptIn(InternalAPI::class)
    @Test
    fun getLastHopUrlString_whenResponseIs2xx_returnsRequestUrl() = runTest {
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
            val urlString = HttpClient(engine) {
                expectSuccess = true
            }.use { client ->
                client.getLastHopUrlString(url)
            }
            assertEquals(
                "https://maps.google.com/redirected",
                urlString,
            )
        }
    }

    @Test
    fun rethrowExceptionsAsNetworkException_whenResponseIs2xx_returnsResponseIncludingLocationHeader() = runTest {
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
            val header = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                client
                    .get(url)
                    .headers[HttpHeaders.Location]
            }
            assertEquals(
                "https://maps.google.com/redirected",
                header,
            )
        }
    }

    @Test
    fun rethrowExceptionsAsNetworkException_whenResponseIs2xx_andFollowRedirectsIsFalse_returnsResponseIncludingLocationHeader() =
        runTest {
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
                val header = HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    client
                        .config { followRedirects = false }
                        .get(url)
                        .headers[HttpHeaders.Location]
                }
                assertEquals(
                    "https://maps.google.com/redirected",
                    header,
                )
                val lastRequest = engine.requestHistory.last()
                val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
                assertFalse(clientConfig.followRedirects)
            }
        }

    @Test
    fun rethrowExceptionsAsNetworkException_whenResponseIs3xxAndFollowRedirectsIsDefault_throwsUnrecoverableException() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            for (status in listOf(
                HttpStatusCode.MovedPermanently,
                HttpStatusCode.Found,
            )) {
                val engine = MockEngine { respond("test content", status) }
                val threw = HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    try {
                        client.get(url)
                        null
                    } catch (tr: Exception) {
                        tr
                    }
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
    fun rethrowExceptionsAsNetworkException_whenResponseIs3xxAndFollowRedirectsIsFalse_throwsUnrecoverableException() =
        runTest {
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
                val threw = HttpClient(engine) {
                    expectSuccess = true
                    rethrowExceptionsAsNetworkException(log)
                }.use { client ->
                    try {
                        client
                            .config { followRedirects = false }
                            .get(url)
                        null
                    } catch (tr: Exception) {
                        tr
                    }
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
    fun rethrowExceptionsAsNetworkException_whenResponseIs4xx_throwsUnrecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.BadRequest,
            HttpStatusCode.NotFound,
            HttpStatusCode.TooManyRequests,
        )) {
            val engine = MockEngine { respond("test content", status) }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is UnrecoverableNetworkException)
            assertTrue(threw is ResponseNetworkException)
            assertTrue(threw?.cause is ResponseException)
            assertFalse(threw?.cause is ServerResponseException)
        }
    }

    @Test
    fun rethrowExceptionsAsNetworkException_whenResponseIs5xx_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        for (status in listOf(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.BadGateway,
        )) {
            val engine = MockEngine { respond("test content", status) }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is ServerResponseNetworkException)
            assertTrue(threw?.cause is ServerResponseException)
        }
    }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsUnresolvedAddressException_throwsRecoverableException() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val engine = MockEngine { throw UnresolvedAddressException() }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is UnresolvedAddressNetworkException)
            assertTrue(threw?.cause is UnresolvedAddressException)
        }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsHttpRequestTimeoutException_throwsRecoverableException() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val engine = MockEngine { request -> throw HttpRequestTimeoutException(request) }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is RequestTimeoutNetworkException)
            assertTrue(threw?.cause is HttpRequestTimeoutException)
        }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsSocketTimeoutException_throwsRecoverableException() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val engine = MockEngine { throw SocketTimeoutException() }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is SocketTimeoutNetworkException)
            assertTrue(threw?.cause is SocketTimeoutException)
        }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsConnectTimeoutException_throwsRecoverableException() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val engine = MockEngine { throw ConnectTimeoutException("Connect timeout") }
            val threw = HttpClient(engine) {
                expectSuccess = true
                rethrowExceptionsAsNetworkException(log)
            }.use { client ->
                try {
                    client.get(url)
                    null
                } catch (tr: Exception) {
                    tr
                }
            }
            assertTrue(threw is RecoverableNetworkException)
            assertTrue(threw is ConnectTimeoutNetworkException)
            assertTrue(threw?.cause is ConnectTimeoutException)
        }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsEOFException_throwsRecoverableException() = runTest {
        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw kotlinx.io.EOFException() }
        val threw = HttpClient(engine) {
            expectSuccess = true
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            try {
                client.get(url)
                null
            } catch (tr: Exception) {
                tr
            }
        }
        assertTrue(threw is RecoverableNetworkException)
        assertTrue(threw is ConnectionClosedNetworkException)
        assertTrue(threw?.cause is EOFException)
    }

    @Test
    fun rethrowExceptionsAsNetworkException_whenRequestThrowsUnknownException_throwsUnrecoverableException() = runTest {
        class MyException(message: String) : Exception(message)

        val url = URL("https://maps.google.com/foo")
        val engine = MockEngine { throw MyException("Unknown exception") }
        val threw = HttpClient(engine) {
            expectSuccess = true
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            try {
                client.get(url)
                null
            } catch (tr: Exception) {
                tr
            }
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertTrue(threw is UnknownNetworkException)
        assertTrue(threw?.cause is MyException)
    }
}
