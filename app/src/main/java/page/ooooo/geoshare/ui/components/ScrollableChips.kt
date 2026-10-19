package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ScrollableChips(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = LocalSpacing.current.small,
    content: @Composable RowScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    Row(modifier.horizontalScroll(rememberScrollState())) {
        Row(
            Modifier.padding(horizontal = spacing.windowPadding),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        ) {
            content()
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            ScrollableChips {
                StyledChip(
                    onClick = {},
                    label = { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My second chip") },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My third chip") },
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
        ) {
            ScrollableChips {
                StyledChip(
                    onClick = {},
                    label = { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My second chip") },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My third chip") },
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
        ) {
            ScrollableChips {
                StyledChip(
                    onClick = {},
                    label = { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My second chip") },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My third chip") },
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
        ) {
            ScrollableChips {
                StyledChip(
                    onClick = {},
                    label = { Text("My first chip") },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My second chip") },
                )
                StyledChip(
                    onClick = {},
                    label = { Text("My third chip") },
                )
            }
        }
    }
}
