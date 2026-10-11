package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class ShareNavigationGoogleUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : SharePointUriOutput {
    override val id = "ShareNavigationGoogleUriOutput"

    override fun getUriString(value: Point, uriQuote: UriQuote) =
        GoogleMapsUriFormatter.formatNavigationUriString(coordinateConverter.toWGS84(value), uriQuote)

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.output_open_navigation)

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.output_open_navigation)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareNavigationGoogleUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
