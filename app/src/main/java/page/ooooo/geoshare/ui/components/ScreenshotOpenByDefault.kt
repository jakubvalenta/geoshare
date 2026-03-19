package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.ScreenshotTheme
import kotlin.math.min

@Composable
fun ScreenshotOpenByDefault() {
    val spacing = LocalSpacing.current

    // This box measures the available space
    BoxWithConstraints(
        Modifier
            .widthIn(max = 400.dp)
            .padding(horizontal = spacing.largeAdaptive)
    ) {
        val origWidth = 430.dp
        val origHeight = 632.dp
        val scale = min(maxWidth / origWidth, 1f)

        // This box clips the scaled screenshot
        Box(
            Modifier
                .padding(horizontal = LocalSpacing.current.large)
                .requiredSize(origWidth * scale, origHeight * scale)
        ) {

            // This box is the scaled screenshot
            Box(
                Modifier
                    .requiredSize(origWidth, origHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(MaterialTheme.shapes.large)
                    .background(ScreenshotTheme.colors.surfaceColor)
            ) {
                Text(
                    stringResource(R.string.open_by_default),
                    Modifier.padding(horizontal = 27.dp, vertical = 15.dp),
                    color = ScreenshotTheme.colors.textColor,
                    fontSize = 36.sp,
                    letterSpacing = 0.sp,
                )
                Box(Modifier.background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))) {
                    Column(
                        Modifier
                            .padding(horizontal = 27.dp)
                            .padding(top = 73.dp, bottom = 35.dp)
                            .background(
                                ScreenshotTheme.colors.surfaceContainerHighestColor,
                                shape = MaterialTheme.shapes.extraLarge,
                            )
                            .padding(start = 30.dp, end = 33.dp, bottom = 20.dp)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides ScreenshotTheme.colors.textColor) {
                            Text(
                                pluralStringResource(
                                    R.plurals.intro_open_by_default_app_screenshot_links,
                                    73,
                                    73,
                                ),
                                Modifier
                                    .padding(vertical = 23.dp)
                                    .align(Alignment.CenterHorizontally),
                                fontSize = 20.sp,
                                letterSpacing = 0.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                                mapOf(
                                    "app.goog.gl" to false,
                                    "goo.gl" to false,
                                    "google.com" to false,
                                    "maps.app.goog.gl" to true,
                                    "maps.apple" to false,
                                    "maps.apple.com" to false,
                                    "maps.google.com" to true,
                                    "www.google.com" to true,
                                ).forEach { (link, checked) ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Use a disabled checkbox but style it to appear enabled
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = null,
                                            enabled = false,
                                            colors = CheckboxDefaults.colors().run {
                                                copy(
                                                    disabledCheckedBoxColor = checkedBoxColor,
                                                    disabledUncheckedBoxColor = uncheckedBoxColor,
                                                    disabledBorderColor = checkedBorderColor,
                                                    disabledUncheckedBorderColor = uncheckedBorderColor,
                                                    disabledCheckmarkColor = checkedCheckmarkColor,
                                                )
                                            },
                                        )
                                        Text(
                                            text = link,
                                            fontSize = 17.sp,
                                            letterSpacing = 0.sp,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Row(
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Text(
                                    stringResource(R.string.intro_open_by_default_app_screenshot_cancel),
                                    Modifier.padding(end = 27.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp,
                                )
                                Text(
                                    stringResource(R.string.intro_open_by_default_app_screenshot_add),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp,
                                )
                            }
                        }
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
