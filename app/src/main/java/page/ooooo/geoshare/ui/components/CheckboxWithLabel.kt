package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun CheckboxWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = LocalSpacing.current

    Row(
        modifier.toggleable(value = checked, enabled = enabled, role = Role.Checkbox) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by the parent row; avoids double callbacks.
            enabled = enabled,
        )
        Text(
            text = label,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    var checked by remember { mutableStateOf(false) }
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)) {
                CheckboxWithLabel(
                    label = "Accept terms and conditions",
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
                CheckboxWithLabel(
                    label = "Disabled option",
                    checked = true,
                    onCheckedChange = {},
                    enabled = false,
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    var checked by remember { mutableStateOf(false) }
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)) {
                CheckboxWithLabel(
                    label = "Accept terms and conditions",
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
                CheckboxWithLabel(
                    label = "Disabled option",
                    checked = true,
                    onCheckedChange = {},
                    enabled = false,
                )
            }
        }
    }
}
