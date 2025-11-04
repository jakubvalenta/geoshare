package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.Action as Action_

sealed interface Output {
    interface WithAction : Output {
        val action: Action_
        val label: @Composable () -> String
    }

    data class Action(override val action: Action_, override val label: @Composable () -> String) :
        WithAction

    data class AppAction(
        val packageName: String,
        override val action: Action_,
        override val label: @Composable () -> String,
    ) :
        WithAction

    data class Chip(override val action: Action_, override val label: @Composable () -> String) :
        WithAction

    data class PointAction(val i: Int, override val action: Action_, override val label: @Composable () -> String) :
        WithAction

    data class SupportingText(val text: String) : Output

    data class Text(val text: String) : Output
}
