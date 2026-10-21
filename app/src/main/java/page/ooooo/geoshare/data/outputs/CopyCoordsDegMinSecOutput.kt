package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.formatters.CoordinateFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class CopyCoordsDegMinSecOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    override val uriQuote: UriQuote,
) : CopyPointTextOutput {
    override val id = "CopyCoordsDegMinSecOutput"

    override fun getText(value: Point) =
        CoordinateFormatter.formatDegMinSecCoords(coordinateConverter.toWGS84(value))

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_copy_coordinates)

    @Composable
    override fun automationDescription() =
        CoordinateFormatter.formatDegMinSecCoords(WGS84Point.Kilimanjaro)

    @Composable
    override fun automationSuccessText(appDetail: AppDetail?) =
        stringResource(R.string.conversion_automation_copy_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyCoordsDegMinSecOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
