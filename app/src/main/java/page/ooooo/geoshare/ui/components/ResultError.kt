package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultError(
    state: ConversionState.HasError,
    initialExpanded: Boolean = false,
    onNavigateToInputsScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.conversion_error_title),
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.small, bottom = spacing.small),
            style = MaterialTheme.typography.headlineSmall,
        )
        SelectionContainer {
            Column(
                Modifier.padding(horizontal = spacing.windowPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                Text(
                    state.message,
                    Modifier.testTag("geoShareConversionErrorMessage"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                state.stackTrace?.let { details ->
                    ResultDetails(
                        details,
                        initialExpanded = initialExpanded,
                    )
                }
                ResultUri(state.source)
            }
        }
        ScrollableChips {
            if (!state.warning) {
                item {
                    StyledChip(
                        stringResource(R.string.conversion_error_retry),
                        icon = {
                            Icon(Icons.Default.Refresh, null)
                        },
                        onClick = onRetry,
                    )
                }
                item {
                    val uriHandler = LocalUriHandler.current
                    StyledChip(
                        stringResource(R.string.conversion_error_report),
                    ) {
                        uriHandler.openUri("https://github.com/jakubvalenta/geoshare/issues/new?template=1-bug-map-link.yml")
                    }
                }
            }
            item {
                StyledChip(
                    stringResource(R.string.inputs_title),
                    icon = {
                        Icon(painterResource(R.drawable.map_24px), null)
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
        val state = ConversionFailed(
            source = "41°24′12.2″N 2°10′26.5″E",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            stackTrace = NotImplementedError().stackTraceToString(),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
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
        val state = ConversionFailed(
            source = "41°24′12.2″N 2°10′26.5″E",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            stackTrace = NotImplementedError().stackTraceToString(),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
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
        val state = ConversionFailed(
            source = "41°24′12.2″N 2°10′26.5″E",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            stackTrace = NotImplementedError().stackTraceToString(),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
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
        val state = ConversionFailed(
            source = "41°24′12.2″N 2°10′26.5″E",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            stackTrace = NotImplementedError().stackTraceToString(),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
                initialExpanded = true,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoDetailsPreview() {
    AppTheme {
        val state = ConversionFailed(
            source = "",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoDetailsPreview() {
    AppTheme {
        val state = ConversionFailed(
            source = "",
            message = stringResource(R.string.conversion_failed_reason_no_points),
            warning = false,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningPreview() {
    AppTheme {
        val state = ConversionFailed(
            source = "https://share.google/diIxnYa8dIA6dZfpy",
            message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
            warning = true,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWarningPreview() {
    AppTheme {
        val state = ConversionFailed(
            source = "https://share.google/diIxnYa8dIA6dZfpy",
            message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
            warning = true,
        )
        Surface(color = mainContainerColor(state)) {
            ResultError(
                state = state,
                onNavigateToInputsScreen = {},
                onRetry = {},
            )
        }
    }
}
