package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IntroScreen(
    onBack: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Do nothing.
    }

    IntroScreen(
        onBack = {
            viewModel.setIntroShown()
            onBack()
        },
        onShowOpenByDefaultSettings = {
            viewModel.intentTools.showOpenByDefaultSettings(context, settingsLauncher)
        },
        onShowOpenByDefaultSettingsForPackage = { packageName ->
            viewModel.intentTools.showOpenByDefaultSettingsForPackage(context, settingsLauncher, packageName)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun IntroScreen(
    initialPage: Int = 0,
    onBack: () -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
    onShowOpenByDefaultSettingsForPackage: (packageName: String) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val pageCount = 2
    var page by remember { mutableIntStateOf(initialPage) }
    val animatedProgress by animateFloatAsState(
        targetValue = (page + 1f) / pageCount,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    BackHandler {
        if (page != 0) {
            coroutineScope.launch {
                scrollState.scrollTo(0)
                page--
            }
        } else {
            onBack()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                { animatedProgress },
                Modifier.padding(vertical = Spacing.tiny),
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                when (page) {
                    0 -> IntroPage(
                        stringResource(R.string.intro_how_to_share_headline),
                        page,
                    ) {
                        IntroFigure(
                            stringResource(R.string.intro_how_to_share_google_maps_caption),
                        ) {
                            ScreenshotMapAppOpen()
                        }
                        IntroFigure(
                            stringResource(R.string.intro_how_to_share_app_caption, appName),
                        ) {
                            ScreenshotOpen()
                        }
                    }

                    1 -> IntroPage(
                        stringResource(R.string.intro_open_by_default_headline),
                        page,
                    ) {
                        IntroFigure(
                            stringResource(R.string.intro_open_by_default_google_maps_caption),
                        ) {
                            ScreenshotOpenByDefaultMapApp()
                            FilledTonalButton({
                                onShowOpenByDefaultSettingsForPackage(GOOGLE_MAPS_PACKAGE_NAME)
                            }) {
                                Text(stringResource(R.string.intro_open_by_default_google_maps_button))
                            }
                        }
                        IntroFigure(
                            stringResource(R.string.intro_open_by_default_app_caption, appName),
                        ) {
                            ScreenshotOpenByDefault()
                            FilledTonalButton({
                                onShowOpenByDefaultSettings()
                            }) {
                                Text(stringResource(R.string.intro_open_by_default_app_button, appName))
                            }
                            ParagraphHtml(
                                stringResource(R.string.intro_open_by_default_app_note),
                                Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.tiny),
            ) {
                if (page != pageCount - 1) {
                    TextButton(
                        { onBack() },
                        Modifier.testTag("geoShareIntroScreenCloseButton"),
                    ) {
                        Text(stringResource(R.string.intro_nav_close))
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    {
                        if (page != pageCount - 1) {
                            coroutineScope.launch {
                                scrollState.scrollTo(0)
                                page++
                            }
                        } else {
                            onBack()
                        }
                    },
                    Modifier.testTag("geoShareIntroScreenNextButton"),
                ) {
                    Text(
                        stringResource(
                            if (page != pageCount - 1) {
                                R.string.intro_nav_next
                            } else {
                                R.string.intro_nav_finish
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroPage(
    headline: String,
    page: Int,
    content: @Composable () -> Unit = {},
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            headline,
            Modifier
                .testTag("geoShareIntroPage${page}HeadingText")
                .padding(vertical = Spacing.small),
            style = MaterialTheme.typography.headlineSmall,
        )
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            content()
        }
    }
}

@Composable
private fun IntroFigure(
    captionHtml: String,
    content: @Composable () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
    ) {
        ParagraphHtml(captionHtml, Modifier.fillMaxWidth())
        content()
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun PageOnePreview() {
    AppTheme {
        IntroScreen(
            onBack = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageOnePreview() {
    AppTheme {
        IntroScreen(
            onBack = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageTwoPreview() {
    AppTheme {
        IntroScreen(
            initialPage = 1,
            onBack = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageTwoPreview() {
    AppTheme {
        IntroScreen(
            initialPage = 1,
            onBack = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPageOnePreview() {
    AppTheme {
        IntroScreen(
            onBack = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}
