package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.point.Point

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

    override suspend fun onUrlChange(
        urlString: String,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseWebResult? {
        return null
    }

    override fun shouldInterceptRequest(requestUrlString: String, log: ILog) = false

    @StringRes
    override val permissionTitleResId = R.string.converter_debug_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_debug_loading_indicator_title
}
