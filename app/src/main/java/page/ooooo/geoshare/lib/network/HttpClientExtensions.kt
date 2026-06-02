package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.EOFException
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import java.net.SocketException
import java.net.URL

const val DESKTOP_USER_AGENT =
    @Suppress("SpellCheckingInspection") "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36"
const val REQUEST_TIMEOUT = 256_000L
const val CONNECT_TIMEOUT = 128_000L
const val SOCKET_TIMEOUT = 128_000L

private const val TAG = "HttpClient"

/**
 * Makes a HEAD request to [url] and returns the value of the response Location header.
 */
@Throws(NetworkException::class)
suspend fun HttpClient.headLocationHeader(url: URL): String =
    config {
        followRedirects = false
    }.use { client ->
        try {
            client.head(url)
        } catch (e: RedirectResponseException) {
            // Expect that the request returns 3xx
            e.response
        } catch (e: ResponseNetworkException) {
            // Expect that the request returns 3xx; version for when the exception is wrapped in NetworkException
            (e.cause as? RedirectResponseException)?.response ?: throw e
        }
            .headers[HttpHeaders.Location] ?: throw MissingHeaderNetworkException()
    }

/**
 * Makes a GET request to [url], follows all redirects, and returns the final URL that the request redirected to.
 */
@Throws(NetworkException::class)
suspend fun HttpClient.getLastHopUrlString(url: URL): String =
    get(url).request.url.toString()

fun HttpClientConfig<*>.setCookies(cookies: CookiesStorage?) {
    if (cookies != null) {
        install(HttpCookies) {
            storage = cookies
        }
    }
}

fun HttpClientConfig<*>.setUserAgent(userAgent: String?) {
    if (userAgent != null) {
        install(UserAgent) {
            agent = userAgent
        }
    }
}

/**
 * Sets timeouts to values that are suited for slow internet connection.
 */
fun HttpClientConfig<*>.setDefaultTimeouts() {
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT
        connectTimeoutMillis = CONNECT_TIMEOUT
        socketTimeoutMillis = SOCKET_TIMEOUT
    }
}

/**
 * Configures [HttpClient] to rethrow all exceptions as [NetworkException], so the caller can decide whether to retry a
 * request based on whether the exception is [RecoverableNetworkException] or [UnrecoverableNetworkException].
 */
fun HttpClientConfig<*>.rethrowExceptionsAsNetworkException(log: Log = DefaultLog) {
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            when (cause) {
                is NetworkException -> {
                    // If the exception already is NetworkException, it probably means that it comes from another HTTP
                    // client created inside this HttpClient. Then we must throw the NetworkException cause, otherwise
                    // the original cause ends up wrapped in NetworkException twice for some reason
                    throw cause.cause ?: cause
                }

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

                is SocketException -> {
                    // Catches also subclasses such as ConnectException
                    log.w(TAG, "Socket exception for ${request.url}", cause)
                    throw ConnectionRefusedNetworkException(cause)
                }

                is EOFException -> {
                    log.w(TAG, "EOF exception for ${request.url}", cause)
                    throw ConnectionClosedNetworkException(cause)
                }

                is ServerResponseException -> {
                    log.w(TAG, "Server error ${cause.response.status} for ${request.url}", cause)
                    throw ServerResponseNetworkException(cause.response, cause)
                }

                is ResponseException -> {
                    // Catches also subclasses such as RedirectResponseException and ClientRequestException
                    log.w(TAG, "Unexpected response code ${cause.response.status} for ${request.url}", cause)
                    throw when (cause.response.status) {
                        HttpStatusCode.TooManyRequests -> TooManyRequestsNetworkException(cause.response, cause)
                        HttpStatusCode.Unauthorized -> UnauthorizedNetworkException(cause.response, cause)
                        else -> ResponseNetworkException(cause.response, cause)
                    }
                }

                else -> {
                    log.w(TAG, "Unknown network exception for ${request.url}", cause)
                    throw UnknownNetworkException(cause)
                }
            }
        }
    }
}
