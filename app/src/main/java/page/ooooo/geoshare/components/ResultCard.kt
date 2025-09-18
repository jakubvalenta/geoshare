package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultCard(
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    after: (@Composable () -> Unit)? = null,
    chips: (@Composable () -> Unit)? = null,
    main: @Composable () -> Unit,
) {
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(top = Spacing.small),
        shape = OutlinedTextFieldDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Row(
            Modifier.padding(
                start = Spacing.small,
                top = Spacing.tiny,
                end = if (after != null) 4.dp else Spacing.small,
            )
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = if (after == null && chips != null) 4.dp else 0.dp),
            ) {
                Column {
                    main()
                }
            }
            after?.invoke()
        }
        Row(
            Modifier
                .padding(start = Spacing.small, bottom = Spacing.tiny)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            chips?.invoke()
        }
    }
}

@Composable
fun ResultCardChip(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit,
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            labelColor = contentColor,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = contentColor.copy(alpha = 0.5f),
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
                chips = {
                    ResultCardChip("My first chip", onClick = {})
                    ResultCardChip("My second chip", modifier = Modifier.padding(end = Spacing.small), onClick = {})
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
                chips = {
                    ResultCardChip("My first chip", onClick = {})
                    ResultCardChip("My second chip", modifier = Modifier.padding(end = Spacing.small), onClick = {})
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
                chips = {
                    ResultCardChip(
                        "My first chip",
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        modifier = Modifier.padding(end = Spacing.small),
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
                chips = {
                    ResultCardChip(
                        "My first chip",
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {},
                    )
                    ResultCardChip(
                        "My second chip",
                        modifier = Modifier.padding(end = Spacing.small),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {},
                    )
                },
            ) {
                Text("My main text", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
