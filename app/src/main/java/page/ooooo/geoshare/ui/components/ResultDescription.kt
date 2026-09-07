package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateHistoryItem
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TestTimeSource

fun LazyListScope.resultDescription(
    stateHistory: List<ConversionStateHistoryItem>,
    animationsEnabled: Boolean = true,
    initialExpanded: Boolean = false,
    onCancel: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    items(
        stateHistory
            .zipWithNext()
            .run { stateHistory.lastOrNull()?.let { plus(it to null) } ?: this }
            .reversed(),
        key = { it.hashCode() },
    ) { (item, nextItem) ->
        ResultDescriptionItem(
            item = item,
            nextItem = nextItem,
            animationsEnabled = animationsEnabled,
            initialExpanded = initialExpanded,
            onCancel = onCancel,
            onNavigateToInputsScreen = onNavigateToInputsScreen,
            onRetry = onRetry,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LazyItemScope.ResultDescriptionItem(
    item: ConversionStateHistoryItem,
    nextItem: ConversionStateHistoryItem?,
    animationsEnabled: Boolean = true,
    initialExpanded: Boolean = false,
    onCancel: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onRetry: () -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current
    val uriHandler = LocalUriHandler.current

    // TODO Stop clock on error
    var elapsed by remember { mutableStateOf(nextItem?.timeMark?.minus(item.timeMark) ?: Duration.ZERO) }

    LaunchedEffect(nextItem) {
        while (nextItem == null) {
            elapsed = item.timeMark.elapsedNow()
            delay(100.milliseconds)
        }
        nextItem.timeMark - item.timeMark
    }

    Column(
        Modifier
            .run {
                if (animationsEnabled) {
                    animateItem()
                } else {
                    this
                }
            }
    ) {
        if (nextItem != null) {
            HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.5f))
        }
        Row(
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .run {
                    if (nextItem != null) {
                        padding(vertical = spacing.tiny)
                    } else {
                        this
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny),
        ) {
            if (nextItem != null) {
                Icon(
                    if (
                        item.conversionState is ConversionState.HasError ||
                        (nextItem.conversionState as? ConversionState.HasAttempt)?.lastAttempt != null
                    ) {
                        Icons.Default.Close
                    } else {
                        Icons.Default.Check
                    },
                    null,
                )
            }
            Column(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    Row(
                        Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                item.conversionState.getDescription(resources),
                                Modifier
                                    .weight(1f)
                                    .testTag("geoShareConversionErrorMessage"), // TODO
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                        Text(elapsed.toString(DurationUnit.SECONDS, 2))
                    }
                }
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                    LocalContentColor provides LocalContentColor.current.copy(alpha = 0.9f),
                ) {
                    item.conversionState.getDetails(resources)?.let { details ->
                        val newLineIndex = details.indexOf('\n')
                        if (newLineIndex > -1) {
                            val firstLine = details.substring(0, newLineIndex)
                            var expanded by remember { mutableStateOf(initialExpanded) }
                            ExpandablePane(
                                expanded = expanded,
                                onSetExpanded = { expanded = it },
                                title = {
                                    Text(
                                        firstLine,
                                        Modifier
                                            .weight(1f)
                                            .testTag("geoShareMainLoadingIndicatorDescription"),
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )
                                },
                                modifier = Modifier.padding(top = spacing.tiny),
                            ) {
                                SelectionContainer {
                                    Text(
                                        details,
                                        Modifier.padding(top = spacing.tiny),
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 25,
                                    )
                                }
                            }
                        } else {
                            SelectionContainer(modifier = Modifier.padding(top = spacing.tiny)) {
                                Text(
                                    details,
                                    Modifier.testTag("geoShareMainLoadingIndicatorDescription"),
                                )
                            }
                        }
                    }
                    item.conversionState.uri?.ifEmpty { null }?.let { uri ->
                        SelectionContainer(modifier = Modifier.padding(top = spacing.tiny)) {
                            if (uri.startsWith("https://")) {
                                Text(
                                    uri,
                                    modifier = Modifier.clickable { uriHandler.openUri(uri) },
                                    textDecoration = TextDecoration.Underline,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )
                            } else {
                                Text(
                                    uri,
                                    fontStyle = FontStyle.Italic,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                if (nextItem == null && item.conversionState !is ConversionState.HasError) {
                    Button(
                        onCancel,
                        Modifier
                            .padding(vertical = spacing.small)
                            .testTag("geoShareMainLoadingIndicatorCancel"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(stringResource(R.string.conversion_loading_indicator_cancel))
                    }
                }
            }
        }
        if (nextItem == null && item.conversionState is ConversionState.HasError) {
            ScrollableChips {
                if (!item.conversionState.warning) {
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
}

// Previews

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = NotImplementedError().stackTraceToString(),
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = NotImplementedError().stackTraceToString(),
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedErrorPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = NotImplementedError().stackTraceToString(),
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    initialExpanded = true,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkExpandedErrorPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = NotImplementedError().stackTraceToString(),
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    initialExpanded = true,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoDetailsPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = ""
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoDetailsPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = ""
            val message = stringResource(R.string.conversion_failed_reason_no_points)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                warning = false,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        ),
                    ),
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            val source = "https://share.google/diIxnYa8dIA6dZfpy"
            val message = stringResource(R.string.conversion_failed_unsupported_source_google_search)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleSearchUriInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = null,
                                warning = true,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        )
                    ),
                    animationsEnabled = false,
                    initialExpanded = true,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWarningPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            val source = "https://share.google/diIxnYa8dIA6dZfpy"
            val message = stringResource(R.string.conversion_failed_unsupported_source_google_search)
            val timeSource = TestTimeSource()
            LazyColumn {
                resultDescription(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleSearchUriInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            ConversionFailed(
                                source = source,
                                message = message,
                                stackTrace = null,
                                warning = true,
                            ),
                            timeSource.apply { plusAssign(50.milliseconds) }.markNow(),
                        )
                    ),
                    animationsEnabled = false,
                    initialExpanded = true,
                    onCancel = {},
                    onNavigateToInputsScreen = {},
                    onRetry = {},
                )
            }
        }
    }
}
