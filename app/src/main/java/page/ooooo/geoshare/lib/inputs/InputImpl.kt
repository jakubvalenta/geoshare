package page.ooooo.geoshare.lib.inputs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.prepareRequest
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.network.getLastHopUrlString
import page.ooooo.geoshare.lib.network.headLocationHeader
import page.ooooo.geoshare.lib.network.rethrowExceptionsAsNetworkException
import page.ooooo.geoshare.lib.network.setCookies
import page.ooooo.geoshare.lib.network.setDefaultTimeouts
import page.ooooo.geoshare.lib.network.setUserAgent
import java.net.MalformedURLException
import kotlin.coroutines.CoroutineContext

interface TextInput : BasicInput<String> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (String) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        block(match)
    }
}

interface UriInput : BasicInput<Uri> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        block(Uri.parse(match, uriQuote))
    }
}

interface GetLastHopUrlInput : UriInput, Input.HasPermission {
    @Suppress("SameReturnValue")
    val cookies: CookiesStorage? get() = null

    @Suppress("SameReturnValue")
    val userAgent: String? get() = null

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
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
        log.i(TAG, "Resolved short link $match to $unshortenedUri")
        block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "GetRedirectUrlInput"
    }
}

interface HeadLocationHeaderInput : UriInput, Input.HasPermission {
    @Suppress("SameReturnValue")
    val cookies: CookiesStorage? get() = null

    @Suppress("SameReturnValue")
    val userAgent: String? get() = null

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
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
        log.i(TAG, "Resolved short link $match to $unshortenedUri")
        block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "HeadLocationHeaderInput"
    }
}

interface BodyAsChannelInput : BasicInput<ByteReadChannel>, Input.HasPermission {
    @Suppress("SameReturnValue")
    val cookies: CookiesStorage? get() = null

    @Suppress("SameReturnValue")
    val userAgent: String? get() = null

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (ByteReadChannel) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        HttpClient(engine) {
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

    private companion object {
        private const val TAG = "BodyAsChannelInput"
    }
}

interface BodyAsTextInput : BasicInput<String>, Input.HasPermission {
    @Suppress("SameReturnValue")
    val cookies: CookiesStorage? get() = null

    @Suppress("SameReturnValue")
    val userAgent: String? get() = null

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (String) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        HttpClient(engine) {
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

    private companion object {
        private const val TAG = "BodyAsTextInput"
    }
}
