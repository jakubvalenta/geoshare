package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.http.Cookie
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsShortLinkInput @Inject constructor(
    private val googleMapsUriInput: GoogleMapsUriInput,
) : HeadLocationHeaderInput {
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

    override val cookies = Companion.cookies
    override val userAgent = USER_AGENT

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

    companion object {
        // Bypass consent page https://stackoverflow.com/a/78115353
        val cookies = ConstantCookiesStorage(
            Cookie(
                name = "CONSENT",
                value = "PENDING+987",
                domain = "www.google.com",
            ),
            @Suppress("SpellCheckingInspection")
            Cookie(
                name = "SOCS",
                value = "CAESHAgBEhJnd3NfMjAyMzA4MTAtMF9SQzIaAmRlIAEaBgiAo_CmBg",
                domain = "www.google.com",
            ),
        )

        // Set custom User-Agent, so that we don't receive Google Lite HTML, which doesn't contain coordinates in
        // case of Google Maps or maps link in case of Google Search.
        const val USER_AGENT = DESKTOP_USER_AGENT
    }
}
