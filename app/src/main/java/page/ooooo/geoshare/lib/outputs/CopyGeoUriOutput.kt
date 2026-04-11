package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import javax.inject.Inject

class CopyGeoUriOutput @Inject constructor(
    private val geoUriFormatter: GeoUriFormatter,
) : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        geoUriFormatter.formatGeoUriString(value, uriQuote = uriQuote)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_link, stringResource(R.string.converter_geo_name))

    override fun getIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.language_24px)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.location_on_24px)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_link_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyGeoUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
