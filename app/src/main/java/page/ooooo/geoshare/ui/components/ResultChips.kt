package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultChips(
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        Modifier
            .padding(start = spacing.windowPadding)
            .padding(vertical = spacing.smallAdaptive)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        content()
        Spacer(Modifier.width(spacing.windowPadding - spacing.small))
    }
}

@Composable
fun ResultChip(
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    SuggestionChip(
        onClick = onClick,
        label = label,
        modifier = modifier,
        icon = icon,
        colors = SuggestionChipDefaults.suggestionChipColors(
            labelColor = LocalContentColor.current,
            iconContentColor = LocalContentColor.current,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = LocalContentColor.current.copy(alpha = 0.5f),
        ),
        shape = MaterialTheme.shapes.medium,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultChips {
                ResultChip(
                    { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {},
                )
                ResultChip(
                    { Text("My second chip") },
                    onClick = {},
                )
                ResultChip(
                    { Text("My third chip") },
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultChips {
                ResultChip(
                    { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {},
                )
                ResultChip(
                    { Text("My second chip") },
                    onClick = {},
                )
                ResultChip(
                    { Text("My third chip") },
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultChips {
                ResultChip(
                    { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {},
                )
                ResultChip(
                    { Text("My second chip") },
                    onClick = {},
                )
                ResultChip(
                    { Text("My third chip") },
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultChips {
                ResultChip(
                    { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {},
                )
                ResultChip(
                    { Text("My second chip") },
                    onClick = {},
                )
                ResultChip(
                    { Text("My third chip") },
                    onClick = {},
                )
            }
        }
    }
}
