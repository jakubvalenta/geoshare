package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

class ShareStreetViewGoogleUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : SharePointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        GoogleMapsUriFormatter.formatStreetViewUriString(coordinateConverter.toWGS84(value), uriQuote = uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_street_view)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.streetview_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(R.string.output_open_street_view)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareStreetViewGoogleUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
