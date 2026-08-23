package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This input is not available in this build flavor.
 *
 * It defaults to URI parsing like [GoogleMapsUriInput] does, and shows a warning if no points were found.
 */
@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor(
    private val uriQuote: UriQuote,
) : GoogleMapsHtmlInput, BasicInput<Uri> {
    override suspend fun fetch(match: String, block: suspend (Uri) -> ParseResult) =
        block(Uri.parse(match, uriQuote))

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        // Default to URI parsing
        val googleMapsParseResult = GoogleMapsUriParser.parse(data)
        points = googleMapsParseResult.points

        // Show a warning if no points were found
        if (points.isEmpty()) {
            warningMessage = resources.getString(R.string.conversion_failed_unsupported_source)
        }
    }

    override fun toString() = "GoogleMapsHtmlInput"
}
