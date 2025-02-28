package page.ooooo.geoshare.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

data class RadioButtonOption<T>(
    val value: T,
    val text: @Composable () -> String,
)

@Composable
fun <T> RadioButtonGroup(
    options: List<RadioButtonOption<T>>,
    selectedValue: T,
    onSelect: (value: T) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Note that Modifier.selectableGroup() is essential to ensure correct
    // accessibility behavior
    Column(modifier.selectableGroup()) {
        options.forEach { (value, text) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.tiny)
                    .selectable(
                        selected = (value == selectedValue),
                        onClick = { onSelect(value) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (value == selectedValue),
                    // Null recommended for accessibility with screen readers
                    onClick = null
                )
                Text(
                    text = text(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Spacing.small)
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        RadioButtonGroup(
            options = listOf(
                RadioButtonOption(
                    1,
                    { "Foo bar" },
                ),
                RadioButtonOption(
                    2,
                    { "Kotlin is a modern but already mature programming language designed to make developers happier." },
                ),
            ),
            selectedValue = 2,
            onSelect = {}
        )
    }
}
