package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.geo.Point
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface Input {
    @Suppress("SameReturnValue")
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    fun getErrorMessage(resources: Resources): String =
        resources.getString(R.string.conversion_failed_reason_no_points)

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

    suspend fun parse(data: T, match: String): ParseResult
}

interface WebViewInput : Input, Input.HasPermission {
    val timeout: Duration get() = 60.seconds
    val unsafeExtractionJavascript: String

    suspend fun parse(data: String, match: String): ParseResult

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface NoopInput : Input
