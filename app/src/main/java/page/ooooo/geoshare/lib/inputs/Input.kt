package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.geo.Point
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface Input {
    fun getName(resources: Resources): String
    val group: InputGroup
    val changelog: ImmutableList<InputChangelogItem> get() = persistentListOf()

    fun match(source: String): String? = null

    interface HasPermission

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

/**
 * Input that fetches the data it needs, for examples make s HEAD request to resolve a short link, in the [fetch] method
 * and then parses the data in the [parse] method.
 */
interface BasicInput<T> : Input {
    suspend fun fetch(match: String, block: suspend (T) -> ParseResult): ParseResult

    suspend fun parse(data: T, match: String, resources: Resources): ParseResult
}

/**
 * Input that waits for the UI layer to render a WebView and to provide the data extracted from the WebView to the
 * [parse] method.
 */
interface WebViewInput : Input, Input.HasPermission {
    val timeout: Duration get() = 60.seconds

    fun getUnsafeExtractionJavaScript(match: String): String

    suspend fun parse(data: String, match: String, resources: Resources): ParseResult

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface NoopInput : Input
