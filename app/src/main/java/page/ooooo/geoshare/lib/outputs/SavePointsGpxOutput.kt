package page.ooooo.geoshare.lib.outputs

import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.getTimestamp
import page.ooooo.geoshare.lib.geo.Points
import javax.inject.Inject

class SavePointsGpxOutput @Inject constructor(
    private val gpxFormatter: GpxFormatter,
) : PointsOutput.WithFile, SaveFileOutput {
    override fun getFilename(resources: Resources) =
        resources.getString(
            R.string.conversion_succeeded_save_gpx_filename,
            resources.getString(R.string.app_name),
            getTimestamp(),
        )

    override val mimeType = "text/xml"

    override suspend fun execute(uri: Uri, value: Points, actionContext: ActionContext) = withContext(Dispatchers.IO) {
        AndroidTools.openFileUri(actionContext.context, uri) {
            gpxFormatter.writeGpxPoints(value, this)
        }
    }

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_save_gpx)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is SavePointsGpxOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
