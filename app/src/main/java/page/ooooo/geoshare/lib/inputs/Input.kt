package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.network.NetworkTools

sealed interface Input<T> {
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    suspend fun parse(
        data: T,
        // TODO Add match: String,
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

interface BasicInput<T> : Input<T> {
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

interface WebViewInput : Input<String>, Input.HasPermission {
    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}
