package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
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
) : GoogleMapsHtmlInput, BasicOfflineInput {
    override val group = InputGroup.GOOGLE_MAPS

    override fun parse(match: String, resources: Resources) =
        parseResult {
            // Default to URI parsing
            val uri = Uri.parse(match, uriQuote)
            val googleMapsParseResult = GoogleMapsUriParser.parse(uri)
            points = googleMapsParseResult.points

            // Show a warning if no points were found
            if (points.isEmpty()) {
                warningMessage = resources.getString(R.string.conversion_failed_unsupported_source)
            }
        }

    override fun toString() = "GoogleMapsHtmlInput"
}
