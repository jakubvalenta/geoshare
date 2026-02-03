package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri

/**
 * Debug input is useful for WebView testing, because it has web parsing enabled but doesn't make a request to
 * a commercial website.
 */
object DebugInput : Input.HasWeb {
    override val uriPattern = Regex("""(?:https?://)?(?:www\.)?example\.com(?:/\S+|$)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.DEBUG,
        nameResId = R.string.converter_debug_name,
        items = emptyList(),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        webUriString = uri.toString()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_debug_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_debug_loading_indicator_title
}
