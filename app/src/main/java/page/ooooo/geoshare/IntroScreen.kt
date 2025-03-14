package page.ooooo.geoshare

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IntroScreen(
    initialPage: Int = 0,
    onCloseIntro: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)
    val pageCount = 3
    var page by remember { mutableStateOf(initialPage) }
    val animatedProgress by animateFloatAsState(
        targetValue = (page + 1f) / pageCount,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        // Do nothing.
    }

    fun showOpenByDefaultSettings(packageName: String) {
        try {
            val action =
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    // Samsung supposedly doesn't allow going to the "Open by
                    // default" settings page.
                    Build.MANUFACTURER.lowercase(Locale.ROOT) != "samsung"
                ) {
                    Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                } else {
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                }
            val intent = Intent(action, Uri.parse("package:$packageName"))
            settingsLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.intro_settings_activity_not_found,
                Toast.LENGTH_LONG,
            )
                .show()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) { innerPadding ->
        Column(
            modifier = Modifier
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
                            R.drawable.google_maps_share,
                            stringResource(R.string.intro_how_to_share_google_maps_content_description),
                            stringResource(R.string.intro_how_to_share_google_maps_caption),
                        )
                        IntroFigure(
                            R.drawable.geo_share_open,
                            stringResource(
                                R.string.intro_how_to_share_app_content_description,
                                appName
                            ),
                            stringResource(
                                R.string.intro_how_to_share_app_caption,
                                stringResource(R.string.share_activity),
                                appName
                            ),
                        )
                    }

                    1 -> IntroPage(
                        stringResource(R.string.intro_open_by_default_headline),
                        page,
                    ) {
                        IntroFigure(
                            R.drawable.open_by_default_google_maps,
                            stringResource(R.string.intro_open_by_default_google_maps_content_description),
                            stringResource(R.string.intro_open_by_default_google_maps_caption),
                        ) {
                            OutlinedButton({
                                showOpenByDefaultSettings("com.google.android.apps.maps")
                            }) {
                                Text(stringResource(R.string.intro_open_by_default_google_maps_button))
                            }
                        }
                        IntroFigure(
                            R.drawable.open_by_default_geo_share,
                            stringResource(
                                R.string.intro_open_by_default_app_content_description,
                                appName
                            ),
                            stringResource(
                                R.string.intro_open_by_default_app_caption,
                                appName
                            ),
                        ) {
                            OutlinedButton({
                                showOpenByDefaultSettings(context.packageName)
                            }) {
                                Text(
                                    stringResource(
                                        R.string.intro_open_by_default_app_button,
                                        appName
                                    )
                                )
                            }
                            ParagraphHtml(
                                stringResource(R.string.intro_open_by_default_app_note),
                                Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    2 -> IntroPage(
                        stringResource(R.string.intro_geo_links_headline),
                        page,
                    ) {
                        IntroFigure(
                            R.drawable.google_maps_copy,
                            stringResource(R.string.intro_geo_links_copy_content_description),
                            stringResource(
                                R.string.intro_geo_links_copy_caption,
                                stringResource(R.string.copy_activity)
                            ),
                        )
                        IntroFigure(
                            R.drawable.geo_share_main,
                            stringResource(
                                R.string.intro_geo_links_form_content_description,
                                appName
                            ),
                            stringResource(
                                R.string.intro_geo_links_form_caption,
                                appName
                            ),
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.tiny),
            ) {
                if (page != pageCount - 1) {
                    TextButton({ onCloseIntro() }) {
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
    drawableId: Int,
    contentDescription: String,
    captionHtml: String,
    content: @Composable () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
    ) {
        ParagraphHtml(captionHtml, Modifier.fillMaxWidth())
        Image(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .padding(horizontal = Spacing.large)
                .clip(MaterialTheme.shapes.medium),
        )
        content()
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun PageOnePreview() {
    AppTheme {
        IntroScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageOnePreview() {
    AppTheme {
        IntroScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun PageTwoPreview() {
    AppTheme {
        IntroScreen(initialPage = 1)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageTwoPreview() {
    AppTheme {
        IntroScreen(initialPage = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun PageThreePreview() {
    AppTheme {
        IntroScreen(initialPage = 2)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageThreePreview() {
    AppTheme {
        IntroScreen(initialPage = 2)
    }
}
