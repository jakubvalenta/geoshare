package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

/**
 * This output creates a 'google.navigation:' URI, which many apps support to launch navigation, and opens it in
 * [activity].
 */
class OpenNavigationGoogleUriOutput @Inject constructor(
    override val activity: UriActivity,
    private val coordinateConverter: CoordinateConverter,
    private val uriQuote: UriQuote,
) : OpenPointUriOutput {
    override val id = "OpenNavigationGoogleUriOutput(activity=$activity)"

    override fun getUriString(value: Point) =
        GoogleMapsUriFormatter.formatNavigationUriString(
            coordinateConverter.toSrs(value, PackageNames.getSrs(activity.packageName)),
            uriQuote,
        )

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.output_open_navigation)

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_open_app_navigate_to, appDetail?.label.orEmpty())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenNavigationGoogleUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
