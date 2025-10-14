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
            NetworkTools.Result.Success("https://maps.apple.com/place?address=Thomash%C3%B6he%2C+12053+Berlin%2C+Germany&coordinate=52.4737758%2C13.4373898"),
            mockNetworkTools.requestLocationHeader(url)
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
            NetworkTools.Result.Success("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            mockNetworkTools.requestLocationHeader(url)
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
        assertEquals(
            NetworkTools.Result.Success(null),
            mockNetworkTools.requestLocationHeader(url),
        )
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
        val res = mockNetworkTools.requestLocationHeader(url)
        assertTrue((res as NetworkTools.Result.UnrecoverableError).tr is ResponseException)
        assertFalse(res.tr is ServerResponseException)
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
        val res = mockNetworkTools.requestLocationHeader(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Head)
        assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun requestLocationHeader_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.requestLocationHeader(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is HttpRequestTimeoutException)
    }

    @Test
    fun requestLocationHeader_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.requestLocationHeader(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ConnectTimeoutException)
    }

    @Test
    fun requestLocationHeader_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.requestLocationHeader(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is SocketTimeoutException)
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
            NetworkTools.Result.Success("https://mapy.com/en/turisticka?source=base&id=1723771&x=14.4549515&y=50.0831498&z=17"),
            mockNetworkTools.getRedirectUrlString(url)
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
                    HttpHeaders.Location,
                    "https://mapy.com/s/jakuhelasu"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getRedirectUrlString(url)
        assertTrue(res is NetworkTools.Result.UnrecoverableError)
        assertTrue((res as NetworkTools.Result.UnrecoverableError).tr is ResponseException)
        assertFalse(res.tr is ServerResponseException)
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
                    HttpHeaders.Location,
                    "https://mapy.com/s/jakuhelasu"
                ),
            )
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getRedirectUrlString(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ServerResponseException)
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig = lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        assertEquals(lastRequest.method, HttpMethod.Get)
        assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun getRedirectUrlString_httpRequestTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException(request)
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getRedirectUrlString(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is HttpRequestTimeoutException)
    }

    @Test
    fun getRedirectUrlString_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getRedirectUrlString(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ConnectTimeoutException)
    }

    @Test
    fun getRedirectUrlString_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getRedirectUrlString(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is SocketTimeoutException)
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
            NetworkTools.Result.Success("test content"),
            mockNetworkTools.getText(url),
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
        val res = mockNetworkTools.getText(url)
        assertTrue(res is NetworkTools.Result.UnrecoverableError)
        assertTrue((res as NetworkTools.Result.UnrecoverableError).tr is ResponseException)
        assertFalse(res.tr is ServerResponseException)
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
        val res = mockNetworkTools.getText(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ServerResponseException)
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
        val res = mockNetworkTools.getText(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is HttpRequestTimeoutException)
    }

    @Test
    fun getText_connectTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { _ ->
            throw ConnectTimeoutException("Connect timeout")
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getText(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is ConnectTimeoutException)
    }

    @Test
    fun getText_socketTimeoutException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine {
            throw SocketTimeoutException()
        }
        val mockNetworkTools = NetworkTools(mockEngine, log)
        val res = mockNetworkTools.getText(url)
        assertTrue(res is NetworkTools.Result.RecoverableError)
        assertTrue((res as NetworkTools.Result.RecoverableError).tr is SocketTimeoutException)
    }
}
