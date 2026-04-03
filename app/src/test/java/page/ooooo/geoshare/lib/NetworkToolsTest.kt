package page.ooooo.geoshare.lib

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.AttributeKey
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.asSource
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.EOFException
import kotlinx.io.buffered
import kotlinx.io.readString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class NetworkToolsTest {
    @Test
    fun httpHeadLocationHeader_requestReturns301WithLocationHeader_callsConnectAndReturnsLocationHeader() = runTest {
        val url = URL("https://example.com/")
        val responseHeaders = headersOf(HttpHeaders.Location, "https://example.com/redirect")
        val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently, responseHeaders) }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.httpHeadLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        )
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            method = eq(HttpMethod.Head),
            expectedStatusCodes = eq(listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found)),
            followRedirects = eq(false),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun httpHeadLocationHeader_requestReturns301WithoutLocationHeader_callsConnectAndReturnsNull() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently) }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
        assertNull(mockNetworkTools.httpHeadLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler)))
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            method = eq(HttpMethod.Head),
            expectedStatusCodes = eq(listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found)),
            followRedirects = eq(false),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun httpGetBodyAsByteReadChannel_requestReturns200_callsConnectAndReturnsResponseBody() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
        val text = mockNetworkTools.httpGetBodyAsByteReadChannel(
            url,
            dispatcher = StandardTestDispatcher(testScheduler)
        ) { source ->
            source.asSource().buffered().readString()
        }
        assertEquals("test content", text)
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            method = eq(HttpMethod.Get),
            expectedStatusCodes = eq(listOf(HttpStatusCode.OK)),
            followRedirects = eq(true),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun httpGetRedirectUrlString_requestReturns200_callsConnectAndReturnsRequestUrl() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("") }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
        assertEquals(
            "https://example.com/",
            mockNetworkTools.httpGetRedirectedUrlString(url, dispatcher = StandardTestDispatcher(testScheduler)),
        )
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            method = eq(HttpMethod.Get),
            expectedStatusCodes = eq(listOf(HttpStatusCode.OK)),
            followRedirects = eq(true),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun connect_methodIsDefault_makesRequestWithMethodGet() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        assertEquals(HttpMethod.Get, lastRequest.method)
    }

    @Test
    fun connect_methodIsGet_makesRequestWithMethodGet() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                method = HttpMethod.Get,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        assertEquals(HttpMethod.Get, lastRequest.method)
    }

    @Test
    fun connect_methodIsHead_makesRequestWithMethodHead() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                method = HttpMethod.Head,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        assertEquals(HttpMethod.Head, lastRequest.method)
    }

    @Test
    fun connect_followRedirectsIsDefault_makesRequestWithFollowRedirectsTrue() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_followRedirectsIsTrue_makesRequestWithFollowRedirectsTrue() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirects = true,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_followRedirectsIsFalse_makesRequestWithFollowRedirectsFalse() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirects = false,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun connect_retryIsNull_doesNotWait() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun connect_retryIsZero_doesNotWait() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(R.string.network_exception_unknown, SocketTimeoutException())
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    retry = NetworkTools.Retry(0, tr),
                ) { response -> response.body<String>() },
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun connect_retryIsOne_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(R.string.network_exception_unknown, SocketTimeoutException())
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    retry = NetworkTools.Retry(1, tr),
                ) { response -> response.body<String>() }
            )
        }
        assertEquals(1.seconds, workDuration)
    }

    @Test
    fun connect_retryIsTwo_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(
            R.string.network_exception_unknown,
            SocketTimeoutException(),
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    retry = NetworkTools.Retry(2, tr),
                ) { response -> response.body<String>() }
            )
        }
        assertEquals(2.seconds, workDuration)
    }

    @Test
    fun connect_retryIsThree_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(R.string.network_exception_unknown, SocketTimeoutException())
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    retry = NetworkTools.Retry(3, tr),
                ) { response -> response.body<String>() },
            )
        }
        assertEquals(4.seconds, workDuration)
    }

    @Test
    fun connect_retryIsMaxRetries_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(R.string.network_exception_unknown, SocketTimeoutException())
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    retry = NetworkTools.Retry(9, tr),
                ) { response -> response.body<String>() },
            )
        }
        assertEquals(256.seconds, workDuration)
    }

    @Test
    fun connect_retryIsGreaterThanMaxRetries_doesNotWaitAndThrowsUnrecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val tr = RecoverableNetworkException(R.string.network_exception_unknown, SocketTimeoutException())
        val workDuration = testScheduler.timeSource.measureTime {
            var threw: Exception? = null
            try {
                assertEquals(
                    "test content",
                    mockNetworkTools.connect(
                        mockEngine,
                        url,
                        retry = NetworkTools.Retry(10, tr),
                    ) { response -> response.body<String>() },
                )
            } catch (tr: Exception) {
                threw = tr
            }
            assertTrue(threw is UnrecoverableNetworkException)
            assertEquals(
                tr.messageResId,
                (threw as? UnrecoverableNetworkException)?.messageResId,
            )
            assertEquals(
                tr.cause,
                (threw as? UnrecoverableNetworkException)?.cause,
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun connect_requestReturns301_returnsResponseIncludingLocationHeader() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            respond(
                content = "test content",
                status = HttpStatusCode.MovedPermanently,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://example.com/redirect"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
                followRedirects = false,
            ) { response -> response.headers[HttpHeaders.Location] }
        )
    }

    @Test
    fun connect_requestReturns302_returnsResponseIncludingLocationHeader() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            respond(
                content = "test content",
                status = HttpStatusCode.Found,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://example.com/redirect"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
                followRedirects = false,
            ) { response -> response.headers[HttpHeaders.Location] }
        )
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesIsNotPassed_throwsUnrecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertEquals(
            R.string.network_exception_response_error,
            (threw as? UnrecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is ResponseException)
        assertFalse(threw?.cause is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesDoesNotContainIt_throwsUnrecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.NoContent)
            ) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertEquals(
            R.string.network_exception_response_error,
            (threw as? UnrecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is ResponseException)
        assertFalse(threw?.cause is ServerResponseException)
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesContainsIt_returnsResponse() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.NotFound)
            ) { response -> response.body<String>() }
        )
    }

    @Test
    fun connect_requestReturns429AndExpectedStatusCodesIsDefault_throwsUnrecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.TooManyRequests) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertEquals(
            R.string.network_exception_too_many_requests,
            (threw as? UnrecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is ResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_requestReturns500AndExpectedStatusCodesIsDefault_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.InternalServerError) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_server_response_error,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_requestReturns500AndExpectedStatusCodesContainsIt_returnsResponse() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.InternalServerError) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.InternalServerError),
            ) { response -> response.body<String>() },
        )
    }

    @Test
    fun connect_requestThrowsUnresolvedAddressException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw UnresolvedAddressException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_unresolved_address,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun connect_requestThrowsHttpRequestTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request -> throw HttpRequestTimeoutException(request) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_request_timeout,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun connect_requestThrowsSocketTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw SocketTimeoutException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_socket_timeout,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun connect_requestThrowsConnectTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw ConnectTimeoutException("Connect timeout") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_connect_timeout,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun connect_requestThrowsEOFException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw EOFException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is RecoverableNetworkException)
        assertEquals(
            R.string.network_exception_eof,
            (threw as? RecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is EOFException)
    }

    @Test
    fun connect_requestThrowsUnknownException_throwsUnrecoverableException() = runTest {
        class MyException(message: String) : Exception(message)

        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw MyException("Unknown exception") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        assertTrue(threw is UnrecoverableNetworkException)
        assertEquals(
            R.string.network_exception_unknown,
            (threw as? UnrecoverableNetworkException)?.messageResId,
        )
        assertTrue(threw?.cause is MyException)
    }
}
