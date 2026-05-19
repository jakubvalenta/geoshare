package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapyComShortLinkInput @Inject constructor(
    private val mapyComUriInput: dagger.Lazy<MapyComUriInput>,
) : GetLastHopUrlInput {
    override val pattern = Regex("""((?:https?://)?(?:www\.)?mapy\.[a-z]{2,3}/s/\S+)""")

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextStep = NextStep(mapyComUriInput.get(), data.toString())
    }

    override fun toString() = "MapyComShortLinkInput"
}
