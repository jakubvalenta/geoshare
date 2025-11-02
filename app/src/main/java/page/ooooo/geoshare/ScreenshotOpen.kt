package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ScreenshotOpen() {
    val appName = stringResource(R.string.app_name)
    val density = LocalDensity.current
    Screenshot(
        R.drawable.geo_share_open,
        stringResource(R.string.intro_how_to_share_app_content_description, appName),
        IntSize(1080, 896),
    ) { scale ->
        ScreenshotRow(
            scale,
            1080,
            x = 86,
            y = 100,
        ) {
            ScreenshotText(
                Outputs.default.getMainText(Position("42.5784957", "1.8955661")),
                scale,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            x = 86,
            y = 220,
        ) {
            ScreenshotText(
                stringResource(R.string.conversion_succeeded_copy_geo),
                scale,
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.shapes.large,
                    )
                    .padding(
                        horizontal = with(density) { 44.toDp() * scale },
                        vertical = with(density) { 17.toDp() * scale },
                    ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            x = 84,
            y = 478,
        ) {
            ScreenshotText(
                stringResource(R.string.conversion_succeeded_apps_headline),
                scale,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            x = 80,
            y = 774,
            horizontalArrangement = Arrangement.spacedBy(with(density) { 70.toDp() * scale }),
        ) {
            @Suppress("SpellCheckingInspection")
            for (text in listOf("Maps", "Mapy.com", "Organic Maps", "OsmAnd~")) {
                ScreenshotText(
                    text,
                    scale,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
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
