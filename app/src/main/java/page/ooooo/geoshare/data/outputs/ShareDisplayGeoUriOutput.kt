package page.ooooo.geoshare.data.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.formatters.GeoUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class ShareDisplayGeoUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    private val uriQuote: UriQuote,
) : SharePointUriOutput {
    override val id = "ShareDisplayGeoUriOutput"

    override fun getUriString(value: Point) =
        GeoUriFormatter.formatGeoUriString(coordinateConverter.toWGS84(value), uriQuote = uriQuote)

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.share)

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    override fun getIcon(appDetail: AppDetail?) =
        ImageVectorIconDescriptor(Icons.Default.Share)

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.share)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareDisplayGeoUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
