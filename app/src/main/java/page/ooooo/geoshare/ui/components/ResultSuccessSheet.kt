package page.ooooo.geoshare.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.outputs.Output

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessSheet(
    items: List<Output.Item<Action>>,
    sheetVisible: Boolean,
    onSetSheetVisible: (Boolean) -> Unit,
    onRun: (action: Action) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if (sheetVisible) {
        ModalBottomSheet(onDismissRequest = { onSetSheetVisible(false) }, sheetState = sheetState) {
            val (labeledCopyActions, labeledOtherActions) = items.partition { it.action is Action.Copy }
            labeledCopyActions.forEach { (action, label) ->
                ResultSuccessSheetItem(label, supportingText = (action as Action.Copy).text) {
                    onRun(action)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onSetSheetVisible(false)
                        }
                    }
                }
            }
            HorizontalDivider()
            labeledOtherActions.forEach { (action, label) ->
                ResultSuccessSheetItem(label) {
                    onRun(action)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onSetSheetVisible(false)
                        }
                    }
                }
            }
        }
    }
}
