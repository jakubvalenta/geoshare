package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class ShareNavigationGoogleUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    private val uriQuote: UriQuote,
) : SharePointUriOutput {
    override val id = "ShareNavigationGoogleUriOutput"

    override fun getUriString(value: Point) =
        GoogleMapsUriFormatter.formatNavigationUriString(coordinateConverter.toWGS84(value), uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_navigation)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(R.string.output_open_navigation)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareNavigationGoogleUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
