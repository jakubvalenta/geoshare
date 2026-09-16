package page.ooooo.geoshare.lib.outputs

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.getTimestamp
import javax.inject.Inject

class SaveRouteGpxOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) : SavePointsFileOutput {
    override val id = "SaveRouteGpxOutput"

    override fun getFilename(resources: Resources) =
        resources.getString(
            R.string.conversion_succeeded_save_gpx_filename,
            resources.getString(R.string.app_name),
            getTimestamp(),
        )

    override val mimeType = "text/xml"

    override fun write(value: Points, writer: Appendable) {
        GpxFormatter.writeGpxRoute(coordinateConverter.toWGS84(value), writer)
    }

    @Composable
    override fun label(appLabel: String?) =
        stringResource(R.string.output_gpx_route_save)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is SaveRouteGpxOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
