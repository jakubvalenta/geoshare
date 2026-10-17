package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R

/**
 * Not available in this build flavor.
 */
object GoogleMapsPlaceListInputImpl : GoogleMapsPlaceListInput, BasicOfflineInput {
    override val group = GOOGLEInputGroup_MAPS

    override fun parse(match: String, resources: Resources): ParseResult =
        parseResult {
            warningMessage = resources.getString(R.string.conversion_failed_unsupported_source_place_list)
        }

    override fun toString() = "GoogleMapsPlaceListInput"
}
