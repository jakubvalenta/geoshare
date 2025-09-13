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
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL

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
            URL("https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898"),
            mockNetworkTools.requestLocationHeader(url)
        )
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
            URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            mockNetworkTools.requestLocationHeader(url)
        )
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
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
        } catch (_: MalformedURLException) {
            threw = true
        }
        assertTrue(threw)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
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
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
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
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
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
    fun requestLocationHeader_invalidLocationUrl() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Found,
                headers = headersOf(
                    HttpHeaders.Location,
                    "spam"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
        } catch (_: MalformedURLException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
        } catch (_: HttpRequestTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
        } catch (_: ConnectTimeoutException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun requestLocationHeader_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        var threw = false
        try {
            mockNetworkTools.requestLocationHeader(url)
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
