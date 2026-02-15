package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

class ChipsScope {
    val items: MutableList<@Composable () -> Unit> = mutableListOf()

    fun item(content: @Composable () -> Unit) {
        items.add(content)
    }
}

@Composable
fun ScrollableChips(
    paddingValues: PaddingValues = PaddingValues(
        horizontal = LocalSpacing.current.windowPadding,
        vertical = LocalSpacing.current.smallAdaptive,
    ),
    content: ChipsScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current
    val space = spacing.small

    val scope = ChipsScope()
    scope.content()

    Row(
        Modifier
            .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.width(paddingValues.calculateStartPadding(layoutDirection)))
        scope.items.forEachIndexed { i, item ->
            item()
            if (i < scope.items.size - 1) {
                Spacer(Modifier.width(space))
            }
        }
        Spacer(Modifier.width(paddingValues.calculateEndPadding(layoutDirection)))
    }
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
            ScrollableChips {
                item {
                    StyledChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My second chip",
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My third chip",
                        onClick = {},
                    )
                }
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
            ScrollableChips {
                item {
                    StyledChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My second chip",
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My third chip",
                        onClick = {},
                    )
                }
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
            ScrollableChips {
                item {
                    StyledChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My second chip",
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My third chip",
                        onClick = {},
                    )
                }
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
            ScrollableChips {
                item {
                    StyledChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My second chip",
                        onClick = {},
                    )
                }
                item {
                    StyledChip(
                        "My third chip",
                        onClick = {},
                    )
                }
            }
        }
    }
}
