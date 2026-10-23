package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.formatters.GpxFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.log.Log
import javax.inject.Inject

class OpenRouteGpxOutput @Inject constructor(
    override val activity: FileActivity,
    private val coordinateConverter: CoordinateConverter,
    override val log: Log,
) : OpenPointsFileOutput {
    override val id = "OpenRouteGpxOutput(activity=$activity)"

    override fun write(value: Points, writer: Appendable) {
        GpxFormatter.writeGpxRoute(coordinateConverter.toWGS84(value), writer)
    }

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_open)

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_open_in, appDetails[activity.packageName]?.label.orEmpty())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenRouteGpxOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
