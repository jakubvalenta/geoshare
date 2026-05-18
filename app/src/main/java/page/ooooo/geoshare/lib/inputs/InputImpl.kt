package page.ooooo.geoshare.lib.inputs

import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.prepareRequest
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.network.HttpClient
import page.ooooo.geoshare.lib.network.getLastHopUrlString
import page.ooooo.geoshare.lib.network.headLocationHeader
import java.net.MalformedURLException
import kotlin.coroutines.CoroutineContext

interface TextInput : BasicInput<String> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun withData(
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

    override suspend fun withData(
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
    override suspend fun withData(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = HttpClient(engine, log).use { client ->
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
    override suspend fun withData(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult = withContext(coroutineContext) {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = HttpClient(engine, log).use { client ->
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
    override suspend fun withData(
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
        HttpClient(engine, log).use { client ->
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
    override suspend fun withData(
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
        HttpClient(engine, log).use { client ->
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
