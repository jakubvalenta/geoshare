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
import page.ooooo.geoshare.ui.theme.screenshotTextMedium

@Composable
fun ScreenshotOpen() {
    val appName = stringResource(R.string.app_name)
    Screenshot(
        R.drawable.geo_share_open_template,
        stringResource(R.string.intro_how_to_share_app_content_description, appName),
    ) { scale, width ->
        ScreenshotText(
            "Open with OsmAnd~",
            x = 130,
            y = 60,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Just once",
            x = 660,
            y = 260,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Always",
            x = 900,
            y = 260,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Use a different app",
            x = 60,
            y = 440,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Organic Maps",
            x = 130,
            y = 600,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "Mapy.com",
            x = 130,
            y = 740,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
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
