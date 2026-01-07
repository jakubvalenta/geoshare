package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

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
    enabled: Boolean = true,
    options: @Composable () -> List<RadioButtonOption<T>>,
) {
    val spacing = LocalSpacing.current

    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
        Column(modifier.selectableGroup()) {
            options().forEach { option ->
                Row(
                    option.modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.tiny)
                        .selectable(
                            selected = (option.value == selectedValue),
                            onClick = { onSelect(option.value) },
                            role = Role.RadioButton
                        ),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option.value == selectedValue),
                        // Null recommended for accessibility with screen readers
                        onClick = null,
                        enabled = enabled,
                    )
                    if (enabled) {
                        option.label()
                    } else {
                        Box(Modifier.alpha(0.7f)) {
                            option.label()
                        }
                    }
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
        Surface {
            RadioButtonGroup(
                selectedValue = 2,
                onSelect = {},
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
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            RadioButtonGroup(
                selectedValue = 2,
                onSelect = {},
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
}

@Preview(showBackground = true)
@Composable
private fun DisabledPreview() {
    AppTheme {
        Surface {
            RadioButtonGroup(
                selectedValue = 2,
                onSelect = {},
                enabled = false,
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
}
