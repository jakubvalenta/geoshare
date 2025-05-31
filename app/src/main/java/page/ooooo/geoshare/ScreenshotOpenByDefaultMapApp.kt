package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import page.ooooo.geoshare.ui.theme.*

@Composable
fun ScreenshotOpenByDefaultMapApp() {
    Screenshot(
        R.drawable.open_by_default_google_maps,
        stringResource(R.string.intro_open_by_default_google_maps_content_description),
        IntSize(1080, 1048),
    ) { scale ->
        ScreenshotRow(
            scale,
            1080,
            x = 62,
            y = 28,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_open_by_default_app_screenshot_title),
                scale,
                style = MaterialTheme.typography.screenshotTextExtraExtraExtraLarge,
            )
        }
        ScreenshotColumn(
            scale,
            1080,
            x = 62,
            y = 498,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalSpacing = 10,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_open_by_default_google_maps_screenshot_app),
                scale,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.screenshotTextExtraExtraLarge,
            )
            ScreenshotText(
                stringResource(R.string.intro_open_by_default_google_maps_screenshot_choose),
                scale,
                color = MaterialTheme.colorScheme.screenshotMutedTextColor,
                style = MaterialTheme.typography.screenshotTextMedium,
            )
        }
        ScreenshotColumn(
            scale,
            1080,
            x = 189,
            y = 772,
            verticalSpacing = 94,
        ) {
            for (text in listOf(
                stringResource(R.string.intro_open_by_default_google_maps_screenshot_in_the_app),
                stringResource(R.string.intro_open_by_default_google_maps_screenshot_in_your_browser),
            )) {
                ScreenshotText(
                    text,
                    scale = scale,
                    style = MaterialTheme.typography.screenshotTextExtraExtraLarge,
                )
            }
        }
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

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletScreenshotOpenByDefaultMapAppPreview() {
    AppTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenshotOpenByDefaultMapApp()
        }
    }
}
