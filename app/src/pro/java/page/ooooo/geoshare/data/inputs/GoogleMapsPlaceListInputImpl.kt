package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R

/**
 * This input is not available in this build flavor.
 *
 * It shows a warning.
 */
object GoogleMapsPlaceListInputImpl : GoogleMapsPlaceListInput, BasicOfflineInput {
    override val group = GOOGLEInputGroup_MAPS

    override fun parse(match: String, resources: Resources) =
        parseResult {
            warningMessage = resources.getString(R.string.conversion_failed_unsupported_source_place_list)
        }

    override fun toString() = "GoogleMapsPlaceListInput"
}
