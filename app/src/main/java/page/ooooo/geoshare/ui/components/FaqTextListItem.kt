package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun TextListScope.FaqTextListItem(
    text: String,
    example: String? = null,
) {
    val spacing = LocalSpacing.current

    TextListItem(bullet = { TextListBullet() }) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny / 2)) {
            Text(text)
            if (example != null) {
                Text(
                    example,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
