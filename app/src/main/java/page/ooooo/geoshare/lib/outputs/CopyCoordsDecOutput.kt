package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject

class CopyCoordsDecOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        CoordinateFormatter.formatDecCoords(coordinateConverter.toWGS84(value))

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_coordinates)

    override fun getAutomationDescription() = @Composable {
        getText(WGS84Point.example)
    }

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyCoordsDecOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
