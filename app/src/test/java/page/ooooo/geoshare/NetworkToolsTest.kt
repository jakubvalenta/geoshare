package page.ooooo.geoshare

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.UnexpectedResponseCodeException
import java.net.SocketTimeoutException
import java.net.URL

class NetworkToolsTest {
    private val log = FakeLog()

    @Test
    fun requestLocationHeader_head_301Response() = runTest {
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
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_head_302Response() = runTest {
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
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_head_302ResponseMissingLocation() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Found,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertNull(mockNetworkTools.requestLocationHeader(url, HttpMethod.Head))
    }

    @Test
    fun requestLocationHeader_head_200Response() = runTest {
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
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_head_500Response() = runTest {
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
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_head_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        } catch (_: HttpRequestTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_head_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        } catch (_: ConnectTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_head_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Head)
        } catch (_: SocketTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_get_200Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://mapy.com/en/turisticka?source=base&id=1723771&x=14.4549515&y=50.0831498&z=17"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertEquals(
            "https://mapy.com/en/turisticka?source=base&id=1723771&x=14.4549515&y=50.0831498&z=17",
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Get)
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_get_200ResponseMissingLocation() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        assertNull(mockNetworkTools.requestLocationHeader(url, HttpMethod.Get))
    }

    @Test
    fun requestLocationHeader_get_500Response() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(
                    HttpHeaders.Location,
                    "https://mapy.com/s/jakuhelasu"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Get)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_get_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Get)
        } catch (_: HttpRequestTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_get_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Get)
        } catch (_: ConnectTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_get_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url, HttpMethod.Get)
        } catch (_: SocketTimeoutException) {
            threw = true
        }
        assertTrue(threw)
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
            mockNetworkTools.getText(url),
        )
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
        var threw = false
        try {
            mockNetworkTools.getText(url)
        } catch (_: UnexpectedResponseCodeException) {
            threw = true
        }
        assertTrue(threw)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getText_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.getText(url)
        } catch (_: HttpRequestTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun getText_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.getText(url)
        } catch (_: ConnectTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun getText_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.getText(url)
        } catch (_: SocketTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }
}
