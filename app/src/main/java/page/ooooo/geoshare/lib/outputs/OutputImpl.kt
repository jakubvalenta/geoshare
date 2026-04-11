package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.writeFile
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.IconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor

sealed interface CopyPointOutput :
    PointOutput.WithoutLocation,
    Output.HasSuccessText,
    Output.HasAutomationSuccessText {

    fun getText(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getText(value, actionContext.uriQuote)?.let { text ->
            actionContext.androidTools.copyToClipboard(actionContext.clipboard, text)
            true
        } ?: false

    override fun getDescription(value: Point, uriQuote: UriQuote) =
        getText(value, uriQuote)

    override fun getMenuIcon(appDetails: AppDetails): IconDescriptor? =
        ResourceIconDescriptor(R.drawable.content_copy_24px)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.copying_finished)
}

sealed interface OpenPointOutput :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    val packageName: String

    fun getText(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext): Boolean =
        getText(value, actionContext.uriQuote)?.let { uriString ->
            actionContext.androidTools.openApp(actionContext.context, packageName, uriString)
        } ?: false

    override fun getIcon(appDetails: AppDetails): IconDescriptor? =
        appDetails[packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
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

sealed interface OpenPointsOutput :
    PointsOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    val packageName: String

    fun writePoints(value: Points, writer: Appendable)

    override suspend fun execute(value: Points, actionContext: ActionContext) =
        writeFile(actionContext.context.filesDir, "points", "${System.currentTimeMillis()}.gpx") {
            writePoints(value, this)
        }?.let { file ->
            actionContext.androidTools.openAppFile(actionContext.context, packageName, file)
        } ?: false

    override fun getIcon(appDetails: AppDetails): IconDescriptor? =
        appDetails[packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.route_24px)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
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

sealed interface SaveFileOutput :
    Output,
    Output.HasErrorText,
    Output.HasSuccessText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

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

sealed interface SharePointOutput :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    fun getText(value: Point, uriQuote: UriQuote = DefaultUriQuote): String? = null

    override suspend fun execute(value: Point, actionContext: ActionContext): Boolean =
        getText(value, actionContext.uriQuote)?.let { uriString ->
            actionContext.androidTools.openChooser(actionContext.context, uriString)
        } ?: false

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_apps_not_found)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_share_failed)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_share_succeeded)

    @Composable
    override fun automationWaitingText(counterSec: Int, appDetails: AppDetails) =
        pluralStringResource(R.plurals.conversion_automation_share_waiting, counterSec)
}

sealed interface SharePointsOutput :
    PointsOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText,
    Output.HasAutomationSuccessText {

    fun writePoints(value: Points, writer: Appendable)

    override suspend fun execute(value: Points, actionContext: ActionContext) =
        writeFile(
            actionContext.context.filesDir, "points", "${System.currentTimeMillis()}.gpx"
        ) {
            writePoints(value, this)
        }?.let { file ->
            actionContext.androidTools.openChooserFile(actionContext.context, file)
        } ?: false

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
