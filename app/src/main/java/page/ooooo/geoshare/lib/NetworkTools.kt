package page.ooooo.geoshare.lib

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URL

class UnexpectedResponseCodeException : Exception("Unexpected response code")

class NetworkTools(
    private val engine: HttpClientEngine = CIO.create(),
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) {
    @Throws(
        ConnectTimeoutException::class,
        HttpRequestTimeoutException::class,
        MalformedURLException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun requestLocationHeader(url: URL): URL = withContext(Dispatchers.IO) {
        connect(
            engine,
            url,
            methodParam = HttpMethod.Head,
            followRedirectsParam = false,
            expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
        ) { response ->
            val locationUrlString: String? = response.headers["Location"]
            val locationUrl = try {
                if (locationUrlString == null) {
                    throw MalformedURLException()
                }
                val locationUri = Uri.parse(locationUrlString, uriQuote)
                locationUri.toAbsoluteUrl(url.protocol, url.host, url.path)
            } catch (e: MalformedURLException) {
                log.w(null, "Invalid location URL $locationUrlString")
                throw e
            }
            log.i(null, "Resolved short URL $url to $locationUrlString")
            locationUrl
        }
    }

    @Throws(
        ConnectTimeoutException::class,
        HttpRequestTimeoutException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun getText(url: URL): String = withContext(Dispatchers.IO) {
        connect(engine, url) { response ->
            val text: String = response.body()
            text
        }
    }

    private suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        methodParam: HttpMethod = HttpMethod.Get,
        expectedStatusCodes: List<HttpStatusCode> = listOf(HttpStatusCode.OK),
        followRedirectsParam: Boolean = true,
        requestTimeoutMillisParam: Long = 45_000L,
        block: suspend (response: HttpResponse) -> T,
    ): T {
        HttpClient(engine) {
            followRedirects = followRedirectsParam
            // Set custom User-Agent, so that we don't receive Google Lite HTML,
            // which doesn't contain coordinates in case of Google Maps or maps link
            // in case of Google Search.
            BrowserUserAgent()
        }.use { client ->
            try {
                val response = client.request(url) {
                    method = methodParam
                    timeout {
                        requestTimeoutMillis = requestTimeoutMillisParam
                    }
                }
                if (expectedStatusCodes.contains(response.status)) {
                    return block(response)
                }
                log.w(null, "Received HTTP code ${response.status} for $url")
                throw UnexpectedResponseCodeException()
            } catch (e: HttpRequestTimeoutException) {
                log.w(null, "HTTP request timeout for $url")
                throw e
            } catch (e: SocketTimeoutException) {
                log.w(null, "Socket timeout for $url")
                throw e
            } catch (e: ConnectTimeoutException) {
                log.w(null, "Connect timeout for $url")
                throw e
            }
        }
    }
}
