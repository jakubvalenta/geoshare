package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
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
    override val uriQuote: UriQuote,
) : UriInput {
    override val group = InputGroup.DEBUG
    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        next = MatchedInput(debugWebViewInput.get(), match)
    }

    override fun toString() = "DebugUriInput"
}
