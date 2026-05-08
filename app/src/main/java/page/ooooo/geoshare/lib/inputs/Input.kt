package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.network.NetworkTools

sealed interface Input<T> {
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    suspend fun parse(
        data: T,
        prevPoints: Points? = null,
        uriQuote: UriQuote = DefaultUriQuote,
        log: ILog = DefaultLog,
    ): ParseResult

    interface HasPermission {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

interface SyncInput<T> : Input<T> {
    suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote = DefaultUriQuote,
        log: ILog = DefaultLog,
        block: suspend (T) -> ParseResult,
    ): ParseResult
}

// TODO Rename to WebView input
interface WebInput : Input<String>, Input.HasPermission {
    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface TextInput : SyncInput<String> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (String) -> ParseResult,
    ) = block(match)
}

interface UriInput : SyncInput<Uri> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ) = block(Uri.parse(match, uriQuote))
}

interface ShortLinkGetInput : UriInput, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val unshortenedUrlString = networkTools.httpGetRedirectedUrlString(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short URI $match to $unshortenedUri")
        return block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "ShortLinkGetInput"
    }
}

interface ShortLinkHeadInput : UriInput, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (Uri) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val unshortenedUrlString = networkTools.httpHeadLocationHeader(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, " Resolved short URI $match to $unshortenedUri")
        return block(unshortenedUri)
    }

    private companion object {
        private const val TAG = "ShortLinkHeadInput"
    }
}

interface HtmlInput : SyncInput<ByteReadChannel>, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (ByteReadChannel) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        log.i(TAG, "Downloading $uri")
        return networkTools.httpGetBodyAsByteReadChannel(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts, dispatcher = Dispatchers.Default, block,
        )
    }

    private companion object {
        private const val TAG = "HtmlInput"
    }
}

interface ApiInput : SyncInput<String>, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        uriQuote: UriQuote,
        log: ILog,
        block: suspend (String) -> ParseResult,
    ): ParseResult {
        val uri = Uri.parse(match, uriQuote)
        val text = networkTools.httpGetBodyAsText(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        return block(text)
    }
}
