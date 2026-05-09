package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points

object GoogleMapsShortLinkInput : HeadLocationHeaderInput {
    override val pattern = Regex("""((?:https?://)?(?:(?:maps\.)?(?:app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GOOGLE_MAPS,
        nameResId = R.string.converter_google_maps_name,
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

    override suspend fun parse(data: Uri, prevPoints: Points?, uriQuote: UriQuote, log: ILog) = buildParseResult {
        data.run {
            nextInput = GoogleMapsUriInput

            // Google Maps Go
            // https://maps.app.goo.gl/?link={url}
            queryParams["link"]?.takeIf { it.isNotEmpty() }?.let {
                nextMatch = it
            }
        }
    }
}
