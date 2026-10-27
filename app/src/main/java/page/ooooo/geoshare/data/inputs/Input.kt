package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.data.extensions.groupOrNull
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.network.DefaultFetchTools
import page.ooooo.geoshare.data.network.FetchTools
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

    interface HasRandomSource {
        fun genRandomSource(point: Point): String?
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
 * Input that expects the caller to render a WebView with a match as the page URL, execute [unsafeClientJavaScript]
 * to read data from the page, and then call [parse] with this data.
 */
interface WebViewInput : Input {
    val allowedUrlPattern: Regex

    /**
     * JavaScript code that will be executed on the page. It is considered unsafe, because it can lead to a syntax error
     * or an injection.
     */
    val unsafeClientJavaScript: String

    val timeout: Duration get() = 60.seconds

    fun parse(data: String, match: String, resources: Resources): ParseResult

    fun extendWebSettings(settings: WebSettings) {}

    fun shouldInterceptRequest(requestUrlString: String): Boolean = false
}

interface NoopInput : Input
