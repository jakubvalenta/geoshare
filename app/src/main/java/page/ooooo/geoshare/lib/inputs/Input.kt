package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.network.DefaultFetchTools
import page.ooooo.geoshare.lib.network.FetchTools
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface Input {
    fun getName(resources: Resources): String
    val group: InputGroup
    val changelog: ImmutableList<InputChangelogItem> get() = persistentListOf()

    interface HasPattern {
        val pattern: Regex

        fun match(source: String) = pattern.find(source)?.groupOrNull()
    }

    interface HasRandomUri {
        fun genRandomUri(point: Point): String?
    }
}

sealed interface BasicInput : Input

/**
 * Input that expects the caller to call [parse] with a match. The implementation of [parse] must not make any network
 * connections.
 */
interface BasicOfflineInput : BasicInput {
    fun parse(match: String, resources: Resources): ParseResult
}

/**
 * Input that expects the caller to call [parse] with a match. The implementation of [parse] can make network
 * connections.
 */
interface BasicOnlineInput : BasicInput {
    suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools = DefaultFetchTools): ParseResult
}

/**
 * Input that expects the caller to render a WebView with a match as the page URL, call [getUnsafeExtractionJavaScript]
 * to get JavaScript that extracts data from the WebView, and then call [parse] with the extracted data.
 */
interface WebViewInput : Input {
    val timeout: Duration get() = 60.seconds

    fun getUnsafeExtractionJavaScript(): String

    fun parse(data: String, match: String, resources: Resources): ParseResult

    fun extendWebSettings(settings: WebSettings) {}
    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface NoopInput : Input
