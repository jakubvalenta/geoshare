package page.ooooo.geoshare.data.local.preferences

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
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class AutomationAction {
    class Noop : AutomationAction()

    @Immutable
    data class Copy(val text: String) : AutomationAction()

    @Immutable
    data class OpenApp(val packageName: String, val uriString: String) : AutomationAction()

    @Immutable
    data class OpenChooser(val uriString: String) : AutomationAction()

    class SaveGpx() : AutomationAction()
}

sealed interface Automation {
    fun run(position: Position, uriQuote: UriQuote = DefaultUriQuote()): AutomationAction

    @Composable
    fun Label()

    interface HasDelay : Automation {
        val delay: Duration

        @Composable
        fun waitingText(counterSec: Int): String
    }

    interface HasSuccessMessage : Automation {
        @Composable
        fun successText(): String
    }

    interface HasErrorMessage : Automation {
        @Composable
        fun errorText(): String
    }
}

sealed class AutomationImpl : Automation {
    override fun equals(other: Any?) = other != null && this::class == other::class
    override fun hashCode() = javaClass.hashCode()

    class Noop : AutomationImpl() {
        override fun run(position: Position, uriQuote: UriQuote) = AutomationAction.Noop()

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_nothing))
        }
    }

    class CopyCoordsDec :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) = AutomationAction.Copy(position.toCoordsDecString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.user_preferences_automation_copy_coords,
                    Position.example.toCoordsDecString().replace(' ', ' ')
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    class CopyCoordsDegMinSec :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.Copy(position.toDegMinSecCoordsString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.user_preferences_automation_copy_coords,
                    Position.example.toDegMinSecCoordsString().replace(' ', ' '),
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    class CopyGeoUri :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.Copy(position.toGeoUriString(uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyGoogleMapsUri :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.Copy(GoogleMapsUrlConverter.formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_link, GoogleMapsUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyAppleMapsUri :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.Copy(AppleMapsUrlConverter.formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_link, AppleMapsUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyMagicEarthUri :
        AutomationImpl(),
        Automation.HasSuccessMessage {

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.Copy(MagicEarthUrlConverter.formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_link, MagicEarthUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    @Immutable
    data class OpenApp(val packageName: String) :
        AutomationImpl(),
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val delay = 5.seconds

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.OpenApp(packageName, position.toGeoUriString(uriQuote))

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
                    Text(stringResource(R.string.user_preferences_automation_open_app, app.label))
                }
            } ?: Text(stringResource(R.string.user_preferences_automation_open_app, packageName))
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

    class SaveGpx :
        AutomationImpl(),
        Automation.HasSuccessMessage,
        Automation.HasErrorMessage,
        Automation.HasDelay {

        override val delay = 5.seconds

        override fun run(position: Position, uriQuote: UriQuote) = AutomationAction.SaveGpx()

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

    class Share :
        AutomationImpl(),
        Automation.HasErrorMessage,
        Automation.HasSuccessMessage,
        Automation.HasDelay {

        override val delay = 5.seconds

        override fun run(position: Position, uriQuote: UriQuote) =
            AutomationAction.OpenChooser(position.toGeoUriString(uriQuote))

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
}
