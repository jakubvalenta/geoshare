package page.ooooo.geoshare

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.network.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.NetworkTools
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class NetworkToolsTest {
    private val log = FakeLog()

    @Test
    fun requestLocationHeader_301Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.MovedPermanently,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898",
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_302Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Found,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd",
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_302ResponseMissingLocation() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Found,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertNull(mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler)))
    }

    @Test
    fun requestLocationHeader_200Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.UnrecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertFalse(threw?.cause is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_500Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_unresolvedAddressException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw UnresolvedAddressException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun requestLocationHeader_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun requestLocationHeader_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun requestLocationHeader_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.requestLocationHeader(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun getRedirectUrlString_200Response() = runTest {
        val url = URL("https://mapy.com/en/turisticka?source=base&id=1723771&x=14.4549515&y=50.0831498&z=17")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://mapy.com/en/turisticka?source=base&id=1723771&x=14.4549515&y=50.0831498&z=17",
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getRedirectUrlString_404Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound,
                headers = headersOf(
                    HttpHeaders.Location, "https://mapy.com/s/jakuhelasu"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
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
    fun getRedirectUrlString_500Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(
                    HttpHeaders.Location, "https://mapy.com/s/jakuhelasu"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
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
    fun getRedirectUrlString_unresolvedAddressException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw UnresolvedAddressException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun getRedirectUrlString_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun getRedirectUrlString_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun getRedirectUrlString_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getRedirectUrlString(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun getText_200Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "test content",
                status = HttpStatusCode.OK,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "test content",
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler)),
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getText_404Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "test content",
                status = HttpStatusCode.NotFound,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
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
    fun getText_500Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "test content",
                status = HttpStatusCode.InternalServerError,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
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
    fun getText_unresolvedAddressException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw UnresolvedAddressException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun getText_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun getText_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun getText_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw: NetworkTools.NetworkException? = null
        try {
            mockNetworkTools.getText(url, dispatcher = StandardTestDispatcher(testScheduler))
        } catch (tr: NetworkTools.RecoverableException) {
            threw = tr
        }
        assertNotNull(threw)
        assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun getText_retryIsNull_doesNotWait() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                "test content",
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = null,
                ),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun getText_retryIsZero_doesNotWait() = runTest {
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
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = NetworkTools.Retry(0, tr),
                ),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun getText_retryIsOne_waits() = runTest {
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
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = NetworkTools.Retry(1, tr),
                ),
            )
        }
        assertEquals(1.seconds, workDuration)
    }

    @Test
    fun getText_retryIsTwo_waits() = runTest {
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
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = NetworkTools.Retry(2, tr),
                ),
            )
        }
        assertEquals(2.seconds, workDuration)
    }

    @Test
    fun getText_retryIsThree_waits() = runTest {
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
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = NetworkTools.Retry(3, tr),
                ),
            )
        }
        assertEquals(4.seconds, workDuration)
    }

    @Test
    fun getText_retryIsMaxRetries_waits() = runTest {
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
                mockNetworkTools.getText(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    retry = NetworkTools.Retry(9, tr),
                ),
            )
        }
        assertEquals(256.seconds, workDuration)
    }

    @Test
    fun getText_retryIsGreaterThanMaxRetries_doesNotWaitAndThrowsUnrecoverableException() = runTest {
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
                    mockNetworkTools.getText(
                        url,
                        dispatcher = StandardTestDispatcher(testScheduler),
                        retry = NetworkTools.Retry(10, tr),
                    ),
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
}
