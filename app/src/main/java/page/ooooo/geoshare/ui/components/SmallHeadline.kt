package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun SmallHeadline(
    text: String,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
) {
    Text(
        text,
        Modifier
            .background(containerColor)
            .fillMaxWidth()
            .padding(
                start = Spacing.windowPadding,
                top = Spacing.large,
                end = Spacing.windowPadding,
                bottom = Spacing.medium,
            ),
        color = contentColor,
        style = MaterialTheme.typography.headlineMedium,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            SmallHeadline("My heading")
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            SmallHeadline("My heading")
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun BackgroundPreview() {
    AppTheme {
        Surface {
            SmallHeadline(
                "My heading",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkBackgroundPreview() {
    AppTheme {
        Surface {
            SmallHeadline(
                "My heading",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
