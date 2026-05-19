package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads example.com in a WebView.
 *
 * This input iss useful for WebView testing, because it doesn't make a request to a commercial website.
 */
@Singleton
class DebugUriInput @Inject constructor(
    private val debugWebViewInput: dagger.Lazy<DebugWebViewInput>,
) : UriInput {
    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.DEBUG,
        items = emptyList(),
    )

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextStep = NextStep(debugWebViewInput.get(), match)
    }

    override fun toString() = "DebugUriInput"
}
