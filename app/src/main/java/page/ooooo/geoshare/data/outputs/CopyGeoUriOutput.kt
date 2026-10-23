package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.formatters.GeoUriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class CopyGeoUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    override val uriQuote: UriQuote,
) : CopyPointTextOutput {
    override val id = "CopyGeoUriOutput"

    override fun getText(value: Point) =
        GeoUriFormatter.formatGeoUriString(coordinateConverter.toWGS84(value), uriQuote = uriQuote)

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
