package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

/**
 * This output creates a Cartes IGN URL and opens it in [activity].
 *
 * We need this output, because Cartes IGN doesn't properly support geo: URIs.
 */
class OpenDisplayCartesIGNUrlOutput @Inject constructor(
    override val activity: UriActivity,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointUriOutput {
    override val id = "OpenDisplayCartesIGNUrlOutput(activity=$activity)"

    override fun getUriString(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            coordinateConverter.toWGS84(value),
            "https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}",
            uriQuote = uriQuote,
        )

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.output_open_display)

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_open_app_display, appDetail?.label.orEmpty())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenDisplayCartesIGNUrlOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
