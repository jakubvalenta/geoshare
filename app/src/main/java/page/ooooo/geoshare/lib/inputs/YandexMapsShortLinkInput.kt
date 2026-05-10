package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object YandexMapsShortLinkInput : HeadLocationHeaderInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title

    override val pattern = Regex("""((?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+)""")

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextInput = YandexMapsUriInput
        nextMatch = data.toString()
    }

    override fun toString() = "YandexMapsShortLinkInput"
}
