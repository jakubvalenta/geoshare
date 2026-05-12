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
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.EOFException
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import java.net.URL
import kotlin.math.pow
import kotlin.math.roundToLong

interface NetworkTools {
    data class Attempt(val number: Int, val cause: RecoverableNetworkException)

    suspend fun httpHeadLocationHeader(
        url: URL,
        lastAttempt: Attempt? = null,
        maxAttempts: Int = 1,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String

    suspend fun <T> httpGetBodyAsByteReadChannel(
        url: URL,
        lastAttempt: Attempt? = null,
        maxAttempts: Int = 1,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend (channel: ByteReadChannel) -> T,
    ): T

    suspend fun httpGetBodyAsText(
        url: URL,
        lastAttempt: Attempt? = null,
        maxAttempts: Int = 1,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String

    suspend fun httpGetRedirectedUrlString(
        url: URL,
        lastAttempt: Attempt? = null,
        maxAttempts: Int = 1,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String
}

class DefaultNetworkTools(
    private val engine: HttpClientEngine = CIO.create(),
    private val log: Log = DefaultLog,
) : NetworkTools {
    /**
     * Makes a HEAD request to [url] and returns the value of the response Location header.
     *
     * To enable retrying, pass [maxAttempts] and [lastAttempt]. [maxAttempts] sets the maximum number of requests to
     * make including the first request. [lastAttempt] tracks how many attempts have already been made. We use this
     * custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, so that the caller can notify
     * the user while requests are being retried.
     *
     * The network request is executed on [dispatcher].
     */
    @Throws(NetworkException::class)
    override suspend fun httpHeadLocationHeader(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = withContext(dispatcher) {
        connect(
            engine,
            url,
            method = HttpMethod.Head,
            followRedirects = false,
            lastAttempt = lastAttempt,
            maxAttempts = maxAttempts,
        ) { response ->
            response.headers[HttpHeaders.Location]
        } ?: throw MissingHeaderNetworkException()
    }

    /**
     * Makes a GET request to [url] and invokes [block] with the resulting [ByteReadChannel].
     *
     * To enable retrying, pass [maxAttempts] and [lastAttempt]. [maxAttempts] sets the maximum number of requests to
     * make including the first request. [lastAttempt] tracks how many attempts have already been made. We use this
     * custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, so that the caller can notify
     * the user while requests are being retried.
     *
     * The network request as well as the [block] are executed on [dispatcher]. When the [block] finishes, the channel
     * is closed.
     */
    @Throws(NetworkException::class)
    override suspend fun <T> httpGetBodyAsByteReadChannel(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
        block: suspend (channel: ByteReadChannel) -> T,
    ): T = withContext(dispatcher) {
        connect(engine, url, lastAttempt = lastAttempt, maxAttempts = maxAttempts) { response ->
            val channel: ByteReadChannel = response.body()
            block(channel)
        }
    }

    /**
     * Makes a HEAD request to [url] and returns the response body as text.
     *
     * To enable retrying, pass [maxAttempts] and [lastAttempt]. [maxAttempts] sets the maximum number of requests to
     * make including the first request. [lastAttempt] tracks how many attempts have already been made. We use this
     * custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, so that the caller can notify
     * the user while requests are being retried.
     *
     * The network request is executed on [dispatcher].
     */
    @Throws(NetworkException::class)
    override suspend fun httpGetBodyAsText(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = withContext(dispatcher) {
        connect(engine, url, lastAttempt = lastAttempt, maxAttempts = maxAttempts) { response ->
            response.bodyAsText()
        }
    }

    /**
     * Makes a GET request to [url], follows all redirects, and returns the final URL that the request redirected to.
     *
     * To enable retrying, pass [maxAttempts] and [lastAttempt]. [maxAttempts] sets the maximum number of requests to
     * make including the first request. [lastAttempt] tracks how many attempts have already been made. We use this
     * custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, so that the caller can notify
     * the user while requests are being retried.
     *
     * The network request is executed on [dispatcher].
     */
    @Throws(NetworkException::class)
    override suspend fun httpGetRedirectedUrlString(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = withContext(dispatcher) {
        connect(engine, url, lastAttempt = lastAttempt, maxAttempts = maxAttempts) { response ->
            response.request.url.toString()
        }
    }

    /**
     * Internal method that makes HTTP request to [url] using [method] and invokes [block] with the [HttpResponse].
     * This method is exposed only due to unit tests. Use high-level methods such as [httpGetBodyAsByteReadChannel].
     *
     * Throws [NetworkException] if there is a connection error or if the HTTP response code is unexpected. Expected
     * response codes are 2xx if [followRedirects] is true or 3xx if [followRedirects] is false.
     *
     * To enable retrying, pass [maxAttempts] and [lastAttempt]. [maxAttempts] sets the maximum number of requests to
     * make including the first request. [lastAttempt] tracks how many attempts have already been made. We use this
     * custom retrying instead of the [io.ktor.client.plugins.HttpRequestRetry] plugin, so that the caller can notify
     * the user while requests are being retried.
     */
    @Throws(NetworkException::class)
    suspend fun <T> connect(
        engine: HttpClientEngine,
        url: URL,
        method: HttpMethod = HttpMethod.Get,
        followRedirects: Boolean = true,
        lastAttempt: NetworkTools.Attempt? = null,
        maxAttempts: Int = 1,
        block: suspend (response: HttpResponse) -> T,
    ): T {
        if (lastAttempt != null && lastAttempt.number > 1) {
            if (lastAttempt.number > maxAttempts) {
                log.w(TAG, "Maximum number of $maxAttempts attempts reached for $url")
                throw MaxAttemptsReachedNetworkException(lastAttempt.cause)
            }
            val delayMillis = (EXPONENTIAL_DELAY_BASE.pow(lastAttempt.number - 2) * EXPONENTIAL_DELAY_BASE_DELAY)
                .roundToLong()
            log.i(TAG, "Waiting ${delayMillis}ms before attempt ${lastAttempt.number} of $maxAttempts for $url")
            delay(delayMillis)
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
                    when {
                        response.status.isSuccess() || !followRedirects && response.status.isRedirect() -> {}
                        response.status.isServerError() -> throw ServerResponseException(response, "<not implemented>")
                        else -> throw ResponseException(response, "<not implemented>")
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
                client
                    .prepareRequest(url) {
                        this.method = method
                    }
                    .execute(block)
            } catch (tr: UnresolvedAddressException) {
                log.w(TAG, "Unresolved address for $url", tr)
                throw UnresolvedAddressNetworkException(tr)
            } catch (tr: HttpRequestTimeoutException) {
                log.w(TAG, "Request timeout for $url", tr)
                throw RequestTimeoutNetworkException(tr)
            } catch (tr: SocketTimeoutException) {
                log.w(TAG, "Socket timeout for $url", tr)
                throw SocketTimeoutNetworkException(tr)
            } catch (tr: ConnectTimeoutException) {
                log.w(TAG, "Connect timeout for $url", tr)
                throw ConnectTimeoutNetworkException(tr)
            } catch (tr: EOFException) {
                log.w(TAG, "EOF exception for $url", tr)
                throw ConnectionClosedNetworkException(tr)
            } catch (tr: ServerResponseException) {
                log.w(TAG, "Server error ${tr.response.status} for $url", tr)
                throw ServerResponseNetworkException(tr.response.status, tr)
            } catch (tr: ResponseException) {
                log.w(TAG, "Unexpected response code ${tr.response.status} for $url", tr)
                throw ResponseNetworkException(tr.response.status, tr)
            } catch (tr: Exception) {
                log.e(TAG, "Unknown network exception for $url", tr)
                throw UnknownNetworkException(tr)
            }
        }
    }

    private fun HttpStatusCode.isRedirect(): Boolean = value in (300 until 400)

    private fun HttpStatusCode.isServerError(): Boolean = value in (500 until 600)

    companion object {
        const val DESKTOP_USER_AGENT =
            @Suppress("SpellCheckingInspection") "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36"
        const val EXPONENTIAL_DELAY_BASE = 2.0
        const val EXPONENTIAL_DELAY_BASE_DELAY = 1_000L
        const val REQUEST_TIMEOUT = 256_000L
        const val CONNECT_TIMEOUT = 128_000L
        const val SOCKET_TIMEOUT = 128_000L

        private const val TAG = "NetworkTools"
    }
}

open class FakeNetworkTools : NetworkTools {
    override suspend fun httpHeadLocationHeader(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = throw NotImplementedError()

    override suspend fun <T> httpGetBodyAsByteReadChannel(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
        block: suspend (channel: ByteReadChannel) -> T,
    ): T = throw NotImplementedError()

    override suspend fun httpGetBodyAsText(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = throw NotImplementedError()

    override suspend fun httpGetRedirectedUrlString(
        url: URL,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        dispatcher: CoroutineDispatcher,
    ): String = throw NotImplementedError()
}
