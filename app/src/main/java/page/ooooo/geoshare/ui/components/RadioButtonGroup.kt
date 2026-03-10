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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun <T> RadioButtonGroup(
    selectedValue: T,
    onSelect: (value: T) -> Unit,
    values: List<T>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    getTestTag: ((value: T) -> String)? = null,
    option: @Composable (value: T) -> Unit,
) {
    val spacing = LocalSpacing.current

    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
        Column(modifier.selectableGroup()) {
            values.forEach { value ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.tinyAdaptive)
                        .selectable(
                            selected = value == selectedValue,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(value) },
                        )
                        .run {
                            getTestTag?.invoke(value)?.let {
                                testTag(it)
                            } ?: this
                        },
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = value == selectedValue,
                        // Null recommended for accessibility with screen readers
                        onClick = null,
                        enabled = enabled,
                    )
                    if (enabled) {
                        option(value)
                    } else {
                        Box(Modifier.alpha(0.7f)) {
                            option(value)
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
                values = listOf(1, 2),
            ) { value ->
                when (value) {
                    1 -> Text("Foo bar")
                    2 -> Text("Kotlin is a modern but already mature programming language designed to make developers happier.")
                }
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
                values = listOf(1, 2),
            ) { value ->
                when (value) {
                    1 -> Text("Foo bar")
                    2 -> Text("Kotlin is a modern but already mature programming language designed to make developers happier.")
                }
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
                values = listOf(1, 2),
                enabled = false,
            ) { value ->
                when (value) {
                    1 -> Text("Foo bar")
                    2 -> Text("Kotlin is a modern but already mature programming language designed to make developers happier.")
                }
            }
        }
    }
}
