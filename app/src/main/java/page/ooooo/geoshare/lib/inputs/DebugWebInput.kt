package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * Loads example.com in a WebView, so it's useful for WebView testing without making a request to a commercial website.
 */
object DebugInput : NewUriInput {
    override val pattern = Regex("""((?:https?://)?(?:www\.)?example\.com(?:/\S+|$))""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.DEBUG,
        nameResId = R.string.converter_debug_name,
        items = emptyList(),
    )

    override suspend fun parse(uri: Uri, uriQuote: UriQuote) = buildParseResult {
        nextInput = DebugWebInput
    }
}

object DebugWebInput : NewWebInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_debug_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_debug_loading_indicator_title

    override suspend fun parse(webUrlString: String) = buildParseResult {
        points = persistentListOf(WGS84Point(NaivePoint.genRandomPoint()))
    }
}
