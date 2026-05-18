package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Point
import kotlin.coroutines.CoroutineContext

sealed interface Input<T> {
    @Suppress("SameReturnValue")
    val documentation: InputDocumentation? get() = null

    fun match(source: String): String? = null

    suspend fun parse(
        data: T,
        match: String,
        prevResult: ParseResult? = null,
        uriQuote: UriQuote = DefaultUriQuote,
        log: Log = DefaultLog,
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
    suspend fun withData(
        match: String,
        engine: HttpClientEngine = CIO.create(),
        log: Log = DefaultLog,
        uriQuote: UriQuote = DefaultUriQuote,
        coroutineContext: CoroutineContext = Dispatchers.Default,
        block: suspend (T) -> ParseResult,
    ): ParseResult
}

interface WebViewInput : Input<String>, Input.HasPermission {
    val unsafeExtractionJavascript: String

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}
