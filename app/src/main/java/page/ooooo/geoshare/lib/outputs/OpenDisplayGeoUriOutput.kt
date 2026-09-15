package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * Creates a geo: URI and opens it in [activity]. It's the most important of all outputs.
 */
class OpenDisplayGeoUriOutput @Inject constructor(
    override val activity: UriActivity,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointUriOutput {
    override val id = "OpenDisplayGeoUriOutput(activity=$activity)"

    override fun getUriString(value: Point, uriQuote: UriQuote) =
        GeoUriFormatter.formatGeoUriString(
            coordinateConverter.toSrs(value, PackageNames.getSrs(activity.packageName)),
            PackageNames.getGeoUriFlavor(activity.packageName),
            uriQuote,
        )

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_display)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_display,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenDisplayGeoUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
