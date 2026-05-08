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
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
        timeout: Duration = 60.seconds,
        uriQuote: UriQuote = DefaultUriQuote,
        log: ILog = DefaultLog,
    ): T
}

interface AsyncInput<T> : Input<T>

interface TextInput : SyncInput<String> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ) = match
}

interface UriInput : SyncInput<Uri> {
    val pattern: Regex

    override fun match(source: String) = pattern.find(source)?.groupOrNull()

    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ) = Uri.parse(match, uriQuote)
}

interface ShortLinkGetInput : UriInput, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ): Uri {
        val uri = Uri.parse(match, uriQuote)
        val unshortenedUrlString = networkTools.httpGetRedirectedUrlString(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, "Resolved short URI $match to $unshortenedUri")
        return unshortenedUri
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
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ): Uri {
        val uri = Uri.parse(match, uriQuote)
        val unshortenedUrlString = networkTools.httpHeadLocationHeader(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
        val unshortenedUri = Uri.parse(unshortenedUrlString, uriQuote).toAbsoluteUri(uri)
        log.i(TAG, " Resolved short URI $match to $unshortenedUri")
        return unshortenedUri
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
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ): ByteReadChannel {
        val uri = Uri.parse(match, uriQuote)
        log.i(TAG, "Downloading $uri")
        // TODO dispatcher param
        networkTools.httpGetBodyAsByteReadChannel(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts, dispatcher = Dispatchers.Default
        ) { channel ->
            TODO()
        }
    }

    private companion object {
        private const val TAG = "HtmlInput"
    }
}

interface WebInput : AsyncInput<URL>, Input.HasPermission {
    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface ApiInput : SyncInput<String>, Input.HasPermission {
    override suspend fun getData(
        match: String,
        networkTools: NetworkTools,
        lastAttempt: NetworkTools.Attempt?,
        maxAttempts: Int,
        timeout: Duration,
        uriQuote: UriQuote,
        log: ILog,
    ): String {
        val uri = Uri.parse(match, uriQuote)
        return networkTools.httpGetBodyAsText(
            uri.toUrl(), lastAttempt = lastAttempt, maxAttempts = maxAttempts
        )
    }
}
