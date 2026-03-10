package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.GpxFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.writeFile
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.IconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

/**
 * This output creates a GPX route starting at current device location and opens it in [packageName].
 *
 * It's only useful for TomTom, because TomTom doesn't support geo: URIs.
 */
data class OpenRouteOnePointGpxOutput(val packageName: String) :
    PointOutput.WithLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    override suspend fun execute(
        location: Point?,
        value: Point,
        actionContext: ActionContext,
    ) =
        location?.let { location ->
            // Notice that we use the .xml extension, because that's what TomTom requires.
            writeFile(actionContext.context.filesDir, "routes", "${System.currentTimeMillis()}.xml") {
                GpxFormat.writeGpxRoute(persistentListOf(location, value), this)
            }?.let { file ->
                actionContext.androidTools.openAppFile(actionContext.context, packageName, file)
            }
        } ?: false

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_navigation)

    override fun getIcon(appDetails: AppDetails): IconDescriptor? =
        appDetails[packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.navigation_24px)

    @Composable
    override fun permissionText() = stringResource(
        R.string.output_gpx_location_permission,
        stringResource(R.string.app_name),
    )

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetails[packageName]?.label ?: packageName,
        )

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_navigate_to,
            appDetails[packageName]?.label ?: packageName,
        )

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_failed,
            appDetails[packageName]?.label ?: packageName,
        )

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_succeeded,
            appDetails[packageName]?.label ?: packageName,
        )

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetails[packageName]?.label ?: packageName,
            counterSec,
        )
}
