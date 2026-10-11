package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainHelp(
    randomUris: StateFlow<List<String>>,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
    message: (@Composable (paddingValues: PaddingValues) -> Unit)? = null,
) {
    val spacing = LocalSpacing.current

    val randomUris by randomUris.collectAsStateWithLifecycle()

    Column(
        Modifier
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.tiny)
    ) {
        message?.invoke(PaddingValues(bottom = spacing.small))
        TextButton(onNavigateToInputsScreen) {
            Icon(
                painterResource(R.drawable.map_24px),
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.inputs_title))
        }
        TextButton({ onNavigateToFaqScreen(null) }) {
            Icon(
                painterResource(R.drawable.help_24px),
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.faq_title))
        }
        TextButton({
            randomUris.firstOrNull()?.let { newSource ->
                onSetSource(newSource)
                onSetErrorMessageResId(null)
            }
        }) {
            Icon(
                painterResource(R.drawable.ifl_24px),
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.main_random))
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            MainHelp(
                randomUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            MainHelp(
                randomUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    AppTheme {
        Surface {
            MainHelp(
                randomUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            ) { paddingValues ->
                if (helpLifecycleMessage != null) {
                    helpLifecycleMessage(Modifier.padding(paddingValues))
                } else {
                    HelpWelcomeMessage(
                        dismissedHelpMessages = MutableStateFlow(emptySet()),
                        modifier = Modifier.padding(paddingValues),
                        onDismissHelpMessage = {},
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkMessagePreview() {
    AppTheme {
        Surface {
            MainHelp(
                randomUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            ) { paddingValues ->
                if (helpLifecycleMessage != null) {
                    helpLifecycleMessage(Modifier.padding(paddingValues))
                } else {
                    HelpWelcomeMessage(
                        dismissedHelpMessages = MutableStateFlow(emptySet()),
                        modifier = Modifier.padding(paddingValues),
                        onDismissHelpMessage = {},
                    )
                }
            }
        }
    }
}
