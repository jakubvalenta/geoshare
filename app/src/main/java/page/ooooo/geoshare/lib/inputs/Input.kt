package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import page.ooooo.geoshare.lib.geo.Point

sealed interface Input {
    @Suppress("SameReturnValue")
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    interface HasPermission {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

interface BasicInput<T> : Input {
    suspend fun fetch(match: String, block: suspend (T) -> ParseResult): ParseResult

    suspend fun parse(data: T, match: String, prevResult: ParseResult? = null): ParseResult
}

interface WebViewInput : Input, Input.HasPermission {
    val unsafeExtractionJavascript: String

    suspend fun parse(data: String, match: String, prevResult: ParseResult? = null): ParseResult

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface NoopInput : Input
