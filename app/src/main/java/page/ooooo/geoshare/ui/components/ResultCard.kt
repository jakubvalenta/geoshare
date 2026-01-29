package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.CoordinatesOutput
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    end: (@Composable () -> Unit)? = null,
    bottom: (@Composable () -> Unit)? = null,
    chips: @Composable () -> Unit,
    main: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(modifier) {
        Row(
            Modifier.padding(
                start = spacing.windowPadding,
                end = (if (end != null) 4.dp else spacing.windowPadding),
                bottom = spacing.smallAdaptive,
            ),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = if (end == null) 4.dp else 0.dp),
                content = main,
            )
            end?.invoke()
        }
        bottom?.let {
            Column(
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(bottom = spacing.smallAdaptive)
            ) {
                bottom()
            }
        }
        Row(
            Modifier
                .padding(start = spacing.windowPadding, bottom = spacing.smallAdaptive)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            chips()
            Spacer(Modifier.width(spacing.windowPadding - spacing.small))
        }
    }
}

@Composable
fun ResultCardChip(
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
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
                end = {
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
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
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
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
                end = {
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
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultTopAndBottomPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
                end = {
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
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkTopAndBottomPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
                end = {
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
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
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
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
            ) {
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
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
            ResultCard(
                chips = {
                    ResultCardChip(
                        { Text("My first chip") },
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My second chip") },
                        onClick = {},
                    )
                    ResultCardChip(
                        { Text("My third chip") },
                        onClick = {},
                    )
                },
            ) {
                Text(
                    CoordinatesOutput.formatDegMinSecString(Point.example),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
