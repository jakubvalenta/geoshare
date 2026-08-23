package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This input is not available in this build flavor.
 *
 * It shows a warning.
 */
@Singleton
class GoogleMapsPlaceListInputImpl @Inject constructor() : GoogleMapsPlaceListInput, BasicInput<String> {
    override suspend fun fetch(match: String, block: suspend (String) -> ParseResult) = block(match)

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        warningMessage = resources.getString(R.string.conversion_failed_unsupported_source_place_list)
    }

    override fun toString() = "GoogleMapsPlaceListInput"
}
