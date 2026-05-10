package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points

/**
 * Loads example.com in a WebView.
 *
 * This input iss useful for WebView testing, because it doesn't make a request to a commercial website.
 */
object DebugUriInput : UriInput {
    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.DEBUG,
        items = emptyList(),
    )

    override suspend fun parse(
        data: Uri,
        match: String,
        prevPoints: Points?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextInput = DebugWebViewInput
    }

    override fun toString() = "DebugInput"
}
