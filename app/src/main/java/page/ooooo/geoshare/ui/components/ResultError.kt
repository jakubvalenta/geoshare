package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultError(
    @StringRes errorMessageResId: Int,
    inputUriString: String,
    initialRetryLoadingIndicatorVisible: Boolean = false,
    onNavigateToInputsScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val (retryLoadingIndicatorVisible, setRetryLoadingIndicatorVisible) = retain {
        mutableStateOf(initialRetryLoadingIndicatorVisible)
    }

    ResultCard(
        modifier = Modifier
            .testTag("geoShareConversionError")
            .fillMaxWidth(),
        chips = {
            if (!retryLoadingIndicatorVisible) {
                ResultCardChip(
                    { Text(stringResource(R.string.conversion_error_retry)) },
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = {
                        coroutineScope.launch {
                            setRetryLoadingIndicatorVisible(true)
                            delay(1000)
                            setRetryLoadingIndicatorVisible(false)
                            onRetry()
                        }
                    },
                )
                ResultCardChip(
                    { Text(stringResource(R.string.conversion_error_report)) },
                ) {
                    uriHandler.openUri("https://github.com/jakubvalenta/geoshare/issues/new?template=1-bug-map-link.yml")
                }
                ResultCardChip(
                    { Text(stringResource(R.string.inputs_title)) },
                    icon = {
                        Icon(Icons.Outlined.Info, null)
                    },
                ) {
                    onNavigateToInputsScreen()
                }
            }
        },
    ) {
        if (!retryLoadingIndicatorVisible) {
            SelectionContainer {
                Text(
                    stringResource(errorMessageResId),
                    Modifier.testTag("geoShareConversionErrorMessage"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (inputUriString.isNotEmpty()) {
                SelectionContainer {
                    if (inputUriString.startsWith("https://")) {
                        Text(
                            inputUriString,
                            modifier = Modifier.clickable { uriHandler.openUri(inputUriString) },
                            textDecoration = TextDecoration.Underline,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            inputUriString,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingIndicator(Modifier.size(96.dp), color = LocalContentColor.current)
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
                R.string.conversion_failed_parse_url_error,
                "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                initialRetryLoadingIndicatorVisible = false,
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
                R.string.conversion_failed_parse_url_error,
                "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                initialRetryLoadingIndicatorVisible = false,
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
                R.string.conversion_failed_parse_url_error,
                "41°24′12.2″N 2°10′26.5″E",
                initialRetryLoadingIndicatorVisible = false,
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
                R.string.conversion_failed_parse_url_error,
                "41°24′12.2″N 2°10′26.5″E",
                initialRetryLoadingIndicatorVisible = false,
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
                R.string.conversion_failed_parse_url_error,
                "",
                initialRetryLoadingIndicatorVisible = false,
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
                R.string.conversion_failed_parse_url_error,
                "",
                initialRetryLoadingIndicatorVisible = false,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                initialRetryLoadingIndicatorVisible = true,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                initialRetryLoadingIndicatorVisible = true,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}
