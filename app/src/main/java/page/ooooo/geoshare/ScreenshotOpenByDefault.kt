package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.screenshotTextLarge
import page.ooooo.geoshare.ui.theme.screenshotTextMedium

@Composable
fun ScreenshotOpenByDefault() {
    val appName = stringResource(R.string.app_name)
    Screenshot(
        R.drawable.open_by_default_geo_share_template,
        stringResource(R.string.intro_open_by_default_app_content_description, appName)
    ) { scale, width ->
        ScreenshotText(
            "Open by default",
            x = 55,
            y = 30,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
        ScreenshotText(
            "8 supported links",
            x = 0,
            y = 240,
            scale = scale,
            width = 1080,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
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
        for ((i, link) in links.withIndex()) {
            ScreenshotText(
                link,
                x = 220,
                y = 360 + i * 125,
                scale = scale,
                style = MaterialTheme.typography.screenshotTextMedium,
            )
        }
        ScreenshotText(
            "Cancel",
            x = 660,
            y = 1390,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Add",
            x = 860,
            y = 1390,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
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
