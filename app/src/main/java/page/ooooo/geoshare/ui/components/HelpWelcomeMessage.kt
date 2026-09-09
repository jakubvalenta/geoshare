package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun HelpWelcomeMessage(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val examplePoint = WGS84Point.Kilimanjaro
    val exampleSource = UriFormatter.formatUriString(
        examplePoint, "https://maps.google.com/?q={lat}%2C{lon}"
    )

    HelpMessageCard(
        helpMessage = HelpMessage.WELCOME,
        dismissedHelpMessages = dismissedHelpMessages,
        title = { Text(stringResource(R.string.help_welcome_title)) },
        modifier = modifier,
        after = exampleSource?.let {
            {
                SelectionContainer {
                    Text(
                        exampleSource,
                        Modifier.background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)),
                    )
                }
            }
        },
        actionText = exampleSource?.let {
            {
                stringResource(R.string.help_welcome_action)
            }
        },
        onAction = {
            exampleSource?.let { exampleSource ->
                coroutineScope.launch {
                    AndroidTools.copyToClipboard(clipboard, exampleSource)
                }
            }
        },
        onDismiss = onDismissHelpMessage,
    ) {
        ParagraphText(
            stringResource(
                R.string.help_welcome_text,
                stringResource(R.string.main_create_geo_uri),
            )
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        HelpWelcomeMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            onDismissHelpMessage = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        HelpWelcomeMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            onDismissHelpMessage = {},
        )
    }
}
