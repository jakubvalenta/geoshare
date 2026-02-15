package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.GoogleMapsUriFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

/**
 * This output creates a 'google.navigation:' URI, which many apps support to launch navigation, and opens it in
 * [packageName].
 */
data class OpenNavigationGoogleUriOutput(override val packageName: String) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        GoogleMapsUriFormat.formatNavigationUriString(value, uriQuote)

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
}
