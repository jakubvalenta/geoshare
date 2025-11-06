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
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

object GeoUriOutputGroup : OutputGroup<Position> {

    object CopyOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, Srs.WGS84, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_geo)
    }

    object ChooserOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenChooser(formatUriString(value, Srs.WGS84, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_share)
    }

    @Immutable
    data class AppOutput(override val packageName: String) : Output.App<Position> {
        private val srs get() = if (packageName in GoogleMapsOutputGroup.PACKAGE_NAMES) Srs.GCJ02 else Srs.WGS84

        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatUriString(value, srs, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) = when (srs) {
            is Srs.WGS84 -> stringResource(R.string.conversion_succeeded_open_app, app.label)
            is Srs.GCJ02 -> stringResource(R.string.conversion_succeeded_open_app_srs, app.label, srs.name)
        }
    }

    object ChipOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, Srs.WGS84, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_geo)
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GEO_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, Srs.WGS84, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object ChooserAutomation : Automation.HasErrorMessage, Automation.HasSuccessMessage, Automation.HasDelay {
        override val type = Automation.Type.SHARE
        override val packageName = ""
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenChooser(formatUriString(position, Srs.WGS84, uriQuote))

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
    data class AppAutomation(override val packageName: String) :
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP
        override val testTag = "geoShareUserPreferenceAutomationOpenApp_${packageName}"

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatUriString(position, Srs.WGS84, uriQuote))

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

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
        ChooserOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) =
        packageNames.map { AppOutput(it) }

    override fun getChipOutputs() = listOf(
        ChipOutput,
    )

    override fun getChooserOutput() = ChooserOutput

    override fun getAutomations(packageNames: List<String>): List<Automation> = buildList {
        add(CopyAutomation)
        add(ChooserAutomation)
        packageNames.forEach { add(AppAutomation(it)) }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GEO_URI -> CopyAutomation
        Automation.Type.SHARE -> ChooserAutomation
        Automation.Type.OPEN_APP if packageName != null -> AppAutomation(packageName)
        else -> null
    }

    fun formatUriString(value: Position, srs: Srs, uriQuote: UriQuote = DefaultUriQuote()): String = value.run {
        GeoUriPointOutputGroup.formatUriString(mainPoint ?: Point(), srs, uriQuote, q = q, zStr = zStr)
    }
}
