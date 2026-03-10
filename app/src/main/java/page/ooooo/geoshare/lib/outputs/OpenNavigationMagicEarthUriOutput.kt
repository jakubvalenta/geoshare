package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.MagicEarthUriFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

/**
 * This output creates a 'magicearth:' navigation URI and opens it in [packageName].
 *
 * We need this output, because Magic Earth doesn't properly support google.navigation: URIs.
 */
data class OpenNavigationMagicEarthUriOutput(override val packageName: String) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        MagicEarthUriFormat.formatNavigationUriString(value, uriQuote)

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
