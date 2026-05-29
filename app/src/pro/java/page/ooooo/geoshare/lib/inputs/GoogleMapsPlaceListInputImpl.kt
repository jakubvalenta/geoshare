package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not available in this build flavor.
 */
@Singleton
class GoogleMapsPlaceListInputImpl @Inject constructor() : GoogleMapsPlaceListInput, NoopInput {
    override fun getMessage(resources: Resources) =
        resources.getString(R.string.conversion_failed_unsupported_source_place_list) // TODO Test

    override fun toString() = "GoogleMapsPlaceListInput"
}
