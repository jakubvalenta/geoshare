package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.isDismissed
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun HelpOpenByDefaultMessage(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    sourceComesFromIntent: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (sourceComesFromIntent) {
        val appName = stringResource(R.string.app_name)
        val helpMessage = HelpMessage.OPEN_BY_DEFAULT
        val dismissedHelpMessages by dismissedHelpMessages.collectAsStateWithLifecycle()
        val visible = remember(dismissedHelpMessages) { !helpMessage.isDismissed(dismissedHelpMessages) }

        HelpMessageCard(
            title = { Text(stringResource(R.string.help_open_by_default_title, appName)) },
            visible = visible,
            modifier = modifier.testTag("geoShareHelpMessage_$helpMessage"),
            actionText = {
                stringResource(R.string.help_open_by_default_action)
            },
            onAction = {
                onNavigateToFaqScreen(FaqItemId.OPEN_BY_DEFAULT)
            },
            onDismissRequest = { onDismissHelpMessage(helpMessage) },
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
