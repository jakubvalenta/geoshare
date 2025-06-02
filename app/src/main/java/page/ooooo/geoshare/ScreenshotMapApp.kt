package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.ScreenshotTheme

private data class Icon(val name: String, val label: String? = null)

@Composable
private fun ScreenshotMapApp(contentDescription: String, highlightedIconIndex: Int) {
    val appName = stringResource(R.string.app_name)
    val density = LocalDensity.current
    Screenshot(
        R.drawable.map_app,
        contentDescription,
        IntSize(1080, 952),
    ) { scale ->
        ScreenshotRow(
            scale,
            1080,
            x = 42,
            y = 306,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_google_maps_screenshot_title),
                scale,
                style = ScreenshotTheme.typography.textExtraLarge,
            )
        }
        ScreenshotColumn(
            scale,
            1080,
            x = 65,
            y = 443,
            verticalSpacing = 6,
        ) {
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_google_maps_screenshot_place),
                scale,
                fontWeight = FontWeight.Bold,
            )
            ScreenshotText(
                stringResource(R.string.intro_how_to_share_google_maps_screenshot_url),
                scale,
                color = ScreenshotTheme.colors.mutedTextColor,
            )
        }
        ScreenshotRow(
            scale,
            1080,
            y = 836,
        ) {
            for (icon in listOf(
                Icon(stringResource(R.string.intro_how_to_share_google_maps_screenshot_app_messaging)),
                Icon(appName, stringResource(R.string.share_activity)),
                Icon(appName, stringResource(R.string.copy_activity)),
                Icon(stringResource(R.string.intro_how_to_share_google_maps_screenshot_app_bluetooth)),
                Icon(stringResource(R.string.intro_how_to_share_google_maps_screenshot_app_chrome)),
            )) {
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(with(density) { 4.toDp() * scale }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ScreenshotText(
                        icon.name,
                        scale,
                        style = ScreenshotTheme.typography.textSmall,
                    )
                    if (icon.label != null) {
                        ScreenshotText(
                            icon.label,
                            scale,
                            color = ScreenshotTheme.colors.mutedTextColor,
                            style = ScreenshotTheme.typography.textSmall,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(0, 646) * scale }
                .padding(start = with(density) { ((1080 / 5) * highlightedIconIndex + 11).toDp() * scale })
                .size(with(density) { 192.toDp() * scale })
                .border(
                    with(density) { 8.toDp() * scale },
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                )
        )
    }
}

@Composable
fun ScreenshotMapAppOpen() {
    ScreenshotMapApp(
        stringResource(R.string.intro_how_to_share_google_maps_content_description),
        highlightedIconIndex = 1
    )
}

@Composable
fun ScreenshotMapAppCopy() {
    ScreenshotMapApp(
        stringResource(R.string.intro_geo_links_copy_content_description),
        highlightedIconIndex = 2
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun ScreenshotMapAppPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotMapAppOpen()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkScreenshotMapAppPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotMapAppCopy()
        }
    }
}

@Preview(showBackground = true, locale = "ar-rEG")
@Composable
private fun RTLScreenshotMapAppPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            ScreenshotMapAppOpen()
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletScreenshotMapAppPreview() {
    AppTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenshotMapAppOpen()
        }
    }
}
