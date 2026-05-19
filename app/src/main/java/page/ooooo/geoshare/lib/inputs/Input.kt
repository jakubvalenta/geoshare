package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import page.ooooo.geoshare.lib.geo.Point

sealed interface Input<T> {
    @Suppress("SameReturnValue")
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    suspend fun parse(data: T, match: String, prevResult: ParseResult? = null): ParseResult

    interface HasPermission {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

interface BasicInput<T> : Input<T> {
    suspend fun fetch(match: String, block: suspend (T) -> ParseResult): ParseResult
}

interface WebViewInput : Input<String>, Input.HasPermission {
    val unsafeExtractionJavascript: String

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}
