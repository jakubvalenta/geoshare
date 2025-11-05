package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*
import kotlin.time.Duration.Companion.seconds

object GpxOutputManager : OutputManager {

    object SaveAutomation : Automation.HasSuccessMessage, Automation.HasErrorMessage, Automation.HasDelay {
        override val type = Automation.Type.SAVE_GPX
        override val packageName = ""
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) = Action.SaveGpx(position)

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

    object SaveOutput : Output.Chip, Output.SaveGpxAction {
        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.SaveGpx(position, uriQuote)

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_save_gpx)
    }

    override fun getOutputs(packageNames: List<String>) = listOf(
        SaveOutput,
    )

    override fun getAutomations(packageNames: List<String>) = listOf(
        SaveAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.SAVE_GPX -> SaveAutomation
        else -> null
    }
}
