package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * This output creates a 'google.navigation:' URI, which many apps support to launch navigation, and opens it in
 * [packageName].
 */
class OpenNavigationGoogleUriOutput @Inject constructor(
    override val packageName: String,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        GoogleMapsUriFormatter.formatNavigationUriString(
            coordinateConverter.toSrs(value, PackageNames.getSrs(packageName)),
            uriQuote,
        )

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_navigation)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_navigate_to,
            appDetails[packageName]?.label ?: packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenNavigationGoogleUriOutput
        return packageName == other.packageName
    }

    override fun hashCode() = packageName.hashCode()
}
