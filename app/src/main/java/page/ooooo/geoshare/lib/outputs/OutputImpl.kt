package page.ooooo.geoshare.lib.outputs

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.copy
import page.ooooo.geoshare.lib.android.writeToContentProvider
import page.ooooo.geoshare.lib.android.openFileWithChooser
import page.ooooo.geoshare.lib.android.openUriWithChooser
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.deleteAllAndWriteFile
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.IconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import java.io.File

sealed interface CopyPointTextOutput :
    PointOutput.WithoutLocation,
    Output.HasSuccessText,
    Output.HasAutomationSuccessText {

    fun getText(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getText(value, actionContext.uriQuote)
            ?.let { text ->
                actionContext.clipboard.copy(text)
                true
            }
            .toActionResult()

    override fun getDescription(value: Point, uriQuote: UriQuote) =
        getText(value, uriQuote)

    override fun getMenuIcon(appDetails: AppDetails): IconDescriptor? =
        ResourceIconDescriptor(R.drawable.content_copy_24px)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.copying_finished)
}

sealed interface OpenPointUriOutput :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    val activity: UriActivity

    fun getUriString(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getUriString(value, actionContext.uriQuote)
            ?.let { uriString -> activity.launch(actionContext.context, uriString) }
            .toActionResult(openedApp = true)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_failed,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetails[activity.packageName]?.label ?: activity.packageName,
            counterSec,
        )
}

sealed interface OpenPointsFileOutput :
    PointsOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    val activity: FileActivity
    val log: Log

    fun write(value: Points, writer: Appendable)

    override suspend fun execute(value: Points, actionContext: ActionContext) =
        File(actionContext.context.filesDir, "points")
            .deleteAllAndWriteFile("${System.currentTimeMillis()}.gpx") {
                write(value, this)
            }
            ?.let { file -> activity.launch(actionContext.context, file, log) }
            .toActionResult(openedApp = true)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.route_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_failed,
            appDetails[activity.packageName]?.label ?: activity.packageName,
        )

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetails[activity.packageName]?.label ?: activity.packageName,
            counterSec,
        )
}

sealed interface SavePointFileOutput :
    PointOutput.WithFile,
    Output.HasErrorText,
    Output.HasSuccessText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    fun write(value: Point, writer: Appendable)

    override suspend fun execute(uri: Uri, value: Point, actionContext: ActionContext) = withContext(Dispatchers.IO) {
        actionContext.context.writeToContentProvider(uri) {
            write(value, this)
        }
            .toActionResult()
    }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.download_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_save_gpx_failed)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_save_gpx_succeeded)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        errorText(appDetails)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_save_gpx_succeeded)

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(R.plurals.conversion_automation_save_gpx_waiting, counterSec, counterSec)
}

sealed interface SavePointsFileOutput :
    PointsOutput.WithFile,
    Output.HasErrorText,
    Output.HasSuccessText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    fun write(value: Points, writer: Appendable)

    override suspend fun execute(uri: Uri, value: Points, actionContext: ActionContext) = withContext(Dispatchers.IO) {
        actionContext.context.writeToContentProvider(uri) {
            write(value, this)
        }
            .toActionResult()
    }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.download_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_save_gpx_failed)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_save_gpx_succeeded)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        errorText(appDetails)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_save_gpx_succeeded)

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(R.plurals.conversion_automation_save_gpx_waiting, counterSec, counterSec)
}

sealed interface SharePointUriOutput :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    fun getUriString(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getUriString(value, actionContext.uriQuote)
            ?.let { uriString -> actionContext.context.openUriWithChooser(uriString) }
            .toActionResult(openedApp = true)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_apps_not_found)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_share_failed)

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(R.plurals.conversion_automation_share_waiting, counterSec, counterSec)
}

sealed interface SharePointsFileOutput :
    PointsOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    fun write(value: Points, writer: Appendable)

    override suspend fun execute(value: Points, actionContext: ActionContext) =
        File(actionContext.context.filesDir, "points")
            .deleteAllAndWriteFile("${System.currentTimeMillis()}.gpx") {
                write(value, this)
            }
            ?.let { file -> actionContext.context.openFileWithChooser(file) }
            .toActionResult(openedApp = true)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.route_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_share_failed)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_share_automation_failed)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.output_gpx_route_share_automation_succeeded)

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(R.plurals.output_gpx_route_share_automation_waiting, counterSec, counterSec)
}
