package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object AmapShortLinkInput : HeadLocationHeaderInput {
    override val pattern = Regex("""((?:https?://)?surl\.amap\.com/\S+)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.AMAP,
        items = listOf(
            InputDocumentationItem.Url(27, "https://surl.amap.com/"),
        ),
    )

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextInput = AmapUriInput
        nextMatch = data.toString()
    }

    override fun toString() = "AmapShortLinkInput"
}
