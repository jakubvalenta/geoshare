package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.GpxFormat
import page.ooooo.geoshare.lib.point.Points

object ShareRouteGpxOutput : SharePointsOutput {
    override fun writePoints(value: Points, writer: Appendable) {
        GpxFormat.writeGpxRoute(value, writer)
    }

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_share)
}
