package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.PlusCodeFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject

class CopyPlusCodeUriOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        PlusCodeFormatter.formatGoogleMapsPlusCodeUri(
            coordinateConverter.toGCJ02MainlandChina(value),
            uriQuote = uriQuote,
        )

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_copy_link,
            stringResource(R.string.converter_plus_code_name),
        )

    override fun getAutomationDescription() =
        getText(WGS84Point(NaivePoint.example))?.let { @Composable { it } }

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_link_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyPlusCodeUriOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
