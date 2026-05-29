package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not available in this build flavor.
 */
@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor() : GoogleMapsHtmlInput, NoopInput {
    override fun getErrorMessage(resources: Resources) =
        resources.getString(R.string.conversion_failed_unsupported_source)

    override fun toString() = "GoogleMapsHtmlInput"
}
