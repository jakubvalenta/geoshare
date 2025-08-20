package page.ooooo.geoshare.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScaffold(
    title: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    startButton: (@Composable () -> Unit)? = null,
    endButton: (@Composable () -> Unit)? = null,
    fill: Boolean = true,
    content: (@Composable (ColumnScope.() -> Unit))? = null,
) {
    Scaffold(
        Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = {
                    if (title != null) {
                        Text(title)
                    }
                },
                navigationIcon = navigationIcon,
            )
        },
    ) { innerPadding ->
        if (startButton != null || endButton != null || content != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .padding(horizontal = Spacing.windowPadding)
                    .padding(top = Spacing.small)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill)
                ) {
                    if (content != null) {
                        content()
                    }
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
    }
}
