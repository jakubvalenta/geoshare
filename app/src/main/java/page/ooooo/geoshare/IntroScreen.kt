package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.Dp.Companion.Hairline
import kotlinx.coroutines.launch
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IntroScreen(
    onCloseIntro: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Do nothing.
    }

    IntroScreen(
        onCloseIntro = onCloseIntro,
        onShowOpenByDefaultSettings = {
            viewModel.showOpenByDefaultSettings(context, settingsLauncher)
        },
        onShowOpenByDefaultSettingsForPackage = { packageName ->
            viewModel.showOpenByDefaultSettingsForPackage(context, settingsLauncher, packageName)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IntroScreen(
    initialPage: Int = 0,
    onCloseIntro: () -> Unit,
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
                            IntroOutlinedButton({
                                onShowOpenByDefaultSettingsForPackage("com.google.android.apps.maps")
                            }) {
                                Text(stringResource(R.string.intro_open_by_default_google_maps_button))
                            }
                        }
                        IntroFigure(
                            stringResource(R.string.intro_open_by_default_app_caption, appName),
                        ) {
                            ScreenshotOpenByDefault()
                            IntroOutlinedButton({
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
                        { onCloseIntro() },
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
                            onCloseIntro()
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
fun IntroPage(
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
fun IntroFigure(
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

@Composable
fun IntroOutlinedButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    OutlinedButton(onClick, border = BorderStroke(width = Hairline, color = MaterialTheme.colorScheme.outline)) {
        content()
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun PageOnePreview() {
    AppTheme {
        IntroScreen(
            onCloseIntro = {},
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
            onCloseIntro = {},
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
            onCloseIntro = {},
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
            onCloseIntro = {},
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
            onCloseIntro = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}
