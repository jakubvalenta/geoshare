package page.ooooo.geoshare.data.inputs

import android.content.res.Resources

/**
 * Loads example.com in a WebView.
 *
 * This input iss useful for WebView testing, because it doesn't make a request to a commercial website.
 */
object DebugUriInput : BasicOfflineInput, Input.HasPattern {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = DebugInputGroup

    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")

    override fun parse(match: String, resources: Resources) =
        parseResult {
            next = MatchedInput(DebugWebViewInput, match)
        }

    override fun toString() = "DebugUriInput"
}
