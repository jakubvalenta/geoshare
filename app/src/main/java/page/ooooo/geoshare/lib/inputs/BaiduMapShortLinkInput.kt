package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object BaiduMapShortLinkInput : HeadLocationHeaderInput {
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.BAIDU_MAP,
        items = listOf(
            InputDocumentationItem.Url(35, "https://j.map.baidu.com"),
        ),
    )
    override val pattern = Regex("""((?:https?://)?j\.map\.baidu\.com/\S+)""")

    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextStep = NextStep(BaiduMapUriInput, data.toString())
    }

    override fun toString() = "BaiduMapShortLinkInput"
}
