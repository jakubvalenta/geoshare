package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads example.com in a WebView.
 *
 * This input iss useful for WebView testing, because it doesn't make a request to a commercial website.
 */
@Singleton
class DebugUriInput @Inject constructor(
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.DEBUG

    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")

    override fun parse(match: String, resources: Resources) =
        parseResult {
            next = MatchedInput(DebugWebViewInput, match)
        }

    override fun toString() = "DebugUriInput"
}
