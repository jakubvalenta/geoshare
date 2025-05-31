package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    ) { scale, width ->
        ScreenshotRow(
            scale,
            x = 126,
            y = 55,
            width = width,
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
            x = 78,
            y = 265,
            width = width,
            horizontalArrangement = Arrangement.End,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_app_screenshot_once),
                scale,
                Modifier.padding(end = with(density) { 50.toDp() }),
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
            x = 62,
            y = 434,
            width = width,
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
            x = 126,
            y = 600,
            width = width,
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
