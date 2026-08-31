package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainHelp(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    inputRepository: InputRepository,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    val examplePoint = WGS84Point.Kilimanjaro
    val exampleSource = UriFormatter.formatUriString(
        examplePoint, "https://maps.google.com/?q={lat}%2C{lon}"
    )

    Column(modifier.padding(horizontal = spacing.windowPadding)) {
        HelpMessageCard(
            helpMessage = HelpMessage.WELCOME,
            dismissedHelpMessages = dismissedHelpMessages,
            title = { Text(stringResource(R.string.help_welcome_title)) },
            modifier = Modifier.padding(bottom = spacing.small),
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
            inputRepository
                .all
                .shuffled()
                .firstNotNullOfOrNull { it as? Input.HasRandomUri }
                ?.run {
                    val randomPoint = WGS84Point(
                        NaivePoint.genRandomPoint(
                            name = resources.getString(R.string.intro_how_to_share_google_maps_screenshot_place),
                        )
                    )
                    genRandomUri(randomPoint)
                }
                ?.let { newSource ->
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
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                inputRepository = FakeInputRepository,
                onDismissHelpMessage = {},
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
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                inputRepository = FakeInputRepository,
                onDismissHelpMessage = {},
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            )
        }
    }
}
