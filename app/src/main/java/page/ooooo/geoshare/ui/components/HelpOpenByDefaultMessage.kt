package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun HelpOpenByDefaultMessage(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    sourceComesFromIntent: StateFlow<Boolean>,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (sourceComesFromIntent) {
        val appName = stringResource(R.string.app_name)
        HelpMessageCard(
            helpMessage = HelpMessage.OPEN_BY_DEFAULT,
            dismissedHelpMessages = dismissedHelpMessages,
            title = { Text(stringResource(R.string.help_open_by_default_title, appName)) },
            actionText = {
                stringResource(R.string.help_open_by_default_action)
            },
            onAction = {
                onNavigateToFaqScreen(FaqItemId.OPEN_BY_DEFAULT)
            },
            onDismiss = onDismissHelpMessage,
        ) {
            ParagraphText(
                stringResource(R.string.help_open_by_default_text, appName)
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        HelpOpenByDefaultMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        HelpOpenByDefaultMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}
