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

sealed interface Automation {
    @Composable
    fun Label()

    interface HasSuccessMessage : Automation {
        @Composable
        fun successText(): String
    }

    interface AlwaysSucceeds : Automation {
        fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        )
    }

    interface CanFail : Automation {
        @Composable
        fun errorText(): String

        fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ): Boolean
    }

    interface CanWait : Automation {
        val delaySec: Int

        @Composable
        fun waitingText(counterSec: Int): String
    }
}

sealed class AutomationImplementation : Automation {
    class Noop : AutomationImplementation() {
        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_nothing))
        }
    }

    class CopyCoordsDec : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toCoordsDecString())
        }

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

    class CopyCoordsNorthSouthWestEastDec : Automation.AlwaysSucceeds, Automation.HasSuccessMessage,
        AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toNorthSouthWestEastDecCoordsString())
        }

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

    class CopyGeoUri : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toGeoUriString())
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_geo))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyGoogleMapsUri : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toGoogleMapsUriString())
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_google_maps_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyAppleMapsUri : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toAppleMapsUriString())
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_apple_maps_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class CopyMagicEarthUri : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onCopy(position.toMagicEarthUriString())
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.user_preferences_automation_copy_magic_earth_link))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    class OpenApp(val packageName: String) : Automation.CanFail, Automation.CanWait, Automation.HasSuccessMessage,
        AutomationImplementation() {
        override val delaySec = 5

        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) = onOpenApp(packageName, position.toGeoUriString())

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

    class SaveGpx : Automation.AlwaysSucceeds, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) {
            onSave()
        }

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_save_gpx))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_save_gpx_succeeded)
    }

    class Share : Automation.CanFail, Automation.HasSuccessMessage, AutomationImplementation() {
        override fun run(
            position: Position,
            onCopy: (text: String) -> Unit,
            onOpenApp: (packageName: String, uriString: String) -> Boolean,
            onOpenChooser: (uriString: String) -> Boolean,
            onSave: () -> Unit,
        ) = onOpenChooser(position.toGeoUriString())

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
