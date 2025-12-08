package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
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
