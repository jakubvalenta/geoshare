package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * This output creates a Cartes IGN URL and opens it in [packageName].
 *
 * We need this output, because Cartes IGN doesn't properly support geo: URIs.
 */
class OpenDisplayCartesIGNUrlOutput @Inject constructor(
    override val packageName: String,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            coordinateConverter.toWGS84(value),
            "https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}",
            uriQuote = uriQuote,
        )

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenDisplayCartesIGNUrlOutput
        return packageName == other.packageName
    }

    override fun hashCode() = packageName.hashCode()
}
