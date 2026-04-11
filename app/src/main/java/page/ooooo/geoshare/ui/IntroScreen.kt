package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.point.CoordinateConverter
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScreenshotMapAppOpen
import page.ooooo.geoshare.ui.components.ScreenshotOpen
import page.ooooo.geoshare.ui.components.ScreenshotOpenByDefault
import page.ooooo.geoshare.ui.components.ScreenshotOpenByDefaultMapApp
import page.ooooo.geoshare.ui.components.styledArgsString
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun IntroScreen(
    onClose: () -> Unit,
    viewModel: IntroViewModel,
    outputViewModel: OutputViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Do nothing.
    }

    IntroScreen(
        coordinateFormatter = outputViewModel.coordinateFormatter,
        onClose = {
            viewModel.setShown()
            onClose()
        },
        onShowOpenByDefaultSettings = {
            AndroidTools.showOpenByDefaultSettings(context, settingsLauncher)
        },
        onShowOpenByDefaultSettingsForPackage = { packageName ->
            AndroidTools.showOpenByDefaultSettingsForPackage(context, settingsLauncher, packageName)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun IntroScreen(
    coordinateFormatter: CoordinateFormatter,
    initialPage: Int = 0,
    onClose: () -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
    onShowOpenByDefaultSettingsForPackage: (packageName: String) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val pageCount = 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val animatedProgress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1f) / pageCount,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )
    val maxWidth = 600.dp

    BackHandler {
        if (pagerState.canScrollBackward) {
            coroutineScope.launch {
                pagerState.scrollToPage(pagerState.currentPage)
            }
        } else {
            onClose()
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
                .consumeWindowInsets(innerPadding),
        ) {
            LinearProgressIndicator(
                { animatedProgress },
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = spacing.tinyAdaptive),
                trackColor = MaterialTheme.colorScheme.surface,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> IntroPage(
                        stringResource(R.string.intro_how_to_share_headline),
                        page,
                        maxWidth,
                    ) {
                        IntroFigure(
                            stringResource(R.string.intro_how_to_share_google_maps_caption),
                        ) {
                            ScreenshotMapAppOpen()
                        }
                        IntroFigure(
                            stringResource(R.string.intro_how_to_share_app_caption, appName),
                        ) {
                            ScreenshotOpen(coordinateFormatter)
                        }
                    }

                    1 -> IntroPage(
                        stringResource(R.string.intro_open_by_default_headline),
                        page,
                        maxWidth,
                    ) {
                        IntroFigure(
                            stringResource(R.string.intro_open_by_default_google_maps_caption),
                        ) {
                            ScreenshotOpenByDefaultMapApp()
                            Button(
                                {
                                    onShowOpenByDefaultSettingsForPackage(GOOGLE_MAPS_PACKAGE_NAME)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            ) {
                                Text(stringResource(R.string.intro_open_by_default_google_maps_button))
                            }
                        }
                        IntroFigure(
                            stringResource(R.string.intro_open_by_default_app_caption, appName),
                        ) {
                            ScreenshotOpenByDefault()
                            FilledTonalButton(
                                {
                                    onShowOpenByDefaultSettings()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            ) {
                                Text(stringResource(R.string.intro_open_by_default_app_button, appName))
                            }
                            ParagraphText(
                                styledArgsString(
                                    R.string.intro_open_by_default_app_note_1,
                                    SpanStyle(fontWeight = FontWeight.Bold),
                                    "maps.app.goog.gl", "maps.google.com", "www.google.com",
                                )
                            )
                            ParagraphText(
                                stringResource(R.string.intro_open_by_default_app_note)
                            )
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier
                        .widthIn(max = maxWidth)
                        .padding(horizontal = spacing.windowPadding, vertical = spacing.tinyAdaptive),
                ) {
                    if (pagerState.canScrollForward) {
                        TextButton(
                            { onClose() },
                            Modifier.testTag("geoShareIntroScreenCloseButton"),
                        ) {
                            Text(stringResource(R.string.intro_nav_close))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        {
                            if (pagerState.canScrollForward) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onClose()
                            }
                        },
                        Modifier.testTag("geoShareIntroScreenNextButton"),
                    ) {
                        Text(
                            stringResource(
                                if (pagerState.canScrollForward) {
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
}

@Composable
private fun IntroPage(
    headline: String,
    page: Int,
    maxWidth: Dp,
    content: @Composable () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier
                .fillMaxHeight()
                .widthIn(max = maxWidth)
                .padding(horizontal = spacing.windowPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                headline,
                Modifier
                    .padding(vertical = spacing.smallAdaptive)
                    .testTag("geoShareIntroPage${page}HeadingText"),
                style = MaterialTheme.typography.headlineSmall,
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
                content()
            }
        }
    }
}

@Composable
private fun IntroFigure(
    captionHtml: String,
    content: @Composable () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive),
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
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageOnePreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun SmallPageOnePreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPageOnePreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageTwoPreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            initialPage = 1,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageTwoPreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            initialPage = 1,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun SmallPageTwoPreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            initialPage = 1,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPageTwoPreview() {
    AppTheme {
        val context = LocalContext.current
        val chinaGeometry = ChinaGeometry(context)
        val coordinateConverter = CoordinateConverter(chinaGeometry)
        val coordinateFormatter = CoordinateFormatter(coordinateConverter)
        IntroScreen(
            coordinateFormatter = coordinateFormatter,
            initialPage = 1,
            onClose = {},
            onShowOpenByDefaultSettings = {},
            onShowOpenByDefaultSettingsForPackage = {},
        )
    }
}
