package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.deleteAllAndWriteFile
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import java.io.File
import javax.inject.Inject

/**
 * When executed, this output creates a GPX route starting at current device location and opens it in [activity].
 *
 * It's only useful for TomTom, because TomTom doesn't support geo: URIs.
 */
class OpenRouteOnePointGpxOutput @Inject constructor(
    override val activity: FileActivity,
    private val coordinateConverter: CoordinateConverter,
    private val log: Log,
) :
    PointOutput.WithLocation,
    Output.HasActivity<FileActivity>,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    override val id = "OpenRouteOnePointGpxOutput(activity=$activity)"

    fun write(location: Point, value: Point, writer: Appendable) {
        GpxFormatter.writeGpxRoute(coordinateConverter.toWGS84(persistentListOf(location, value)), writer)
    }

    override suspend fun execute(location: Point?, value: Point, actionContext: ActionContext) =
        location?.let { location ->
            File(actionContext.context.filesDir, "routes")
                // Don't use a .gpx extension but use .xml instead, because that's what TomTom requires
                .deleteAllAndWriteFile("${System.currentTimeMillis()}.xml") {
                    write(location, value, this)
                }
                ?.let { file -> activity.launch(actionContext.context, file, log) }
        }
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.output_open_navigation)

    override fun getIcon(appDetail: AppDetail?) =
        appDetail?.let { DrawableIconDescriptor(it.icon) }

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun permissionText() = stringResource(
        R.string.output_gpx_location_permission,
        stringResource(R.string.app_name),
    )

    @Composable
    override fun errorText(appDetail: AppDetail?) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetail?.label.orEmpty(),
        )

    @Composable
    override fun automationLabel(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_open_app_navigate_to, appDetail?.label.orEmpty())

    @Composable
    override fun automationErrorText(appDetail: AppDetail?) =
        stringResource(R.string.conversion_automation_open_app_failed, appDetail?.label.orEmpty())

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetail: AppDetail?) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetail?.label.orEmpty(),
            counterSec,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenRouteOnePointGpxOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
