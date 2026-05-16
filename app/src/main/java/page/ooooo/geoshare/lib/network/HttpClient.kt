package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.request
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.EOFException
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import java.net.URL

const val DESKTOP_USER_AGENT =
    @Suppress("SpellCheckingInspection") "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36"
const val REQUEST_TIMEOUT = 256_000L
const val CONNECT_TIMEOUT = 128_000L
const val SOCKET_TIMEOUT = 128_000L

private const val TAG = "NetworkTools"

/**
 * Creates a pre-configured HTTP client.
 *
 * TODO Docs
 *
 * Throws [NetworkException] if there is a connection error or if the HTTP response code is unexpected. Expected
 * response codes are 2xx, and if [HttpClientConfig.followRedirects] is false, then also 3xx.
 */
fun HttpClient(
    engine: HttpClientEngine = CIO.create(),
    log: Log = DefaultLog,
    userConfig: HttpClientConfig<*>.() -> Unit = {},
): HttpClient = HttpClient(engine) {
    userConfig()
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
    HttpResponseValidator {
        val followRedirects = this@HttpClient.followRedirects
        validateResponse { response ->
            val statusCode = response.status.value
            if (statusCode < 300) {
                return@validateResponse
            }
            val exceptionResponseText = "<not implemented>"
            val exception = when (statusCode) {
                in 300..399 -> if (followRedirects) {
                    RedirectResponseException(response, exceptionResponseText)
                } else {
                    // If the caller doesn't want to follow redirect, it probably means it wants to read the
                    // response Location header and expects the request to return 3xx, so let's not throw
                    return@validateResponse
                }

                in 400..499 -> ClientRequestException(response, exceptionResponseText)
                in 500..599 -> ServerResponseException(response, exceptionResponseText)
                else -> ResponseException(response, exceptionResponseText)
            }
            throw exception
        }
        handleResponseExceptionWithRequest { cause, request ->
            when (cause) {
                is UnresolvedAddressException -> {
                    log.w(TAG, "Unresolved address for ${request.url}", cause)
                    throw UnresolvedAddressNetworkException(cause)
                }

                is HttpRequestTimeoutException -> {
                    log.w(TAG, "Request timeout for ${request.url}", cause)
                    throw RequestTimeoutNetworkException(cause)
                }

                is SocketTimeoutException -> {
                    log.w(TAG, "Socket timeout for ${request.url}", cause)
                    throw SocketTimeoutNetworkException(cause)
                }

                is ConnectTimeoutException -> {
                    log.w(TAG, "Connect timeout for ${request.url}", cause)
                    throw ConnectTimeoutNetworkException(cause)
                }

                is EOFException -> {
                    log.w(TAG, "EOF exception for ${request.url}", cause)
                    throw ConnectionClosedNetworkException(cause)
                }

                is ServerResponseException -> {
                    log.w(TAG, "Server error ${cause.response.status} for ${request.url}", cause)
                    throw ServerResponseNetworkException(cause.response.status, cause)
                }

                is ResponseException -> {
                    // Catches also subclasses such as RedirectResponseException and ClientRequestException
                    log.w(TAG, "Unexpected response code ${cause.response.status} for ${request.url}", cause)
                    throw ResponseNetworkException(cause.response.status, cause)
                }

                else -> {
                    log.e(TAG, "Unknown network exception for ${request.url}", cause)
                    throw UnknownNetworkException(cause)
                }
            }
        }
    }
}

/**
 * Makes a HEAD request to [url] and returns the value of the response Location header.
 */
@Throws(NetworkException::class)
suspend fun HttpClient.headLocationHeader(url: URL): String =
    config {
        // FIXME Follow redirects must be set before HttpClient's config
        followRedirects = false
    }
        .head(url)
        .headers[HttpHeaders.Location] ?: throw MissingHeaderNetworkException()

/**
 * Makes a GET request to [url], follows all redirects, and returns the final URL that the request redirected to.
 */
@Throws(NetworkException::class)
suspend fun HttpClient.getRedirectUrlString(url: URL): String =
    get(url)
        .request.url.toString()
