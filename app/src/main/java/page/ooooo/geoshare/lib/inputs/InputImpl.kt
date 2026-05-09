package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.network.NetworkTools
import java.net.MalformedURLException

interface TextInput : BasicInput<String> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (String) -> ParseResult,
    ) = block(match)
}

interface UriInput : BasicInput<Uri> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ) = block(Uri.parse(match, uriQuote))
}

interface GetRedirectUrlInput : UriInput, Input.HasPermission {
    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = networkTools.httpGetRedirectedUrlString(
            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short URI $match to $unshortenedUri")
        return block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "GetRedirectUrlInput"
    }
}

interface HeadLocationHeaderInput : UriInput, Input.HasPermission {
    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        val unshortenedUrlString = networkTools.httpHeadLocationHeader(
            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short URI $match to $unshortenedUri")
        return block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "HeadLocationHeaderInput"
    }
}

interface BodyAsChannelInput : BasicInput<ByteReadChannel>, Input.HasPermission {
    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (ByteReadChannel) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        return networkTools.httpGetBodyAsByteReadChannel(
            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts, dispatcher = Dispatchers.Default, block,
        )
    }

    private companion object {
        private const val TAG = "BodyAsChannelInput"
    }
}

interface BodyAsTextInput : BasicInput<String>, Input.HasPermission {
    override suspend fun withData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (String) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val url = uri.toUrl() ?: throw MalformedURLException()
        log.i(TAG, "Downloading $uri")
        val text = networkTools.httpGetBodyAsText(
            url, lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        return block(text)
    }

    private companion object {
        private const val TAG = "BodyAsTextInput"
    }
}
