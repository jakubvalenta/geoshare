package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.ui.theme.Spacing

private val examplePosition = Position("50.123456", "-11.123456")

sealed class AutomationAction {
    class Noop : AutomationAction()
    class Copy(val text: String) : AutomationAction()
    class OpenApp(val packageName: String, val uriString: String) : AutomationAction()
    class OpenChooser(val uriString: String) : AutomationAction()
    class SaveGpx() : AutomationAction()
}

sealed interface Automation {
    @Composable
    fun Label()

    fun run(position: Position): AutomationAction

    interface HasDelay : Automation {
        val delaySec: Int

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

sealed class AutomationImplementation : Automation {
    override fun equals(other: Any?) = other != null && this::class == other::class
    override fun hashCode() = javaClass.hashCode()

    class Noop : AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Noop()

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_nothing))
        }
    }

    class CopyCoordsDec : Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toCoordsDecString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.user_preferences_automation_copy_coords,
                    examplePosition.toCoordsDecString()
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    class CopyCoordsNorthSouthWestEastDec : Automation.HasSuccessMessage,
        AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toNorthSouthWestEastDecCoordsString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.user_preferences_automation_copy_coords,
                    examplePosition.toNorthSouthWestEastDecCoordsString(),
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    class CopyGeoUri : Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toGeoUriString())

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyGoogleMapsUri : Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toGoogleMapsUriString())

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_google_maps_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyAppleMapsUri : Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toAppleMapsUriString())

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_apple_maps_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyMagicEarthUri : Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.Copy(position.toMagicEarthUriString())

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_magic_earth_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    data class OpenApp(val packageName: String) : Automation.HasSuccessMessage, Automation.HasErrorMessage,
        Automation.HasDelay, AutomationImplementation() {
        override val delaySec = 5
        override fun run(position: Position) = AutomationAction.OpenApp(packageName, position.toGeoUriString())

        @Composable
        override fun Label() {
            queryApp()?.let { app ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
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

    class SaveGpx : Automation.HasSuccessMessage, Automation.HasDelay, AutomationImplementation() {
        override val delaySec = 5
        override fun run(position: Position) = AutomationAction.SaveGpx()

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_save_gpx))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_save_gpx_succeeded)

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(R.string.conversion_automation_save_gpx_waiting, counterSec)
    }

    class Share : Automation.HasErrorMessage, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(position: Position) = AutomationAction.OpenChooser(position.toGeoUriString())

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_share))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_share_succeeded)

        @Composable
        override fun errorText() = stringResource(R.string.conversion_automation_share_failed)
    }
}
