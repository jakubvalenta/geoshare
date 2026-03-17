package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.ScreenshotTheme

@Composable
fun ScreenshotOpenByDefault() {
    val appName = stringResource(R.string.app_name)
    val density = LocalDensity.current
    val links = listOf(
        "google.com",
        "goo.gl",
        "www.google.com",
        "g.co",
        "maps.google.com",
        "app.goog.gl",
        "maps.apple.com",
        "maps.app.goog.gl",
    )
    FlowRow {
        Screenshot(
            R.drawable.open_by_default_geo_share,
            stringResource(R.string.intro_open_by_default_app_content_description, appName),
            IntSize(1080, 1575),
        ) { scale ->
            ScreenshotRow(
                scale,
                1080,
                x = 62,
                y = 28,
            ) {
                ScreenshotText(
                    stringResource(R.string.open_by_default),
                    scale,
                    Modifier.alpha(0.5f),
                    style = ScreenshotTheme.typography.textExtraExtraExtraLarge,
                )
            }
            ScreenshotColumn(
                scale,
                1080,
                y = 232,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScreenshotText(
                    pluralStringResource(R.plurals.intro_open_by_default_app_screenshot_links, links.size, links.size),
                    scale,
                    fontWeight = FontWeight.Medium,
                    style = ScreenshotTheme.typography.textExtraExtraLarge,
                )
            }
            ScreenshotColumn(
                scale,
                1080,
                x = 218,
                y = 356,
                verticalSpacing = 76,
            ) {
                for (text in links) {
                    ScreenshotText(
                        text,
                        scale,
                        style = ScreenshotTheme.typography.textLarge,
                    )
                }
            }
            ScreenshotRow(
                scale,
                1080,
                x = 153,
                y = 1385,
                horizontalArrangement = Arrangement.End,
            ) {
                ScreenshotText(
                    stringResource(R.string.intro_open_by_default_app_screenshot_cancel),
                    scale,
                    Modifier.padding(end = with(density) { 63.toDp() }),
                    fontWeight = FontWeight.Medium,
                )
                ScreenshotText(
                    stringResource(R.string.intro_open_by_default_app_screenshot_add),
                    scale,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Box(
            Modifier
                .widthIn(max = 480.dp)
                .padding(horizontal = LocalSpacing.current.largeAdaptive)
                .clip(MaterialTheme.shapes.large)
                .background(Color.White)
                .scaleAndCrop(0.8f),
        ) {
            Text(
                stringResource(R.string.open_by_default),
                Modifier.padding(horizontal = 30.dp, vertical = 15.dp),
                color = Color(0xFFAAAAAA), // TODO Dark mode
                fontSize = 36.sp,
            )
            Box(Modifier.background(Color(0xAA000000))) { // TODO Dark mode
                Column(
                    Modifier
                        .padding(horizontal = 30.dp)
                        .padding(top = 75.dp, bottom = 35.dp)
                        .fillMaxSize()
                        .background(Color.White, shape = MaterialTheme.shapes.extraLarge)
                        .padding(horizontal = 30.dp)
                ) {
                    Text(
                        pluralStringResource(
                            R.plurals.intro_open_by_default_app_screenshot_links,
                            73,
                            73,
                        ),
                        Modifier
                            .padding(vertical = 25.dp)
                            .align(Alignment.CenterHorizontally),
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                        mapOf(
                            "google.com" to true,
                            "goo.gl" to true,
                            "www.google.com" to true,
                            "g.co" to false, // TODO Replace with another link
                            "maps.google.com" to true,
                            "app.goog.gl" to true,
                            "maps.apple.com" to true,
                            "maps.app.goog.gl" to true,
                        ).forEach { (link, checked) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null, // Handled by the parent row; avoids double callbacks.
                                    enabled = true,
                                )
                                Text(
                                    text = link,
                                    color = Color.Black, // TODO Dark mode
                                    fontSize = 16.sp,
                                    letterSpacing = 0.sp,
                                )
                            }
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            stringResource(R.string.intro_open_by_default_app_screenshot_cancel),
                            Modifier.padding(end = 50.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp,
                        )
                        Text(
                            stringResource(R.string.intro_open_by_default_app_screenshot_add),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp,
                        )
                    }
                }
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ScreenshotOpenByDefaultPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpenByDefault()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkScreenshotOpenByDefaultPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpenByDefault()
        }
    }
}

@Preview(showBackground = true, locale = "ar-rEG")
@Composable
private fun RTLScreenshotOpenByDefaultPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpenByDefault()
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletScreenshotOpenByDefaultPreview() {
    AppTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenshotOpenByDefault()
        }
    }
}
