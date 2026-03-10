package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.MagicEarthUriFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

/**
 * This output creates a 'magicearth:' URI and opens it in [packageName].
 *
 * We need this output, because Magic Earth doesn't properly support geo: URIs.
 */
data class OpenDisplayMagicEarthUriOutput(override val packageName: String) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        MagicEarthUriFormat.formatDisplayUriString(value, uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_display)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_display,
            appDetails[packageName]?.label ?: packageName,
        )
}
