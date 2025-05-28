package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import page.ooooo.geoshare.ui.theme.AppTheme

private data class Icon(val name: String, val label: String? = null)

@Composable
fun IntroFigureImageMapApp(contentDescription: String, highlightedIconIndex: Int = 0) {
    val density = LocalDensity.current
    val textColor = if (isSystemInDarkTheme()) Color(0xffe2e2e2) else Color(0xff1b1b1b)
    val mutedTextColor = if (isSystemInDarkTheme()) Color(0xffc6c6c6) else Color(0xff474747)
    IntroFigureImage(R.drawable.map_app, contentDescription) { scale, width ->
        Text(
            "Sharing link",
            Modifier.offset { IntOffset(42, 305) * scale },
            color = textColor,
            fontSize = 17.sp * scale,
            letterSpacing = 0.sp,
            lineHeight = 17.sp * scale,
        )
        Text(
            "Dropped pin",
            Modifier.offset { IntOffset(65, 443) * scale },
            color = textColor,
            fontSize = 13.5.sp * scale,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            lineHeight = 13.5.sp * scale,
        )
        Text(
            "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8",
            Modifier.offset { IntOffset(65, 492) * scale },
            color = mutedTextColor,
            fontSize = 13.5.sp * scale,
            letterSpacing = 0.sp,
            lineHeight = 13.5.sp * scale,
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
            Text(
                icon.name,
                Modifier
                    .offset { IntOffset(i * iconWidth, 838) * scale }
                    .width(with(density) { iconWidth.toDp() * scale }),
                color = textColor,
                fontSize = 12.sp * scale,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp * scale,
            )
            if (icon.label != null) {
                Text(
                    icon.label,
                    Modifier
                        .offset { IntOffset(i * iconWidth, 881) * scale }
                        .width(with(density) { iconWidth.toDp() * scale }),
                    color = mutedTextColor,
                    fontSize = 12.sp * scale,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp * scale,
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

// Previews

@Preview(showBackground = true)
@Composable
private fun IntroFigureMapAppSharePreview() {
    AppTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            IntroFigureImageMapApp(contentDescription = "My content description", highlightedIconIndex = 2)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkIntroFigureMapAppSharePreview() {
    AppTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            IntroFigureImageMapApp(contentDescription = "My content description", highlightedIconIndex = 2)
        }
    }
}
