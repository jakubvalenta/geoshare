package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.prepareRequest
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.uri.FakeUriQuote
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import java.net.MalformedURLException

interface FetchTools {
    suspend fun getLastHopUrl(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage? = null,
        userAgent: String? = null,
    ): Uri

    suspend fun headLocationHeader(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage? = null,
        userAgent: String? = null,
    ): Uri

    suspend fun <R> getBodyAsChannel(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage? = null,
        userAgent: String? = null, block: suspend (data: ByteReadChannel) -> R,
    ): R

    suspend fun <R> getBodyAsText(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage? = null,
        userAgent: String? = null, block: suspend (data: String) -> R,
    ): R
}

object DefaultFetchTools : FetchTools {
    override suspend fun getLastHopUrl(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
    ): Uri {
        val uri = Uri.parse(uriString, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = HttpClient(engine) {
            expectSuccess = true
            setCookies(cookies)
            setDefaultTimeouts()
            setUserAgent(userAgent)
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            client.getLastHopUrlString(url)
        }
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short link $uriString to $unshortenedUri")
        return unshortenedUri
    }

    override suspend fun headLocationHeader(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
    ): Uri {
        val uri = Uri.parse(uriString, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = HttpClient(engine) {
            expectSuccess = true
            setCookies(cookies)
            setDefaultTimeouts()
            setUserAgent(userAgent)
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            client.headLocationHeader(url)
        }
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short link $uriString to $unshortenedUri")
        return unshortenedUri
    }

    override suspend fun <R> getBodyAsChannel(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?, block: suspend (ByteReadChannel) -> R,
    ): R {
        val uri = Uri.parse(uriString, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        return HttpClient(engine) {
            expectSuccess = true
            setCookies(cookies)
            setDefaultTimeouts()
            setUserAgent(userAgent)
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            client
                .prepareRequest(url)
                .execute { response ->
                    block(response.body())
                }
        }
    }

    override suspend fun <R> getBodyAsText(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?, block: suspend (String) -> R,
    ): R {
        val uri = Uri.parse(uriString, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        return HttpClient(engine) {
            expectSuccess = true
            setCookies(cookies)
            setDefaultTimeouts()
            setUserAgent(userAgent)
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            client
                .prepareRequest(url)
                .execute { response ->
                    block(response.body())
                }
        }
    }

    private const val TAG = "FetchTools"
}

class FakeFetchTools(val body: String) : FetchTools {
    override suspend fun getLastHopUrl(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
    ): Uri = Uri.parse(uriString, FakeUriQuote)

    override suspend fun headLocationHeader(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
    ): Uri = Uri.parse(uriString, FakeUriQuote)

    override suspend fun <R> getBodyAsChannel(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
        block: suspend (data: ByteReadChannel) -> R,
    ): R = block(body.byteInputStream().toByteReadChannel())

    override suspend fun <R> getBodyAsText(
        uriString: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        cookies: CookiesStorage?,
        userAgent: String?,
        block: suspend (data: String) -> R,
    ): R = block(body)
}
