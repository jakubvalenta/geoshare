package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.formatters.MagicEarthUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

/**
 * This output creates a 'magicearth:' URI and opens it in [activity].
 *
 * We need this output, because Magic Earth doesn't properly support geo: URIs.
 */
class OpenDisplayMagicEarthUriOutput @Inject constructor(
    override val activity: UriActivity,
    private val coordinateConverter: CoordinateConverter,
    private val uriQuote: UriQuote,
) : OpenPointUriOutput {
    override val id = "OpenDisplayMagicEarthUriOutput(activity=$activity)"

    override fun getUriString(value: Point) =
        MagicEarthUriFormatter.formatDisplayUriString(coordinateConverter.toWGS84(value), uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_display)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_display,
            appDetails[activity.packageName]?.label.orEmpty()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenDisplayMagicEarthUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
