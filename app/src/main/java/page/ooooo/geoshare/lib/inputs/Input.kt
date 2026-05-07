package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import io.ktor.utils.io.ByteReadChannel
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import java.net.URL

interface Input {
    val uriPattern: Regex
    val documentation: InputDocumentation

    suspend fun parseUri(uri: Uri, uriQuote: UriQuote = DefaultUriQuote): ParseUriResult

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

interface ShortUriInput : Input {
    enum class Method { GET, HEAD }

    val shortUriPattern: Regex
    val shortUriMethod: Method
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}

interface HtmlInput : Input {
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int

    suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: Points,
        uriQuote: UriQuote = DefaultUriQuote,
        log: ILog = DefaultLog,
    ): ParseHtmlResult
}

interface WebInput : Input {
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

sealed interface NewInput {
    val pattern: Regex? get() = null
    val documentation: InputDocumentation? get() = null

    interface HasPermission {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}

interface ShortLinkGetInput : NewInput, NewInput.HasPermission {
    suspend fun parse(unshortenedUri: Uri): ParseResult
}

interface ShortLinkHeadInput : NewInput, NewInput.HasPermission {
    suspend fun parse(unshortenedUri: Uri): ParseResult
}

interface NewUriInput : NewInput {
    suspend fun parse(uri: Uri, uriQuote: UriQuote = DefaultUriQuote): ParseResult
}

interface NewHtmlInput : NewInput, NewInput.HasPermission {
    suspend fun parse(
        channel: ByteReadChannel,
        prevPoints: Points? = null,
        uriQuote: UriQuote = DefaultUriQuote,
        log: ILog = DefaultLog,
    ): ParseResult
}

interface NewWebInput : NewInput, NewInput.HasPermission {
    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false

    suspend fun parse(webUrlString: String): ParseResult
}

interface ApiInput : NewInput, NewInput.HasPermission {
    suspend fun parse(url: URL): ParseResult
}
