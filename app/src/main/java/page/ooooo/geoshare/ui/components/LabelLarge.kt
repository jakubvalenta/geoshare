package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun LabelLarge(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    val spacing = LocalSpacing.current
    Text(
        text,
        Modifier.padding(
            start = spacing.windowPadding,
            top = spacing.largeAdaptive,
            end = spacing.windowPadding,
            bottom = spacing.smallAdaptive,
        ),
        color = color,
        style = MaterialTheme.typography.labelLarge,
    )
}
