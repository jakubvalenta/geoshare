package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object GoogleMapsShortLinkInput : HeadLocationHeaderInput {
    override val pattern = Regex("""((?:https?://)?(?:(?:maps\.)?(?:app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.GOOGLE_MAPS,
        items = listOf(
            InputDocumentationItem.Url(10, "https://g.co/kgs"),
            InputDocumentationItem.Url(5, "https://app.goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://maps.app.goo.gl"),
        ),
    )

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        data.run {
            // Google Maps Go
            // https://maps.app.goo.gl/?link={url}
            queryParams["link"]?.takeIf { it.isNotEmpty() }?.let {
                nextStep = NextStep(GoogleMapsUriInput, it)
                return@buildParseResult
            }

            nextStep = NextStep(GoogleMapsUriInput, data.toString())
        }
    }

    override fun toString() = "GoogleMapsShortLinkInput"
}
