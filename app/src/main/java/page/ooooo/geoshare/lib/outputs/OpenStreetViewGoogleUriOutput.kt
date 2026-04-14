package page.ooooo.geoshare.lib.outputs


import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * This output creates a 'google.streetview:' URI, which some apps support to launch street view, and opens it in
 * [packageName].
 */
class OpenStreetViewGoogleUriOutput @Inject constructor(
    override val packageName: String,
    private val googleMapsUriFormatter: GoogleMapsUriFormatter,
) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        googleMapsUriFormatter.formatStreetViewUriString(value, packageName, uriQuote = uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_street_view)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.streetview_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_street_view,
            appDetails[packageName]?.label ?: packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenStreetViewGoogleUriOutput
        return packageName == other.packageName
    }

    override fun hashCode() = packageName.hashCode()
}
