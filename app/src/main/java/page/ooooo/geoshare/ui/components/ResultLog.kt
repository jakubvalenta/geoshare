package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource

@Composable
fun ResultLog(
    currentState: ConversionState,
    stateLog: StateFlow<List<ConversionStateLogItem>>,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    sourceComesFromIntent: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    initialExpanded: Boolean = false,
    initialItemsExpanded: Boolean = false,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val clipboard = LocalClipboard.current
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    var expanded by remember { mutableStateOf(initialExpanded) }
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()
    val stateLog by stateLog.collectAsStateWithLifecycle()
    val filteredStateLog = stateLog.filterIsInstance<ConversionStateLogItem.Finished>()

    if (currentState is ConversionState.HasSource) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
            Column {
                Card {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(
                            Modifier
                                .weight(1f)
                                .padding(vertical = spacing.tiny)
                                .padding(start = spacing.small)
                        ) {
                            Column {
                                Text(
                                    "Source", // TODO translate
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    currentState.source,
                                    modifier = modifier.clickable {
                                        coroutineScope.launch {
                                            AndroidTools.copyToClipboard(clipboard, currentState.source)
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.Underline,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        if (filteredStateLog.isNotEmpty()) {
                            IconButton({ expanded = !expanded }) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            Spacer(Modifier.width(spacing.small))
                        }
                    }
                }
                if (filteredStateLog.isNotEmpty()) {
                    AnimatedVisibility(expanded) {
                        SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                                filteredStateLog.forEachIndexed { index, item ->
                                    SegmentedListItem(
                                        shapes = ListItemDefaults.segmentedShapes(index, filteredStateLog.size),
                                        colors = colors,
                                    ) {
                                        key(item.id) {
                                            ResultLogItem(
                                                item = item,
                                                animationsEnabled = animationsEnabled,
                                                initialExpanded = initialItemsExpanded,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // TODO Move OPEN_BY_DEFAULT help message somewhere else
            if (currentState is ConversionState.HasResult && sourceComesFromIntent) {
                HelpMessageCard(
                    helpMessage = HelpMessage.OPEN_BY_DEFAULT,
                    dismissedHelpMessages = dismissedHelpMessages,
                    title = { Text(stringResource(R.string.help_open_by_default_title, appName)) },
                    actionText = {
                        stringResource(R.string.help_open_by_default_action)
                    },
                    onAction = {
                        onNavigateToFaqScreen(FaqItemId.OPEN_BY_DEFAULT)
                    },
                    onDismiss = onDismissHelpMessage,
                ) {
                    ParagraphText(
                        stringResource(R.string.help_open_by_default_text, appName)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultLogItem(
    item: ConversionStateLogItem.Finished,
    animationsEnabled: Boolean = true,
    initialExpanded: Boolean = false,
) {
    val spacing = LocalSpacing.current

    var visible by remember { mutableStateOf(!animationsEnabled) }

    // Trigger the enter animation on first composition
    LaunchedEffect(Unit) {
        if (animationsEnabled) {
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
    ) {
        ResultDescription(
            item.state,
            icon = { Icon(if (item.succeeded) Icons.Default.Check else Icons.Default.Close, null) },
            initialExpanded = initialExpanded,
            verticalArrangement = Arrangement.spacedBy(spacing.extraTiny),
        ) {
            ResultTime(item.elapsedTime)
        }
    }
}

@Composable
fun fakeStateLog(
    source: String = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
    timeSource: TimeSource.WithComparableMarks = TestTimeSource(),
) = listOf(
    ConversionStateLogItem.Finished(
        id = 0,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = true,
        startTimeMark = timeSource.markNow(),
        elapsedTime = 657.milliseconds,
    ),
    ConversionStateLogItem.Finished(
        id = 1,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsUriInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = true,
        startTimeMark = timeSource.markNow(),
        elapsedTime = 30.milliseconds,
    ),
    ConversionStateLogItem.Finished(
        id = 2,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = false,
        startTimeMark = timeSource.markNow(),
        elapsedTime = 111.milliseconds,
    ),
    ConversionStateLogItem.Finished(
        id = 3,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(1, ConnectTimeoutNetworkException(Exception())),
        ),
        succeeded = false,
        startTimeMark = timeSource.markNow(),
        elapsedTime = 200.milliseconds,
    ),
    ConversionStateLogItem.Finished(
        id = 4,
        state = ConversionFailed(
            source,
            message = stringResource(R.string.conversion_failed_reason_no_points),
            stackTrace = NotImplementedError().stackTraceToString(),
        ),
        succeeded = false,
        startTimeMark = timeSource.markNow(),
        elapsedTime = 92.milliseconds,
    ),
    ConversionStateLogItem.Pending(
        id = 5,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
        ),
        startTimeMark = timeSource.markNow(),
    ),
)

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            initialExpanded = true,
            initialItemsExpanded = true,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkExpandedPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            initialExpanded = true,
            initialItemsExpanded = true,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(emptyList()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        ResultLog(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            stateLog = MutableStateFlow(emptyList()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HelpPreview() {
    AppTheme {
        ResultLog(
            currentState = ConversionSucceeded(
                source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172",
                points = persistentListOf(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkHelpPreview() {
    AppTheme {
        ResultLog(
            currentState = ConversionSucceeded(
                source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172",
                points = persistentListOf(),
            ),
            stateLog = MutableStateFlow(fakeStateLog()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            animationsEnabled = false,
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}
