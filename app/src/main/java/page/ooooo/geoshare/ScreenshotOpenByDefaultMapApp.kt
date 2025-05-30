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
fun ScreenshotOpenByDefaultMapApp() {
    Screenshot(
        R.drawable.open_by_default_google_maps_template,
        stringResource(R.string.intro_open_by_default_google_maps_content_description)
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
            "Maps",
            x = 0,
            y = 500,
            scale = scale,
            width = 1080,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
        ScreenshotText(
            "Choose how to open web links for this app",
            x = 0,
            y = 570,
            scale = scale,
            width = 1080,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextMedium,
        )
        ScreenshotText(
            "In the app",
            x = 190,
            y = 780,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
        ScreenshotText(
            "In your browser",
            x = 190,
            y = 930,
            scale = scale,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ScreenshotOpenByDefaultMapAppPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpenByDefaultMapApp()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkScreenshotOpenByDefaultMapAppPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotOpenByDefaultMapApp()
        }
    }
}
