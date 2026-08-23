package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shows warning that Google Search URIs are not supported.
 */
@Singleton
class GoogleSearchUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput {
    override val pattern =
        Regex("""((?:https?://)?(?:(?:www\.)?google.com/(?:search|share)$URI_REST|share\.google[/?#]$URI_REST))""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        warningMessage = resources.getString(R.string.input_google_search_error_not_supported)
    }

    override fun toString() = "GoogleSearchUriInput"
}
