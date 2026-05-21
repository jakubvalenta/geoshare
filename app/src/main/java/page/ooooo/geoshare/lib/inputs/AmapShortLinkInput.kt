package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmapShortLinkInput @Inject constructor(
    private val amapUriInput: dagger.Lazy<AmapUriInput>,
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : HeadLocationHeaderInput {
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

    override suspend fun parse(data: Uri, match: String, prevResult: ParseResult?) = parseResult {
        nextStep = NextStep(amapUriInput.get(), data.toString())
    }

    override fun toString() = "AmapShortLinkInput"
}
