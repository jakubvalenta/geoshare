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
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ScreenshotMain() {
    val appName = stringResource(R.string.app_name)
    Screenshot(
        R.drawable.geo_share_main,
        stringResource(R.string.intro_geo_links_form_content_description, appName),
        IntSize(1080, 900),
    ) { scale ->
        ScreenshotColumn(
            scale,
            1080,
            x = 88,
            y = 349,
            verticalSpacing = 53,
        ) {
            ScreenshotText(
                stringResource(R.string.main_input_uri_label),
                scale,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            ScreenshotText(
                stringResource(R.string.main_input_uri_supporting_text),
                scale,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        ScreenshotColumn(
            scale,
            1080,
            y = 593,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalSpacing = 75,
        ) {
            ScreenshotText(
                stringResource(R.string.main_create_geo_uri),
                scale,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
            ScreenshotText(
                stringResource(R.string.main_navigate_to_intro),
                scale,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ScreenshotMainPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotMain()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkScreenshotMainPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotMain()
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletScreenshotMainPreview() {
    AppTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenshotMain()
        }
    }
}
