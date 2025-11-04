package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

object GeoUriOutputManager : OutputManager {

    object CopyGeoUriAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GEO_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object ShareGeoUriAutomation : Automation.HasErrorMessage, Automation.HasSuccessMessage, Automation.HasDelay {
        override val type = Automation.Type.SHARE
        override val packageName = ""
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenChooser(formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_share))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_share_succeeded)

        @Composable
        override fun errorText() = stringResource(R.string.conversion_automation_share_failed)

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(R.string.conversion_automation_share_waiting, counterSec)
    }

    @Immutable
    data class OpenAppAutomation(override val packageName: String) :
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP
        override val testTag = "geoShareUserPreferenceAutomationOpenApp_${packageName}"

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            val spacing = LocalSpacing.current
            queryApp()?.let { app ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        rememberDrawablePainter(app.icon),
                        app.label,
                        Modifier.widthIn(max = 24.dp),
                    )
                    Text(stringResource(R.string.conversion_succeeded_open_app, app.label))
                }
            } ?: Text(stringResource(R.string.conversion_succeeded_open_app, packageName))
        }

        @Composable
        override fun successText() =
            stringResource(
                R.string.conversion_automation_open_app_succeeded,
                queryApp()?.label ?: packageName,
            )

        @Composable
        override fun errorText() =
            stringResource(
                R.string.conversion_automation_open_app_failed,
                queryApp()?.label ?: packageName,
            )

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(
                R.string.conversion_automation_open_app_waiting,
                queryApp()?.label ?: packageName,
                counterSec,
            )

        private var appCache: IntentTools.App? = null

        @Composable
        private fun queryApp(): IntentTools.App? =
            appCache ?: IntentTools().queryApp(LocalContext.current.packageManager, packageName)?.also { appCache = it }
    }

    override fun getOutputs(position: Position, packageNames: List<String>, uriQuote: UriQuote) = buildList {
        val uriString = formatUriString(position, uriQuote)
        add(Output.Action(Action.Copy(uriString)) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        })
        add(Output.Action(Action.OpenChooser(uriString)) {
            stringResource(R.string.conversion_succeeded_share)
        })
        packageNames.forEach { packageName ->
            add(Output.AppAction(packageName, Action.OpenApp(packageName, uriString)) {
                stringResource(R.string.conversion_succeeded_open_app, packageName)
            })
        }
        position.points?.forEachIndexed { i, point ->
            val uriString = formatUriString(point, uriQuote = uriQuote)
            add(Output.PointAction(i, Action.Copy(uriString)) {
                stringResource(R.string.conversion_succeeded_copy_geo)
            })
            add(Output.PointAction(i, Action.OpenChooser(uriString)) {
                stringResource(R.string.conversion_succeeded_share)
            })
        }
        add(Output.Chip(Action.Copy(uriString)) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        })
    }

    override fun getAutomations(packageNames: List<String>): List<Automation> = buildList {
        add(CopyGeoUriAutomation)
        add(ShareGeoUriAutomation)
        packageNames.forEach { add(OpenAppAutomation(it)) }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GEO_URI -> CopyGeoUriAutomation
        Automation.Type.SHARE -> ShareGeoUriAutomation
        Automation.Type.OPEN_APP if packageName != null -> OpenAppAutomation(packageName)
        else -> null
    }

    fun formatUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String = position.run {
        formatUriString(mainPoint ?: Point(), q = q, z = z, uriQuote = uriQuote)
    }

    private fun formatUriString(
        point: Point,
        q: String? = null,
        z: String? = null,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): String = point.run {
        Uri(
            scheme = "geo",
            path = "$lat,$lon",
            queryParams = buildMap {
                set("q", q ?: "$lat,$lon")
                z?.let { z ->
                    set("z", z)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
    }
}
