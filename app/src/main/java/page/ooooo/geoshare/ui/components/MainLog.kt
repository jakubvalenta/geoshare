package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource

@Composable
fun MainLog(
    expanded: Boolean,
    finishedStateLog: StateFlow<List<ConversionStateLogItem.Finished>>,
    animationsEnabled: Boolean = true,
    initialItemsExpanded: Boolean = false,
) {
    val colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val spacing = LocalSpacing.current

    val finishedStateLog by finishedStateLog.collectAsStateWithLifecycle()

    if (finishedStateLog.isNotEmpty()) {
        AnimatedVisibility(
            expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            SelectionContainer {
                Column(
                    Modifier.padding(bottom = spacing.tiny),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    finishedStateLog.forEachIndexed { index, item ->
                        SegmentedListItem(
                            shapes = ListItemDefaults.segmentedShapes(index, finishedStateLog.size),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultLogItem(
    item: ConversionStateLogItem.Finished,
    animationsEnabled: Boolean = true,
    initialExpanded: Boolean = false,
) {
    val resources = LocalResources.current
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
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                Icon(if (item.succeeded) Icons.Default.Check else Icons.Default.Close, null)
                Text(
                    item.state.getDescription(resources),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                    SecondsTimeText(item.elapsedTime)
                }
            }
            item.state.getDetails(resources)?.let { details ->
                ResultDetails(
                    details,
                    Modifier.padding(start = 24.dp + spacing.tiny),
                    initialExpanded = initialExpanded
                )
            }
            item.state.uri?.let { uri ->
                ResultUri(
                    uri,
                    Modifier.padding(start = 24.dp + spacing.tiny),
                )
            }
        }
    }
}

@Composable
fun fakeFinishedStateLog(
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
        elapsedTime = 2011.milliseconds,
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
)

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MainLog(
            expanded = true,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            animationsEnabled = false,
            initialItemsExpanded = true,
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainLog(
            expanded = true,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            animationsEnabled = false,
            initialItemsExpanded = true,
        )
    }
}
