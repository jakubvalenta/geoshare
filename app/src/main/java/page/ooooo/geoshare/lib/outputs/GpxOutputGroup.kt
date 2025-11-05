package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.*
import kotlin.time.Duration.Companion.seconds

object GpxOutputGroup : OutputGroup<Position> {

    object SaveOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.SaveGpx(value, uriQuote)

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_save_gpx)
    }

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

    override fun getTextOutput(): Output.Text<Position>? = null

    override fun getSupportingTextOutput(): Output.Text<Position>? = null

    override fun getActionOutputs() = listOf(
        SaveOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Position>>()

    override fun getChipOutputs() = listOf(
        SaveOutput,
    )

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = listOf(
        SaveAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.SAVE_GPX -> SaveAutomation
        else -> null
    }
}
