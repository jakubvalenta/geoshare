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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainHelp(
    inputRepository: InputRepository,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
    message: (@Composable (paddingValues: PaddingValues) -> Unit)? = null,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.medium)
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
                inputRepository = FakeInputRepository,
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
                inputRepository = FakeInputRepository,
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
                inputRepository = FakeInputRepository,
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            ) { paddingValues ->
                HelpMessageCard(
                    helpMessage = HelpMessage.WELCOME,
                    dismissedHelpMessages = MutableStateFlow(emptySet()),
                    title = { Text(stringResource(R.string.help_welcome_title)) },
                    modifier = Modifier.padding(paddingValues),
                    onDismiss = {},
                ) {
                    ParagraphText(
                        stringResource(
                            R.string.help_welcome_text,
                            stringResource(R.string.main_create_geo_uri),
                        )
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
                inputRepository = FakeInputRepository,
                onNavigateToFaqScreen = {},
                onNavigateToInputsScreen = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
            ) { paddingValues ->
                HelpMessageCard(
                    helpMessage = HelpMessage.WELCOME,
                    dismissedHelpMessages = MutableStateFlow(emptySet()),
                    title = { Text(stringResource(R.string.help_welcome_title)) },
                    modifier = Modifier.padding(paddingValues),
                    onDismiss = {},
                ) {
                    ParagraphText(
                        stringResource(
                            R.string.help_welcome_text,
                            stringResource(R.string.main_create_geo_uri),
                        )
                    )
                }
            }
        }
    }
}
