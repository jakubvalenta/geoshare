package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.EOFException
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import java.net.URL
import kotlin.math.pow
import kotlin.math.roundToLong

open class NetworkTools(
    private val engine: HttpClientEngine = CIO.create(),
    private val log: ILog = DefaultLog,
) {
    data class Retry(val count: Int, val tr: NetworkException)

    /**
     * Make a HEAD request to [url] and return the value of the response Location header.
     *
     * The network request is executed on [dispatcher].
     */
    @Throws(NetworkException::class)
    open suspend fun httpHeadLocationHeader(
        url: URL,
        retry: Retry? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String? = withContext(dispatcher) {
        connect(
            engine,
            url,
            method = HttpMethod.Head,
            expectedStatusCodes = listOf(
                HttpStatusCode.MovedPermanently,
                HttpStatusCode.Found,
            ),
            followRedirects = false,
            retry = retry,
        ) { response ->
            response.headers[HttpHeaders.Location]
        }
    }

    /**
     * Make a GET request to [url] and invoke [block] with the resulting [ByteReadChannel].
     *
     * The network request as well as the [block] are executed on [dispatcher]. When the [block] finishes, the channel
     * is closed.
     */
    @Throws(NetworkException::class)
    open suspend fun <T> httpGetBodyAsByteReadChannel(
        url: URL,
        retry: Retry? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend (channel: ByteReadChannel) -> T,
    ): T = withContext(dispatcher) {
        connect(engine, url, retry = retry) { response ->
            val channel: ByteReadChannel = response.body()
            block(channel)
        }
    }

    /**
     * Make a GET request to [url], follow all redirects, and return the final URL that the request redirected to.
     *
     * The network request is executed on [dispatcher].
     */
    @Throws(NetworkException::class)
    open suspend fun httpGetRedirectedUrlString(
        url: URL,
        retry: Retry? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String = withContext(dispatcher) {
        connect(engine, url, retry = retry) { response ->
            response.request.url.toString()
        }
    }

    /**
     * Make an HTTP request to [url] using [method] and invoke [block] with the [HttpResponse].
     *
     * Throw [NetworkException] if there is a connection error or if the HTTP response code is not in
     * [expectedStatusCodes].
     *
     * This is an internal method that is exposed only due to unit tests. You should normally use high-level methods
     * such as [httpGetBodyAsByteReadChannel] instead.
     */
    @Throws(NetworkException::class)
    open suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        method: HttpMethod = HttpMethod.Get,
        expectedStatusCodes: List<HttpStatusCode> = listOf(HttpStatusCode.OK),
        followRedirects: Boolean = true,
        retry: Retry? = null,
        block: suspend (response: HttpResponse) -> T,
    ): T {
        if (retry != null && retry.count > 0) {
            if (retry.count > MAX_RETRIES) {
                log.w(null, "Maximum number of $MAX_RETRIES retries reached for $url")
                throw UnrecoverableNetworkException(retry.tr.messageResId, retry.tr.cause)
            }
            val timeMillis = (EXPONENTIAL_DELAY_BASE.pow(retry.count - 1) * EXPONENTIAL_DELAY_BASE_DELAY).roundToLong()
            log.i(null, "Waiting ${timeMillis}ms before retry ${retry.count} of $MAX_RETRIES for $url")
            delay(timeMillis)
        }
        return HttpClient(engine) {
            this.followRedirects = followRedirects
            // Bypass consent page https://stackoverflow.com/a/78115353
            install(HttpCookies) {
                storage = ConstantCookiesStorage(
                    Cookie(
                        name = "CONSENT",
                        value = "PENDING+987",
                        domain = "www.google.com",
                    ),
                    @Suppress("SpellCheckingInspection")
                    Cookie(
                        name = "SOCS",
                        value = "CAESHAgBEhJnd3NfMjAyMzA4MTAtMF9SQzIaAmRlIAEaBgiAo_CmBg",
                        domain = "www.google.com",
                    ),
                )
            }
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status !in expectedStatusCodes) {
                        throw when (response.status.value) {
                            in 500..599 -> ServerResponseException(response, "<not implemented>")
                            else -> ResponseException(response, "<not implemented>")
                        }
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = CONNECT_TIMEOUT
                socketTimeoutMillis = SOCKET_TIMEOUT
            }
            // Set custom User-Agent, so that we don't receive Google Lite HTML, which doesn't contain coordinates in
            // case of Google Maps or maps link in case of Google Search.
            install(UserAgent) {
                // Use custom user agent, because BrowserUserAgent() shows unsupported browser error in Apple Maps.
                agent = DESKTOP_USER_AGENT
            }
        }.use { client ->
            try {
                client.prepareRequest(url) {
                    this.method = method
                }.execute(block)
            } catch (tr: UnresolvedAddressException) {
                log.w(null, "Unresolved address for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_unresolved_address, tr)
            } catch (tr: HttpRequestTimeoutException) {
                log.w(null, "Request timeout for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_request_timeout, tr)
            } catch (tr: SocketTimeoutException) {
                log.w(null, "Socket timeout for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_socket_timeout, tr)
            } catch (tr: ConnectTimeoutException) {
                log.w(null, "Connect timeout for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_connect_timeout, tr)
            } catch (tr: EOFException) {
                log.w(null, "EOF exception for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_eof, tr)
            } catch (tr: ServerResponseException) {
                log.w(null, "Server error ${tr.response.status} for $url", tr)
                throw RecoverableNetworkException(R.string.network_exception_server_response_error, tr)
            } catch (tr: ResponseException) {
                when (tr.response.status.value) {
                    429 -> {
                        log.w(null, "Too many requests for $url", tr)
                        throw UnrecoverableNetworkException(R.string.network_exception_too_many_requests, tr)
                    }

                    else -> {
                        log.w(null, "Unexpected response code ${tr.response.status} for $url", tr)
                        throw UnrecoverableNetworkException(R.string.network_exception_response_error, tr)
                    }
                }
            } catch (tr: Exception) {
                log.e(null, "Unknown network exception for $url", tr)
                throw UnrecoverableNetworkException(R.string.network_exception_unknown, tr)
            }
        }
    }

    companion object {
        const val DESKTOP_USER_AGENT =
            @Suppress("SpellCheckingInspection") "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
        const val MAX_RETRIES = 9
        const val EXPONENTIAL_DELAY_BASE = 2.0
        const val EXPONENTIAL_DELAY_BASE_DELAY = 1_000L
        const val REQUEST_TIMEOUT = 256_000L
        const val CONNECT_TIMEOUT = 128_000L
        const val SOCKET_TIMEOUT = 128_000L
    }
}
