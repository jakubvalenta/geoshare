package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points

object AmapShortLinkInput : HeadLocationHeaderInput {
    override val pattern = Regex("""((?:https?://)?surl\.amap\.com/\S+)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.AMAP,
        nameResId = R.string.converter_amap_name,
        items = listOf(
            InputDocumentationItem.Url(27, "https://surl.amap.com/"),
        ),
    )

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title

    override suspend fun parse(data: Uri, prevPoints: Points?, uriQuote: UriQuote, log: ILog) = buildParseResult {
        nextInput = AmapUriInput
        nextMatch = data.toString()
    }
}
