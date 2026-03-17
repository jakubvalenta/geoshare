package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import io.ktor.utils.io.ByteReadChannel
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.Point

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
        pointsFromUri: ImmutableList<Point>,
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
