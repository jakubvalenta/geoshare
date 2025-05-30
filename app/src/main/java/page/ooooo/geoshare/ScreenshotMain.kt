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

@Composable
fun ScreenshotMain() {
    val appName = stringResource(R.string.app_name)
    Screenshot(
        R.drawable.geo_share_main_template,
        stringResource(R.string.intro_geo_links_form_content_description, appName)
    ) { scale, width ->
        ScreenshotText(
            stringResource(R.string.main_input_uri_label),
            x = 90,
            y = 350,
            scale = scale,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        ScreenshotText(
            stringResource(R.string.main_input_uri_supporting_text),
            x = 90,
            y = 470,
            scale = scale,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        ScreenshotText(
            stringResource(R.string.main_create_geo_uri),
            x = 0,
            y = 600,
            scale = scale,
            width = 1080,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
        ScreenshotText(
            stringResource(R.string.main_navigate_to_intro),
            x = 0,
            y = 730,
            scale = scale,
            width = 1080,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
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
