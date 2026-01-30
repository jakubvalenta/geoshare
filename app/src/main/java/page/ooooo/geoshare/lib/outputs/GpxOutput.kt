package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.lib.point.toWGS84
import page.ooooo.geoshare.lib.point.writeGpxRoute
import page.ooooo.geoshare.ui.components.AppIcon
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Locale

object GpxOutput : Output {

    open class ShareGpxRouteWithAppAction(override val packageName: String) :
        LocationAction,
        Action.HasErrorMessage,
        Action.HasPackageName {

        override suspend fun runAction(
            points: ImmutableList<Point>,
            i: Int?,
            location: Point,
            context: Context,
            clipboard: Clipboard,
            resources: Resources,
            saveGpxLauncher: ActivityResultLauncher<Intent>,
            uriQuote: UriQuote,
        ): Boolean {
            val file = writeGpxRoute(points, i, location, context.filesDir) ?: return false
            return AndroidTools.openAppFile(context, packageName, file)
        }

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                )
            )
        }

        @Composable
        override fun permissionText() = stringResource(
            R.string.output_gpx_location_permission,
            stringResource(R.string.app_name),
        )

        @Composable
        override fun errorText(): String = stringResource(
            R.string.conversion_succeeded_open_app_failed,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )
    }

    open class ShareGpxRouteAction : LocationAction, Action.HasErrorMessage {
        override suspend fun runAction(
            points: ImmutableList<Point>,
            i: Int?,
            location: Point,
            context: Context,
            clipboard: Clipboard,
            resources: Resources,
            saveGpxLauncher: ActivityResultLauncher<Intent>,
            uriQuote: UriQuote,
        ): Boolean {
            val file = writeGpxRoute(points, i, location, context.filesDir) ?: return false
            return AndroidTools.openChooserFile(context, file)
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.output_gpx_route_share))
        }

        @Composable
        override fun permissionText() = stringResource(
            R.string.output_gpx_location_permission,
            stringResource(R.string.app_name),
        )

        @Composable
        override fun errorText() = stringResource(R.string.output_gpx_route_share_failed)
    }

    open class SaveGpxPointsAction : BasicAction, Action.HasErrorMessage {
        override suspend fun runAction(
            points: ImmutableList<Point>,
            i: Int?,
            context: Context,
            clipboard: Clipboard,
            resources: Resources,
            saveGpxLauncher: ActivityResultLauncher<Intent>,
            uriQuote: UriQuote,
        ): Boolean {
            @Suppress("SpellCheckingInspection")
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(System.currentTimeMillis())
            val filename = resources.getString(
                R.string.conversion_succeeded_save_gpx_filename,
                resources.getString(R.string.app_name),
                timestamp,
            )
            return try {
                saveGpxLauncher.launch(
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/xml"
                        putExtra(Intent.EXTRA_TITLE, filename)
                    },
                )
                true
            } catch (e: Exception) {
                Log.e(null, "Error when saving GPX file", e)
                false
            }
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_save_gpx))
        }

        override fun getIcon() = @Composable {
            Icon(painterResource(R.drawable.file_save_24px), null)
        }

        @Composable
        override fun errorText() = stringResource(R.string.conversion_succeeded_save_gpx_failed)
    }

    data class ShareGpxRouteWithAppAutomation(override val packageName: String) :
        ShareGpxRouteWithAppAction(packageName),
        Action.HasSuccessMessage,
        LocationAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GPX_ROUTE
        override val testTag = "geoShareUserPreferenceAutomationShareGpxWithApp"

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                )
            )
        }

        override fun getIcon() = @Composable {
            AppIcon(packageName)
        }

        @Composable
        override fun successText() = stringResource(
            R.string.conversion_automation_open_app_succeeded,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun errorText() = stringResource(
            R.string.conversion_automation_open_app_failed,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun waitingText(counterSec: Int) = pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    object ShareGpxRouteAutomation :
        ShareGpxRouteAction(),
        Action.HasSuccessMessage,
        LocationAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.SHARE_GPX_ROUTE
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.output_gpx_route_share_automation_succeeded)

        @Composable
        override fun waitingText(counterSec: Int) =
            pluralStringResource(R.plurals.output_gpx_route_share_automation_waiting, counterSec, counterSec)
    }

    object SaveGpxPointsAutomation :
        SaveGpxPointsAction(),
        BasicAutomation,
        Action.HasSuccessMessage,
        Automation.HasDelay {

        override val type = Automation.Type.SAVE_GPX
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_save_gpx))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_save_gpx_succeeded)

        @Composable
        override fun errorText() = stringResource(R.string.conversion_succeeded_save_gpx_failed)

        @Composable
        override fun waitingText(counterSec: Int) =
            pluralStringResource(R.plurals.conversion_automation_save_gpx_waiting, counterSec, counterSec)
    }

    override fun getPositionActions(): List<Action> = listOf(
        ShareGpxRouteAction(),
        SaveGpxPointsAction(),
    )

    override fun getPointActions(): List<Action> = listOf(
        ShareGpxRouteAction(),
        SaveGpxPointsAction(),
    )

    override fun getAppActions(apps: List<AndroidTools.App>) =
        apps.filter { it.type == AndroidTools.AppType.GPX }
            .map { it.packageName to ShareGpxRouteWithAppAction(it.packageName) }

    override fun getChipActions() = listOf(SaveGpxPointsAction())

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = buildList {
        add(ShareGpxRouteAutomation)
        add(SaveGpxPointsAutomation)
        apps.filter { it.type == AndroidTools.AppType.GPX }
            .forEach { add(ShareGpxRouteWithAppAutomation(it.packageName)) }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.OPEN_APP_GPX_ROUTE if packageName != null -> ShareGpxRouteWithAppAutomation(packageName)
        Automation.Type.SAVE_GPX -> SaveGpxPointsAutomation
        Automation.Type.SHARE_GPX_ROUTE -> ShareGpxRouteAutomation
        else -> null
    }

    fun writeGpxRoute(points: ImmutableList<Point>, i: Int?, location: Point?, parentDir: File): File? {
        if (location == null) {
            return null
        }
        val route = if (i == null && points.size > 1) {
            points.toMutableList().apply { add(0, location) }.toImmutableList()
        } else {
            val point = points.getOrNull(i) ?: return null
            persistentListOf(location, point)
        }
        val dir = File(parentDir, "routes")
        dir.deleteRecursively()
        try {
            dir.mkdirs()
        } catch (_: SecurityException) {
            return null
        }
        val timestamp = System.currentTimeMillis()
        val file = File(dir, "$timestamp.xml")
        try {
            file.printWriter().use { writer ->
                route.toWGS84().writeGpxRoute(writer)
            }
        } catch (_: FileNotFoundException) {
            return null
        }
        return file
    }
}
