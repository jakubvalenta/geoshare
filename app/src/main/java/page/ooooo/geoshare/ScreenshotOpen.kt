package page.ooooo.geoshare

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.screenshotTextLarge

@Composable
fun ScreenshotOpen() {
    val appName = stringResource(R.string.app_name)
    val density = LocalDensity.current
    val mapApps = listOf("OsmAnd~", "Organic Maps", "Mapy")
    Screenshot(
        R.drawable.geo_share_open,
        stringResource(R.string.intro_how_to_share_app_content_description, appName),
        IntSize(1080, 864),
    ) { scale ->
        ScreenshotRow(
            scale,
            1080,
            x = 126,
            y = 55,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_app_screenshot_title, mapApps[0]),
                scale,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.screenshotTextLarge,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            x = 78,
            y = 265,
            horizontalArrangement = Arrangement.End,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_app_screenshot_once),
                scale,
                Modifier.padding(end = with(density) { 66.toDp() * scale }),
                fontWeight = FontWeight.Medium,
            )
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_app_screenshot_always),
                scale,
                fontWeight = FontWeight.Medium,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            x = 62,
            y = 434,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_app_screenshot_different),
                scale,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.screenshotTextLarge,
            )
        }
        ScreenshotColumn(
            scale,
            1080,
            x = 126,
            y = 600,
            verticalSpacing = 98,
        ) {
            for (text in mapApps.drop(1)) {
                ScreenshotText(
                    text,
                    scale,
                    style = MaterialTheme.typography.screenshotTextLarge,
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ScreenshotOpenPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpen()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkScreenshotOpenPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpen()
        }
    }
}

@Preview(showBackground = true, locale = "ar-rEG")
@Composable
private fun RTLScreenshotOpenPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpen()
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletScreenshotOpenPreview() {
    AppTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenshotOpen()
        }
    }
}
