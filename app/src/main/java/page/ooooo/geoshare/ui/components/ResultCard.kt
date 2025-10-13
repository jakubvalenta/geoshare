package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultCard(
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    after: (@Composable () -> Unit)? = null,
    chips: @Composable (lastPaddingEnd: Dp) -> Unit,
    main: @Composable ColumnScope.() -> Unit,
) {
    val verticalSpace = Spacing.small

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(verticalSpace)) {
            Row(
                Modifier.padding(
                    start = Spacing.windowPadding,
                    end = (if (after != null) 4.dp else Spacing.windowPadding),
                ),
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .padding(top = 12.dp, bottom = if (after == null) 4.dp else 0.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(verticalSpace), content = main)
                }
                after?.invoke()
            }
            Row(
                Modifier
                    .padding(start = Spacing.windowPadding, bottom = Spacing.small)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                chips(Spacing.windowPadding)
            }
        }
    }
}

@Composable
fun ResultCardChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text) },
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
        Surface {
            ResultCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                chips = { lastPaddingEnd ->
                    ResultCardChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        onClick = {},
                    )
                    ResultCardChip(
                        "My third chip",
                        modifier = Modifier.padding(end = lastPaddingEnd),
                        onClick = {},
                    )
                },
                after = {
                    Box {
                        IconButton({}) {
                            Icon(
                                painterResource(R.drawable.content_copy_24px),
                                contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                            )
                        }
                    }
                },
            ) {
                Text("My main text", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                chips = { lastPaddingEnd ->
                    ResultCardChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        onClick = {},
                    )
                    ResultCardChip(
                        "My third chip",
                        modifier = Modifier.padding(end = lastPaddingEnd),
                        onClick = {},
                    )
                },
                after = {
                    Box {
                        IconButton({}) {
                            Icon(
                                painterResource(R.drawable.content_copy_24px),
                                contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                            )
                        }
                    }
                },
            ) {
                Text("My main text", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface {
            ResultCard(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                chips = { lastPaddingEnd ->
                    ResultCardChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        onClick = {},
                    )
                    ResultCardChip(
                        "My third chip",
                        modifier = Modifier.padding(end = lastPaddingEnd),
                        onClick = {},
                    )
                },
            ) {
                Text("My main text", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface {
            ResultCard(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                chips = { lastPaddingEnd ->
                    ResultCardChip(
                        "My first chip",
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        onClick = {},
                    )
                    ResultCardChip(
                        "My third chip",
                        modifier = Modifier.padding(end = lastPaddingEnd),
                        onClick = {},
                    )
                },
            ) {
                Text("My main text", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
