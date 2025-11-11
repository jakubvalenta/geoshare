package page.ooooo.geoshare.lib

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.network.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.readString
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import page.ooooo.geoshare.R
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class NetworkToolsTest {
    private val log = FakeLog()

    @Test
    fun requestLocationHeader_requestReturns301WithLocationHeader_callsConnectAndReturnsLocationHeader() = runTest {
        val url = URL("https://example.com/")
        val responseHeaders = headersOf(HttpHeaders.Location, "https://example.com/redirect")
        val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently, responseHeaders) }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log))
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        )
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            httpMethod = eq(HttpMethod.Head),
            expectedStatusCodes = eq(listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found)),
            followRedirectsParam = eq(false),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun requestLocationHeader_requestReturns301WithoutLocationHeader_callsConnectAndReturnsNull() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently) }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log))
        assertNull(mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler)))
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            httpMethod = eq(HttpMethod.Head),
            expectedStatusCodes = eq(listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found)),
            followRedirectsParam = eq(false),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun getRedirectUrlString_requestReturns200_callsConnectAndReturnsRequestUrl() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("") }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log))
        assertEquals(
            "https://example.com/",
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler)),
        )
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            httpMethod = eq(HttpMethod.Get),
            expectedStatusCodes = eq(listOf(HttpStatusCode.OK)),
            followRedirectsParam = eq(true),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun getSource_requestReturns200_callsConnectAndReturnsResponseBody() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log))
        mockNetworkTools.getSource(url, dispatcher = StandardTestDispatcher(testScheduler)).use { source ->
            assertEquals(
                "test content",
                source.readString(),
            )
        }
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            httpMethod = eq(HttpMethod.Get),
            expectedStatusCodes = eq(listOf(HttpStatusCode.OK)),
            followRedirectsParam = eq(true),
            retry = eq(null),
            block = any(),
        )
    }

    @Test
    fun getSource_200Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "test content",
                status = HttpStatusCode.OK,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        mockNetworkTools.getSource(url, dispatcher = StandardTestDispatcher(testScheduler)).use { source ->
            assertEquals(
                "test content",
                source.readString(),
            )
        }
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_methodIsDefault_makesRequestWithMethodGet() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                httpMethod = HttpMethod.Get,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        assertEquals(HttpMethod.Get, lastRequest.method)
    }

    @Test
    fun connect_methodIsHead_makesRequestWithMethodHead() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                httpMethod = HttpMethod.Head,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        assertEquals(HttpMethod.Head, lastRequest.method)
    }

    @Test
    fun connect_followRedirectsIsDefault_makesRequestWithFollowRedirectsTrue() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirectsParam = true,
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirectsParam = false,
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
            SocketTimeoutException(),
        )
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
            SocketTimeoutException(),
        )
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
            SocketTimeoutException(),
        )
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
            SocketTimeoutException(),
        )
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val tr = NetworkTools.RecoverableException(
            R.string.conversion_failed_parse_html_connection_error,
            SocketTimeoutException(),
        )
        val workDuration = testScheduler.timeSource.measureTime {
            var threw: NetworkTools.NetworkException? = null
            try {
                assertEquals(
                    "test content",
                    mockNetworkTools.connect(
                        mockEngine,
                        url,
                        retry = NetworkTools.Retry(10, tr),
                    ) { response -> response.body<String>() },
                )
            } catch (tr: NetworkTools.UnrecoverableException) {
                threw = tr
            }
            assertNotNull(threw)
            assertEquals(tr.messageResId, threw?.messageResId)
            assertEquals(tr.cause, threw?.cause)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
                followRedirectsParam = false,
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://example.com/redirect",
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
                followRedirectsParam = false,
            ) { response -> response.headers[HttpHeaders.Location] }
        )
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesIsNotPassed_throwsResponseException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.UnrecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ResponseException)
        assertFalse(threw?.cause is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesDoesNotContainIt_throwsResponseException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(
                mockEngine,
                url,
                expectedStatusCodes = listOf(HttpStatusCode.NoContent)
            ) { response -> response.body<String>() }
        } catch (tr: NetworkTools.UnrecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ResponseException)
        assertFalse(threw?.cause is ServerResponseException)
    }

    @Test
    fun connect_requestReturns404AndExpectedStatusCodesContainsIt_returnsResponse() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.NotFound) }
        val mockNetworkTools = NetworkTools(mockEngine, log)
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
    fun connect_requestReturns500AndExpectedStatusCodesIsDefault_throwsServerResponseException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content", HttpStatusCode.InternalServerError) }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
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
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertEquals(R.string.network_exception_unresolved_address, threw?.messageResId)
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun connect_requestThrowsHttpRequestTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request -> throw HttpRequestTimeoutException(request) }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertEquals(R.string.network_exception_request_timeout, threw?.messageResId)
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun connect_requestThrowsSocketTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw SocketTimeoutException() }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertEquals(R.string.network_exception_socket_timeout, threw?.messageResId)
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun connect_requestThrowsConnectTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw ConnectTimeoutException("Connect timeout") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertEquals(R.string.network_exception_connect_timeout, threw?.messageResId)
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun connect_requestThrowsUnknownException_throwsUnrecoverableException() = runTest {
        class MyException(message: String) : Exception(message)

        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw MyException("Unknown exception") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: NetworkTools.UnrecoverableException) {
            threw = tr
        }
        assertEquals(R.string.network_exception_unknown, threw?.messageResId)
        assertTrue(threw?.cause is MyException)
    }
}
