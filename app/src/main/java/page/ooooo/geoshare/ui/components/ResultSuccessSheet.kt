package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessSheet(
    sheetVisible: Boolean,
    onSetSheetVisible: (Boolean) -> Unit,
    content: @Composable (onHide: () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onSetSheetVisible(false) },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            sheetState = sheetState,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                content {
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
