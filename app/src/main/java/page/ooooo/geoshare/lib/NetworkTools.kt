package page.ooooo.geoshare.lib

import android.net.Network
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
import io.ktor.util.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import java.net.URL
import kotlin.math.pow

class NetworkTools(
    private val engine: HttpClientEngine = CIO.create(),
    private val log: ILog = DefaultLog(),
) {
    companion object {
        const val MAX_RETRIES = 9
        const val EXPONENTIAL_DELAY = 1_000L
        const val REQUEST_TIMEOUT = 256_000L
        const val CONNECT_TIMEOUT = 128_000L
        const val SOCKET_TIMEOUT = 128_000L
    }

    abstract class NetworkException(val messageResId: Int, override val cause: Throwable) : Exception(cause)

    class RecoverableException(messageResId: Int, cause: Throwable) : NetworkException(messageResId, cause)

    class UnrecoverableException(messageResId: Int, cause: Throwable) : NetworkException(messageResId, cause)

    data class Retry(val count: Int, val tr: NetworkException)

    @Throws(NetworkException::class)
    suspend fun requestLocationHeader(
        url: URL,
        retry: Retry? = null,
    ): String? = withContext(Dispatchers.IO) {
        connect(
            engine,
            url,
            httpMethod = HttpMethod.Head,
            expectedStatusCodes = listOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found),
            followRedirectsParam = false,
            retry = retry,
        ) { response ->
            response.headers["Location"]
        }
    }

    @Throws(NetworkException::class)
    suspend fun getText(url: URL, retry: Retry? = null): String = withContext(Dispatchers.IO) {
        connect(engine, url, retry = retry) { response ->
            response.body<String>()
        }
    }

    @Throws(NetworkException::class)
    suspend fun getRedirectUrlString(url: URL, retry: Retry? = null): String = withContext(Dispatchers.IO) {
        connect(engine, url, retry = retry) { response ->
            response.request.url.toString()
        }
    }

    @Throws(NetworkException::class)
    private suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        httpMethod: HttpMethod = HttpMethod.Get,
        expectedStatusCodes: List<HttpStatusCode> = listOf(HttpStatusCode.OK),
        followRedirectsParam: Boolean = true,
        retry: Retry? = null,
        block: suspend (response: HttpResponse) -> T,
    ): T {
        if (retry != null && retry.count > 0) {
            if (retry.count > MAX_RETRIES) {
                log.w(null, "Maximum number of $MAX_RETRIES retries reached for $url")
                throw UnrecoverableException(retry.tr.messageResId, retry.tr.cause)
            }
            val timeMillis = (2.0.pow(retry.count - 1) * EXPONENTIAL_DELAY).toLong()
            log.i(null, "Waiting ${timeMillis}ms before retry $retry.count of $MAX_RETRIES for $url")
            delay(timeMillis)
        }
        HttpClient(engine) {
            followRedirects = followRedirectsParam
            this.expectSuccess
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status.value >= 500) {
                        throw ServerResponseException(response, "<not implemented>")
                    }
                    if (response.status !in expectedStatusCodes) {
                        throw ResponseException(response, "<not implemented>")
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = CONNECT_TIMEOUT
                socketTimeoutMillis = SOCKET_TIMEOUT
            }
            // Set custom User-Agent, so that we don't receive Google Lite HTML,
            // which doesn't contain coordinates in case of Google Maps or maps link
            // in case of Google Search.
            BrowserUserAgent()
        }.use { client ->
            val response = try {
                client.request(url) {
                    method = httpMethod
                }
            } catch (tr: UnresolvedAddressException) {
                // TODO Test
                log.w(null, "Unresolved address for $url", tr)
                throw RecoverableException(R.string.network_exception_unresolved_address, tr)
            } catch (tr: HttpRequestTimeoutException) {
                log.w(null, "Request timeout for $url", tr)
                throw RecoverableException(R.string.network_exception_request_timeout, tr)
            } catch (tr: SocketTimeoutException) {
                log.w(null, "Socket timeout for $url", tr)
                throw RecoverableException(R.string.network_exception_socket_timeout, tr)
            } catch (tr: ConnectTimeoutException) {
                log.w(null, "Connect timeout for $url", tr)
                throw RecoverableException(R.string.network_exception_connect_timeout, tr)
            } catch (tr: ServerResponseException) {
                log.w(null, "Server error ${tr.response.status} for $url", tr)
                throw RecoverableException(R.string.network_exception_server_response_error, tr)
            } catch (tr: ResponseException) {
                log.w(null, "Unexpected response code ${tr.response.status} for $url", tr)
                throw UnrecoverableException(R.string.network_exception_response_error, tr)
            } catch (tr: Exception) {
                log.e(null, "Unknown network exception for $url", tr)
                throw UnrecoverableException(R.string.network_exception_unknown, tr)
            }
            return block(response)
        }
    }
}
