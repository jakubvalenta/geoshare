package page.ooooo.geoshare.data.outputs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.openFileWithChooser
import page.ooooo.geoshare.data.android.openUriWithChooser
import page.ooooo.geoshare.data.android.writeToContentProvider
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.uri.UriQuote
import java.io.File

sealed interface CopyPointTextOutput :
    PointOutput.WithoutLocation,
    Output.HasSuccessText,
    Output.HasAutomationSuccessText {

    val uriQuote: UriQuote

    fun getText(value: Point): String? = null

    override fun isEnabled(value: Point) =
        !getText(value).isNullOrEmpty()

    override suspend fun execute(value: Point, context: Context) =
        getText(value)?.let { text ->
            context.getSystemService<ClipboardManager>()
                ?.setPrimaryClip(ClipData.newPlainText(null, text))
                ?.let { true }
        }
        .toActionResult()

    override fun getDescription(value: Point) =
        getText(value)

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.content_copy_24px)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.copying_finished)
}

sealed interface OpenPointUriOutput :
    PointOutput.WithoutLocation,
    Output.HasActivity<UriActivity>,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    override val activity: UriActivity

    fun getUriString(value: Point): String? = null

    override suspend fun execute(value: Point, context: Context) =
        getUriString(value)
            ?.let { uriString -> activity.launch(context, uriString) }
            .toActionResult(openedApp = true)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetails[activity.packageName]?.label.orEmpty(),
        )

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_failed,
            appDetails[activity.packageName]?.label.orEmpty(),
        )

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetails[activity.packageName]?.label.orEmpty(),
            counterSec,
        )
}

sealed interface OpenPointsFileOutput :
    PointsOutput.WithoutLocation,
    Output.HasActivity<FileActivity>,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    override val activity: FileActivity
    val log: Log

    fun write(value: Points, writer: Appendable)

    override suspend fun execute(value: Points, context: Context) =
        File(context.filesDir, "points")
            .deleteAllAndWriteFile("${System.currentTimeMillis()}.gpx") {
                write(value, this)
            }
            ?.let { file -> activity.launch(context, file, log) }
            .toActionResult(openedApp = true)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.route_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            appDetails[activity.packageName]?.label.orEmpty(),
        )

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_open_app_failed,
            appDetails[activity.packageName]?.label.orEmpty(),
        )

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appDetails[activity.packageName]?.label.orEmpty(),
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

    override suspend fun execute(uri: Uri, value: Point, context: Context) = withContext(Dispatchers.IO) {
        context.writeToContentProvider(uri) {
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

    override suspend fun execute(uri: Uri, value: Points, context: Context) = withContext(Dispatchers.IO) {
        context.writeToContentProvider(uri) {
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

    fun getUriString(value: Point): String? = null

    override suspend fun execute(value: Point, context: Context) =
        getUriString(value)
            ?.let { uriString -> context.openUriWithChooser(uriString) }
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

    override suspend fun execute(value: Points, context: Context) =
        File(context.filesDir, "points")
            .deleteAllAndWriteFile("${System.currentTimeMillis()}.gpx") {
                write(value, this)
            }
            ?.let { file -> context.openFileWithChooser(file) }
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
