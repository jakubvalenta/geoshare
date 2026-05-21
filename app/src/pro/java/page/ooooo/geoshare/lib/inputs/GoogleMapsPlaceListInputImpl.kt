package page.ooooo.geoshare.lib.inputs

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not available in this build flavor.
 */
@Singleton
class GoogleMapsPlaceListInputImpl @Inject constructor() : GoogleMapsPlaceListInput, NoopInput {
    override fun toString() = "GoogleMapsHtmlInput"
}
