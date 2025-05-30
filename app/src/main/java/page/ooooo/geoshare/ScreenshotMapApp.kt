package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.screenshotMutedTextColor
import page.ooooo.geoshare.ui.theme.screenshotTextLarge
import page.ooooo.geoshare.ui.theme.screenshotTextSmall

private data class Icon(val name: String, val label: String? = null)

@Composable
private fun ScreenshotMapApp(contentDescription: String, highlightedIconIndex: Int) {
    val density = LocalDensity.current
    Screenshot(R.drawable.map_app, contentDescription) { scale, width ->
        ScreenshotText(
            "Sharing link",
            x = 42,
            y = 305,
            scale = scale,
            style = MaterialTheme.typography.screenshotTextLarge,
        )
        ScreenshotText(
            "Dropped pin",
            x = 65,
            y = 443,
            scale = scale,
        )
        ScreenshotText(
            "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8",
            x = 65,
            y = 492,
            scale = scale,
        )
        val icons = listOf(
            Icon("Messaging"),
            Icon("Geo Share", "Open"),
            Icon("Geo Share", "Copy geo:"),
            Icon("Bluetooth"),
            Icon("Chrome"),
        )
        val iconWidth = width / icons.size
        for ((i, icon) in icons.withIndex()) {
            ScreenshotText(
                icon.name,
                x = i * iconWidth,
                y = 838,
                scale = scale,
                width = iconWidth,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.screenshotTextSmall,
            )
            if (icon.label != null) {
                ScreenshotText(
                    icon.label,
                    x = i * iconWidth,
                    y = 881,
                    scale = scale,
                    width = iconWidth,
                    color = MaterialTheme.colorScheme.screenshotMutedTextColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.screenshotTextSmall,
                )
            }
            if (i == highlightedIconIndex) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(i * iconWidth + 11, 646) * scale }
                        .size(with(density) { (iconWidth - 24).toDp() * scale })
                        .border(
                            with(density) { 8.toDp() * scale }, MaterialTheme.colorScheme.primaryContainer, CircleShape
                        )
                )
            }
        }
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
