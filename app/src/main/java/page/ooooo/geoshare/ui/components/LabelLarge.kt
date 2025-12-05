package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun LabelLarge(text: String) {
    val spacing = LocalSpacing.current
    Text(
        text,
        Modifier.padding(
            start = spacing.windowPadding,
            top = spacing.large,
            end = spacing.windowPadding,
            bottom = spacing.small,
        ),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}
