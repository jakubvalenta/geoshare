package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R

/**
 * Shows warning that Google Search URIs are not supported.
 */
object GoogleSearchUriInput : BasicOfflineInput, Input.HasPattern {
    override fun getName(resources: Resources) = resources.getString(R.string.input_google_search_name)
    override val group = InputGroup.GOOGLE_MAPS

    override val pattern =
        Regex("""((?:https?://)?(?:(?:www\.)?google.com/(?:search|share)$URI_REST|share\.google[/?#]$URI_REST))""")

    override fun parse(match: String, resources: Resources) =
        parseResult {
            warningMessage = resources.getString(R.string.conversion_failed_unsupported_source_google_search)
        }

    override fun toString() = "GoogleSearchUriInput"
}
