package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultError(
    @StringRes errorMessageResId: Int,
    inputUriString: String,
    retryLoadingIndicatorVisible: Boolean,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.errorContainer
    val contentColor = MaterialTheme.colorScheme.onErrorContainer
    val uriHandler = LocalUriHandler.current

    ResultCard(
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .testTag("geoShareConversionError")
            .fillMaxWidth(),
        chips = { lastPaddingEnd ->
            if (!retryLoadingIndicatorVisible) {
                ResultCardChip(
                    stringResource(R.string.conversion_error_retry),
                    icon = {
                        Icon(Icons.Default.Refresh, null)
                    },
                    onClick = onRetry,
                )
                ResultCardChip(
                    stringResource(R.string.conversion_error_report),
                ) {
                    uriHandler.openUri("https://github.com/jakubvalenta/geoshare/issues/new?template=1-bug-map-link.yml")
                }
                ResultCardChip(
                    stringResource(R.string.url_converters_title),
                    icon = {
                        Icon(Icons.Outlined.Info, null)
                    },
                    modifier = Modifier.padding(end = lastPaddingEnd),
                ) {
                    onNavigateToUrlConvertersScreen()
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
                LoadingIndicator(Modifier.size(96.dp), color = contentColor)
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
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinatesPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "41°24′12.2″N 2°10′26.5″E",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkCoordinatesPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "41°24′12.2″N 2°10′26.5″E",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                retryLoadingIndicatorVisible = false,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                retryLoadingIndicatorVisible = true,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        Surface {
            ResultError(
                R.string.conversion_failed_parse_url_error,
                "",
                retryLoadingIndicatorVisible = true,
                onNavigateToUrlConvertersScreen = {},
                onRetry = {},
            )
        }
    }
}
