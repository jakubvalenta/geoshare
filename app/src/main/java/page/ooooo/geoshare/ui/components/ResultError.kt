package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultError(
    source: String,
    message: String,
    details: String?,
    initialExpanded: Boolean = false,
    onNavigateToInputsScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val uriHandler = LocalUriHandler.current
    var expanded by remember { mutableStateOf(initialExpanded) }

    // Animate alpha when the conversion is being retried, so that there's a visual feedback even if the conversion
    // leads to the same result and nothing changes in the end
    val (isRetrying, setIsRetrying) = remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isRetrying) 0.5f else 1f,
        finishedListener = { setIsRetrying(false) },
    )

    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = animatedAlpha }
    ) {
        Column(
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(bottom = spacing.tiny)
        ) {
            if (details != null) {
                ExpandablePane(
                    expanded = expanded,
                    onSetExpanded = { expanded = it },
                    title = {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                message,
                                Modifier.testTag("geoShareConversionErrorMessage"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                ) {
                    SelectionContainer {
                        Text(
                            details,
                            Modifier.padding(top = spacing.tiny),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 25,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                SelectionContainer(Modifier.fillMaxWidth()) {
                    Text(
                        message,
                        Modifier.testTag("geoShareConversionErrorMessage"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (source.isNotEmpty()) {
                SelectionContainer(Modifier.padding(top = spacing.small)) {
                    if (source.startsWith("https://")) {
                        Text(
                            source,
                            modifier = Modifier.clickable { uriHandler.openUri(source) },
                            textDecoration = TextDecoration.Underline,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            source,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        ScrollableChips {
            item {
                StyledChip(
                    stringResource(R.string.conversion_error_retry),
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {
                        setIsRetrying(true)
                        onRetry()
                    },
                )
            }
            item {
                StyledChip(
                    stringResource(R.string.conversion_error_report),
                ) {
                    uriHandler.openUri("https://github.com/jakubvalenta/geoshare/issues/new?template=1-bug-map-link.yml")
                }
            }
            item {
                StyledChip(
                    stringResource(R.string.inputs_title),
                    icon = {
                        Icon(Icons.Outlined.Info, null)
                    },
                ) {
                    onNavigateToInputsScreen()
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
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = null,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = null,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinatesPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "41°24′12.2″N 2°10′26.5″E",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = NotImplementedError().stackTraceToString(),
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkCoordinatesPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "41°24′12.2″N 2°10′26.5″E",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = NotImplementedError().stackTraceToString(),
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "41°24′12.2″N 2°10′26.5″E",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = NotImplementedError().stackTraceToString(),
                initialExpanded = true,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkExpandedPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "41°24′12.2″N 2°10′26.5″E",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = NotImplementedError().stackTraceToString(),
                initialExpanded = true,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = null,
                initialExpanded = false,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                source = "",
                message = stringResource(R.string.conversion_failed_reason_no_points),
                details = null,
                initialExpanded = false,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}
