package page.ooooo.geoshare.lib.inputs

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not available in this build flavor.
 */
@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor() : GoogleMapsHtmlInput, NoopInput {
    override val group = InputGroup.GOOGLE_MAPS

    override fun toString() = "GoogleMapsHtmlInput"
}
