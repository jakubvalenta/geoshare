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
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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

    sealed class Result<T> {
        data class Success<U>(val value: U) : Result<U>()
        class RecoverableError<U>(val tr: Throwable) : Result<U>()
        class UnrecoverableError<U>(val tr: Throwable) : Result<U>()
    }

    data class Retry(val count: Int, val tr: Throwable)

    suspend fun requestLocationHeader(
        url: URL,
        retry: Retry? = null,
    ): Result<String?> = withContext(Dispatchers.IO) {
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

    suspend fun getText(url: URL, retry: Retry? = null): Result<String> = withContext(Dispatchers.IO) {
        connect(engine, url, retry = retry) { response ->
            response.body<String>()
        }
    }

    suspend fun getRedirectUrlString(url: URL, retry: Retry? = null): Result<String> = withContext(Dispatchers.IO) {
        connect(engine, url, retry = retry) { response ->
            response.request.url.toString()
        }
    }

    @Throws(
        ConnectTimeoutException::class,
        HttpRequestTimeoutException::class,
        ResponseException::class,
        SocketTimeoutException::class,
    )
    private suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        httpMethod: HttpMethod = HttpMethod.Get,
        expectedStatusCodes: List<HttpStatusCode> = listOf(HttpStatusCode.OK),
        followRedirectsParam: Boolean = true,
        retry: Retry? = null,
        block: suspend (response: HttpResponse) -> T,
    ): Result<T> {
        if (retry != null && retry.count > 0) {
            if (retry.count > MAX_RETRIES) {
                log.w(null, "Maximum number of $MAX_RETRIES retries reached for $url")
                return Result.UnrecoverableError(retry.tr)
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
                return Result.RecoverableError(tr)
            } catch (tr: HttpRequestTimeoutException) {
                log.w(null, "Request timeout for $url", tr)
                return Result.RecoverableError(tr)
            } catch (tr: SocketTimeoutException) {
                log.w(null, "Socket timeout for $url", tr)
                return Result.RecoverableError(tr)
            } catch (tr: ConnectTimeoutException) {
                log.w(null, "Connect timeout for $url", tr)
                return Result.RecoverableError(tr)
            } catch (tr: ServerResponseException) {
                log.w(null, "Server error ${tr.response.status} for $url", tr)
                return Result.RecoverableError(tr)
            } catch (tr: ResponseException) {
                log.w(null, "Unexpected response code ${tr.response.status} for $url", tr)
                return Result.UnrecoverableError(tr)
            } catch (tr: Exception) {
                log.e(null, "Unknown network exception for $url", tr)
                return Result.UnrecoverableError(tr)
            }
            return Result.Success(block(response))
        }
    }
}
