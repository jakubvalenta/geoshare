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
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutputGroup : OutputGroup<Position> {

    @Suppress("SpellCheckingInspection")
    const val GMAPS_WV_PACKAGE_NAME = "us.spotco.maps"
    val PACKAGE_NAMES = setOf(
        GOOGLE_MAPS_PACKAGE_NAME,
        GMAPS_WV_PACKAGE_NAME,
    )

    object CopyDisplayOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyNavigateToOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyStreetViewOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatStreetViewUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_street_view, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = value.mainPoint != null
    }

    object ChipOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_google_maps)

        override fun isEnabled(value: Position) = true
    }

    @Immutable
    data class AppNavigateToOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_navigate_to, app.label)

        override fun isEnabled(value: Position) = true
    }

    @Immutable
    data class AppStreetViewOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatStreetViewUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_street_view, app.label)

        override fun isEnabled(value: Position) = value.mainPoint != null
    }

    object CopyDisplayAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyNavigateToAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_drive_to, GoogleMapsInput.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyStreetViewAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatStreetViewUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_street_view, GoogleMapsInput.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    @Immutable
    data class AppNavigateToAutomation(override val packageName: String) :
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatNavigateToUriString(position, uriQuote))

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
                    Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, app.label))
                }
            } ?: Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, packageName))
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

    @Immutable
    data class AppStreetViewAutomation(override val packageName: String) :
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatStreetViewUriString(position, uriQuote))

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
                    Text(stringResource(R.string.conversion_succeeded_open_app_street_view, app.label))
                }
            } ?: Text(stringResource(R.string.conversion_succeeded_open_app_street_view, packageName))
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

    override fun getNameOutput() = null

    override fun getDescriptionOutput() = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyNavigateToOutput,
        CopyStreetViewOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = buildList {
        packageNames.filter { it in PACKAGE_NAMES && it != GMAPS_WV_PACKAGE_NAME }.forEach { packageName ->
            add(AppNavigateToOutput(packageName))
            add(AppStreetViewOutput(packageName))
        }
    }

    override fun getChipOutputs() = listOf(
        ChipOutput,
    )

    override fun getChooserOutput() = null

    override fun getRandomOutput() = CopyDisplayOutput

    override fun getAutomations(packageNames: List<String>) = buildList {
        add(CopyDisplayAutomation)
        add(CopyNavigateToAutomation)
        add(CopyStreetViewAutomation)
        packageNames.filter { it in PACKAGE_NAMES && it != GMAPS_WV_PACKAGE_NAME }.forEach { packageName ->
            add(AppNavigateToAutomation(packageName))
            add(AppStreetViewAutomation(packageName))
        }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI -> CopyDisplayAutomation
        Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI -> CopyNavigateToAutomation
        Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI -> CopyStreetViewAutomation
        Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO if packageName != null -> AppNavigateToAutomation(packageName)
        Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW if packageName != null -> AppStreetViewAutomation(packageName)
        else -> null
    }

    private fun formatDisplayUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            value.apply {
                mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) ->
                    set("q", "$latStr,$lonStr")
                } ?: q?.let { q ->
                    set("q", q)
                }
                zStr?.let { zStr ->
                    set("z", zStr)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "google.navigation",
        path = value.run {
            mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
                ?: q
                ?: "0,0"
        }.let { q ->
            "q=$q"
        },
        uriQuote = uriQuote,
    ).toString()

    private fun formatStreetViewUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "google.streetview",
        path = value.run {
            mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
                ?: "0,0"
        }.let { coords ->
            @Suppress("SpellCheckingInspection")
            "cbll=$coords"
        },
        uriQuote = uriQuote,
    ).toString()
}
