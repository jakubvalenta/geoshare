package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Points
import javax.inject.Inject

class OpenPointsGpxOutput @Inject constructor(
    override val activity: FileActivity,
    private val coordinateConverter: CoordinateConverter,
    override val log: Log,
) : OpenPointsFileOutput {
    override val id = "OpenPointsGpxOutput(activity=$activity)"

    override fun write(value: Points, writer: Appendable) {
        GpxFormatter.writeGpxPoints(coordinateConverter.toWGS84(value), writer)
    }

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.output_gpx_points_open)

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.output_gpx_points_open_in, appDetail?.label.orEmpty())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenPointsGpxOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
