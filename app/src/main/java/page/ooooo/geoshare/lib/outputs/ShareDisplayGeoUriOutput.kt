package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.GeoUriFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.ImageVectorIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

object ShareDisplayGeoUriOutput : SharePointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        GeoUriFormat.formatGeoUriString(value, uriQuote = uriQuote)

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
}
