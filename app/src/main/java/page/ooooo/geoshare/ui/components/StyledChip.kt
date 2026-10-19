package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ChipColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun StyledChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    colors: ChipColors = SuggestionChipDefaults.suggestionChipColors(
        labelColor = LocalContentColor.current,
        iconContentColor = LocalContentColor.current,
    ),
    border: BorderStroke? = SuggestionChipDefaults.suggestionChipBorder(
        enabled = enabled,
        borderColor = LocalContentColor.current.copy(alpha = 0.3f),
        disabledBorderColor = LocalContentColor.current.copy(alpha = 0.3f),
    ),
    contentPadding: PaddingValues = SuggestionChipDefaults.ContentPadding,
) {
    SuggestionChip(
        onClick = onClick,
        label = label,
        modifier = modifier.height(32.dp), // Set height to prevent default vertical padding (caused by min height)
        enabled = enabled,
        icon = icon,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                StyledChip(onClick = {}, label = { Text("My chip") })
            }
        }
    }
}
