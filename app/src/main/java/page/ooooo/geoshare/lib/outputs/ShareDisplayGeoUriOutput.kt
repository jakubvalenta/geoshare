package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ImageVectorIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

class ShareDisplayGeoUriOutput @Inject constructor(
    private val geoUriFormatter: GeoUriFormatter,
) : SharePointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        geoUriFormatter.formatGeoUriString(value, uriQuote = uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_share)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    override fun getIcon(appDetails: AppDetails) =
        ImageVectorIconDescriptor(Icons.Default.Share)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_share)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareDisplayGeoUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
