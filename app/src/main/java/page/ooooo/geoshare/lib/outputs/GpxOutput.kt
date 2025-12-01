package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.AndroidTools.queryAppDetails
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.LocalSpacing
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.seconds

object GpxOutput : Output {

    open class ShareGpxRouteWithAppAction(override val packageName: String) :
        LocationAction,
        Action.HasErrorMessage,
        Action.HasPackageName {

        override suspend fun runAction(
            position: Position,
            i: Int?,
            location: Point?,
            context: Context,
            clipboard: Clipboard,
            resources: Resources,
            saveGpxLauncher: ActivityResultLauncher<Intent>,
            uriQuote: UriQuote,
        ): Boolean {
            val uri = writeGpxRoute(position, i, location, context) ?: return false
            return AndroidTools.openAppFile(context, packageName, uri)
        }

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app,
                    queryAppDetails()?.label ?: packageName,
                ),
            )
        }

        @Composable
        override fun permissionText() = stringResource(
            R.string.output_gpx_location_permission,
            queryAppDetails()?.label ?: packageName,
            stringResource(R.string.app_name),
        )

        @Composable
        override fun errorText(): String = stringResource(R.string.output_gpx_route_share_failed)

        private var appDetailsCache: AndroidTools.AppDetails? = null

        @Composable
        protected fun queryAppDetails(): AndroidTools.AppDetails? =
            appDetailsCache ?: queryAppDetails(LocalContext.current.packageManager, packageName)
                ?.also { appDetailsCache = it }
    }

    open class ShareGpxRouteAction : LocationAction, Action.HasErrorMessage {
        override suspend fun runAction(
            position: Position,
            i: Int?,
            location: Point?,
            context: Context,
            clipboard: Clipboard,
            resources: Resources,
            saveGpxLauncher: ActivityResultLauncher<Intent>,
            uriQuote: UriQuote,
        ): Boolean {
            val uri = writeGpxRoute(position, i, location, context) ?: return false
            return AndroidTools.openChooserFile(context, uri)
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.output_gpx_route_share))
        }

        @Composable
        override fun permissionText() = stringResource(
            R.string.output_gpx_route_share_permission_text,
            stringResource(R.string.app_name),
        )

        @Composable
        override fun errorText() = stringResource(R.string.output_gpx_route_share_failed)
    }

    open class SaveGpxPointsAction : BasicAction, Action.HasErrorMessage {
        override suspend fun runAction(
            position: Position,
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

        @Composable
        override fun errorText() = stringResource(R.string.conversion_succeeded_save_gpx_failed)
    }

    data class ShareGpxRouteWithAppAutomation(override val packageName: String) :
        ShareGpxRouteWithAppAction(packageName),
        Action.HasSuccessMessage,
        LocationAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GPX_ROUTE
        override val testTag = null
        override val delay = 5.seconds

        @Composable
        override fun Label() {
            val spacing = LocalSpacing.current
            queryAppDetails()?.let { appDetails ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        rememberDrawablePainter(appDetails.icon),
                        appDetails.label,
                        Modifier.widthIn(max = 24.dp),
                    )
                    Text(
                        stringResource(
                            R.string.conversion_succeeded_open_app_navigate_to,
                            queryAppDetails()?.label ?: packageName
                        )
                    )
                }
            } ?: Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, packageName))
        }

        @Composable
        override fun successText() = stringResource(
            R.string.conversion_automation_open_app_succeeded,
            queryAppDetails()?.label ?: packageName,
        )

        @Composable
        override fun errorText() = stringResource(
            R.string.conversion_automation_open_app_failed,
            queryAppDetails()?.label ?: packageName,
        )

        @Composable
        override fun waitingText(counterSec: Int) = stringResource(
            R.string.conversion_automation_open_app_waiting,
            queryAppDetails()?.label ?: packageName,
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
        override val delay = 5.seconds

        @Composable
        override fun successText() = stringResource(R.string.output_gpx_route_share_automation_succeeded)

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(R.string.output_gpx_route_share_automation_waiting, counterSec)
    }

    object SaveGpxPointsAutomation :
        SaveGpxPointsAction(),
        BasicAutomation,
        Action.HasSuccessMessage,
        Automation.HasDelay {

        override val type = Automation.Type.SAVE_GPX
        override val packageName = ""
        override val testTag = null
        override val delay = 5.seconds

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
            stringResource(R.string.conversion_automation_save_gpx_waiting, counterSec)
    }

    override fun getPositionActions(): List<Action> = listOf(
        SaveGpxPointsAction(),
        ShareGpxRouteAction(),
    )

    override fun getPointActions(): List<Action> = listOf(
        SaveGpxPointsAction(),
        ShareGpxRouteAction(),
    )

    override fun getAppActions(apps: List<AndroidTools.App>) =
        apps.filter { it.type == AndroidTools.AppType.GPX }
            .map { it.packageName to ShareGpxRouteWithAppAction(it.packageName) }

    override fun getChipActions() = listOf(SaveGpxPointsAction())

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = buildList {
        add(SaveGpxPointsAutomation)
        add(ShareGpxRouteAutomation)
        apps.filter { it.type == AndroidTools.AppType.GPX }
            .forEach { add(ShareGpxRouteWithAppAutomation(it.packageName)) }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.OPEN_APP_GPX_ROUTE if packageName != null -> ShareGpxRouteWithAppAutomation(packageName)
        Automation.Type.SAVE_GPX -> SaveGpxPointsAutomation
        Automation.Type.SHARE_GPX_ROUTE -> ShareGpxRouteAutomation
        else -> null
    }

    // TODO Test writeGpxRoute()
    private fun writeGpxRoute(position: Position, i: Int?, location: Point?, context: Context): Uri? {
        if (location == null) {
            return null
        }
        val point = position.getPoint(i) ?: return null
        // TODO Check if TomTom waypoints work
        val route = Position(persistentListOf(location, point))
        val dir = File(context.filesDir, "routes")
        dir.deleteRecursively()
        try {
            dir.mkdirs()
        } catch (_: SecurityException) {
            return null
        }
        val timestamp = System.currentTimeMillis()
        val file = File(dir, "$timestamp.xml")
        file.printWriter().use { writer ->
            route.writeGpxRoute(writer)
        }
        val uri = try {
            @Suppress("SpellCheckingInspection")
            FileProvider.getUriForFile(context, "page.ooooo.geoshare.RouteFileProvider", file)
        } catch (e: IllegalArgumentException) {
            Log.e(null, "Error when getting URI for file", e)
            return null
        }
        return uri
    }
}
