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
import java.io.IOException
import java.net.URL
import kotlin.math.pow
import kotlin.math.roundToLong

class UnexpectedResponseCodeException : IOException("Unexpected response code")

class NetworkTools(
    private val engine: HttpClientEngine = CIO.create(),
    private val log: ILog = DefaultLog(),
) {
    @Throws(
        ConnectTimeoutException::class,
        HttpRequestTimeoutException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun requestLocationHeader(url: URL): String? = withContext(Dispatchers.IO) {
        connect(
            engine,
            url,
            httpMethod = HttpMethod.Head,
            followRedirectsParam = false,
            expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
        ) { response ->
            response.headers["Location"]
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

    @Throws(
        ConnectTimeoutException::class,
        HttpRequestTimeoutException::class,
        SocketTimeoutException::class,
        UnexpectedResponseCodeException::class,
    )
    suspend fun getRedirectUrlString(url: URL): String = withContext(Dispatchers.IO) {
        connect(
            engine,
            url,
        ) { response ->
            response.request.url.toString()
        }
    }

    private suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        httpMethod: HttpMethod = HttpMethod.Get,
        expectedStatusCodes: List<HttpStatusCode> = listOf(HttpStatusCode.OK),
        followRedirectsParam: Boolean = true,
        maxRetries: Int = 5,
        requestTimeoutMillisParam: Long = 30_000L,
        connectTimeoutMillisParam: Long = 15_000L,
        socketTimeoutMillisParam: Long = 15_000L,
        block: suspend (response: HttpResponse) -> T,
    ): T {
        HttpClient(engine) {
            followRedirects = followRedirectsParam
            install(HttpTimeout) {
                requestTimeoutMillis = requestTimeoutMillisParam
                connectTimeoutMillis = connectTimeoutMillisParam
                socketTimeoutMillis = socketTimeoutMillisParam
            }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries)
                exponentialDelay()
                modifyRequest { request ->
                    request.timeout {
                        val factor = 1.5.pow(retryCount - 1)
                        requestTimeoutMillis = (factor * requestTimeoutMillisParam).roundToLong()
                        connectTimeoutMillis = (factor * requestTimeoutMillisParam).roundToLong()
                        socketTimeoutMillis = (factor * requestTimeoutMillisParam).roundToLong()
                    }
                }
            }
            // Set custom User-Agent, so that we don't receive Google Lite HTML,
            // which doesn't contain coordinates in case of Google Maps or maps link
            // in case of Google Search.
            BrowserUserAgent()
        }.use { client ->
            try {
                val response = client.request(url) {
                    method = httpMethod
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
