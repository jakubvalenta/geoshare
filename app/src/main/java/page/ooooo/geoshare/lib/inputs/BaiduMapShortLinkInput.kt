package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points

object BaiduMapShortLinkInput : HeadLocationHeaderInput {
    override val documentation = InputDocumentation(
        id = InputDocumentationId.BAIDU_MAP,
        nameResId = R.string.converter_baidu_map_name,
        items = listOf(
            InputDocumentationItem.Url(35, "https://j.map.baidu.com"),
        ),
    )
    override val pattern = Regex("""((?:https?://)?j\.map\.baidu\.com/\S+)""")

    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title

    override suspend fun parse(data: Uri, prevPoints: Points?, uriQuote: UriQuote, log: ILog) = buildParseResult {
        nextInput = BaiduMapUriInput
        nextMatch = data.toString()
    }
}
