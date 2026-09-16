package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Points
import javax.inject.Inject

class ShareRouteGpxOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : SharePointsFileOutput {
    override val id = "ShareRouteGpxOutput"

    override fun write(value: Points, writer: Appendable) {
        GpxFormatter.writeGpxRoute(coordinateConverter.toWGS84(value), writer)
    }

    @Composable
    override fun label(appLabel: String?) =
        stringResource(R.string.output_gpx_route_share)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is ShareRouteGpxOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
