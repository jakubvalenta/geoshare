package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.outputs.Output

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessSheet(
    outputs: List<Output.WithAction>,
    sheetVisible: Boolean,
    onSetSheetVisible: (Boolean) -> Unit,
    onRun: (action: Action) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onSetSheetVisible(false) },
            sheetState = sheetState,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val (copyOutputs, otherOutputs) = outputs.partition { it.action is Action.Copy }
                copyOutputs.forEach { output ->
                    ResultSuccessSheetItem(output.label, supportingText = (output.action as? Action.Copy)?.text) {
                        onRun(output.action)
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onSetSheetVisible(false)
                            }
                        }
                    }
                }
                if (copyOutputs.isNotEmpty() && otherOutputs.isNotEmpty()) {
                    HorizontalDivider()
                }
                otherOutputs.forEach { output ->
                    ResultSuccessSheetItem(output.label) {
                        onRun(output.action)
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
}
