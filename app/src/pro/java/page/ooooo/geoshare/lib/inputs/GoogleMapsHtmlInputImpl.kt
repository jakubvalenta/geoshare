package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not available in this build flavor.
 */
@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor(
    private val uriQuote: UriQuote,
) : GoogleMapsHtmlInput, BasicInput<Uri> {
    override suspend fun fetch(match: String, block: suspend (Uri) -> ParseResult) =
        block(Uri.parse(match, uriQuote))

    override suspend fun parse(data: Uri, match: String) = parseResult {
        // Parse URI, so that we return at least points with names
        val googleMapsParseResult = GoogleMapsUriParser.parse(data)
        points = googleMapsParseResult.points
    }

    override fun getErrorMessage(resources: Resources) =
        resources.getString(R.string.conversion_failed_unsupported_source)

    override fun toString() = "GoogleMapsHtmlInput"
}
