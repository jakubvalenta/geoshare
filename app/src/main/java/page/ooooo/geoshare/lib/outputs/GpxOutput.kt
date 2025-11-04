package page.ooooo.geoshare.lib.outputs

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*
import kotlin.time.Duration.Companion.seconds

object GpxOutput : Output {

    object SaveAutomation : Automation.HasSuccessMessage, Automation.HasErrorMessage, Automation.HasDelay {
        override val type = Automation.Type.SAVE_GPX
        override val packageName = ""
        override val testTag = null

        override val delay = 5.seconds

        override fun getAction(position: Position, uriQuote: UriQuote) = Action.SaveGpx()

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

    override fun getText(position: Position, uriQuote: UriQuote) = null

    override fun getText(point: Point, uriQuote: UriQuote) = null

    override fun getActions(position: Position, uriQuote: UriQuote) = emptyList<Output.Item<Action>>()

    override fun getActions(point: Point, uriQuote: UriQuote) = emptyList<Output.Item<Action>>()

    override fun getAutomations(context: Context): List<Automation> = listOf(
        SaveAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) =
        if (type == Automation.Type.SAVE_GPX) SaveAutomation else null

    override fun getChips(position: Position, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.SaveGpx()) {
            stringResource(R.string.conversion_succeeded_save_gpx)
        }
    )
}
