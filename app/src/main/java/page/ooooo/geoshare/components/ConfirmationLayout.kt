package page.ooooo.geoshare.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ConfirmationLayout(
    modifier: Modifier = Modifier,
    startButton: (@Composable () -> Unit)? = null,
    endButton: (@Composable () -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Column(modifier.padding(horizontal = Spacing.windowPadding)) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.tiny)
        ) {
            if (startButton != null) {
                startButton()
            }
            if (endButton != null) {
                Spacer(Modifier.weight(1f))
                endButton()
            }
        }
    }
}
