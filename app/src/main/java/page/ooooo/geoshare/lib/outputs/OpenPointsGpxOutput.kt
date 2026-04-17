package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Points
import javax.inject.Inject

class OpenPointsGpxOutput @Inject constructor(
    override val packageName: String,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointsOutput {
    override fun writePoints(value: Points, writer: Appendable) {
        GpxFormatter.writeGpxPoints(coordinateConverter.toWGS84(value), writer)
    }

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_points_open)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.output_gpx_points_open_in,
            appDetails[packageName]?.label ?: packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenPointsGpxOutput
        return packageName == other.packageName
    }

    override fun hashCode() = packageName.hashCode()
}
