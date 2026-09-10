package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.Message

class MessageSnackbarVisuals(message: Message) : SnackbarVisuals {
    override val message = message.text
    override val actionLabel = null
    override val withDismissAction = false
    override val duration = SnackbarDuration.Short
    val isError = message.isError
}

@Composable
fun MessageSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
    ) { snackbarData ->
        val isError = (snackbarData.visuals as? MessageSnackbarVisuals)?.isError ?: false
        val containerColor = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        val contentColor = contentColorFor(containerColor)
        Snackbar(
            snackbarData = snackbarData,
            containerColor = containerColor,
            contentColor = contentColor,
            dismissActionContentColor = contentColor,
            actionColor = contentColor,
        )
    }
}
