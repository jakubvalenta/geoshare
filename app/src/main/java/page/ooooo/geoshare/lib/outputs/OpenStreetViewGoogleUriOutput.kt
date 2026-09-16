package page.ooooo.geoshare.lib.outputs


import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * This output creates a 'google.streetview:' URI, which some apps support to launch street view, and opens it in
 * [activity].
 */
class OpenStreetViewGoogleUriOutput @Inject constructor(
    override val activity: UriActivity,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointUriOutput {
    override val id = "OpenStreetViewGoogleUriOutput(activity=$activity)"

    override fun getUriString(value: Point, uriQuote: UriQuote) =
        GoogleMapsUriFormatter.formatStreetViewUriString(
            coordinateConverter.toSrs(value, PackageNames.getSrs(activity.packageName)),
            uriQuote,
        )

    @Composable
    override fun label() =
        stringResource(R.string.output_open_street_view)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.streetview_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_street_view,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenStreetViewGoogleUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
