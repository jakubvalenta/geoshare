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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    after: (@Composable () -> Unit)? = null,
    bottom: (@Composable () -> Unit)? = null,
    chips: @Composable (lastPaddingEnd: Dp) -> Unit,
    main: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    val verticalSpace = spacing.small

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpace),
    ) {
        Row(
            Modifier.padding(
                start = spacing.windowPadding,
                end = (if (after != null) 4.dp else spacing.windowPadding),
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
        bottom?.let {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                bottom()
            }
        }
        Row(
            Modifier
                .padding(start = spacing.windowPadding, bottom = spacing.small)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            chips(spacing.windowPadding)
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
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
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
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
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
private fun DefaultBottomPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
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
                bottom = {
                    Row {
                        Text(
                            "point 1",
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        IconButton({}, Modifier.size(16.dp)) {
                            Icon(
                                painterResource(R.drawable.more_horiz_24px),
                                contentDescription = stringResource(R.string.nav_menu_content_description),
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
private fun DarkBottomPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
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
                bottom = {
                    Row {
                        Text(
                            "point 1",
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        IconButton({}, Modifier.size(16.dp)) {
                            Icon(
                                painterResource(R.drawable.more_horiz_24px),
                                contentDescription = stringResource(R.string.nav_menu_content_description),
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
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultCard(
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
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultCard(
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
