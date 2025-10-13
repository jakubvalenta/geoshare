package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

data class RadioButtonOption<T>(
    val value: T,
    val modifier: Modifier = Modifier,
    val label: @Composable () -> Unit,
)

@Composable
fun <T> RadioButtonGroup(
    selectedValue: T,
    onSelect: (value: T) -> Unit,
    modifier: Modifier = Modifier,
    options: @Composable () -> List<RadioButtonOption<T>>,
) {
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(modifier.selectableGroup()) {
        options().forEach { option ->
            Row(
                option.modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.tiny)
                    .selectable(
                        selected = (option.value == selectedValue),
                        onClick = { onSelect(option.value) },
                        role = Role.RadioButton
                    ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.value == selectedValue),
                    // Null recommended for accessibility with screen readers
                    onClick = null
                )
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    option.label()
                }
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
            selectedValue = 2,
            onSelect = {}
        ) {
            listOf(
                RadioButtonOption(value = 1) {
                    Text("Foo bar")
                },
                RadioButtonOption(value = 2) {
                    Text("Kotlin is a modern but already mature programming language designed to make developers happier.")
                },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        RadioButtonGroup(
            selectedValue = 2,
            onSelect = {}
        ) {
            listOf(
                RadioButtonOption(value = 1) {
                    Text("Foo bar")
                },
                RadioButtonOption(value = 2) {
                    Text("Kotlin is a modern but already mature programming language designed to make developers happier.")
                },
            )
        }
    }
}
