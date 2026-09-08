package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
fun ResultLog(
    stateLog: StateFlow<List<ConversionStateLogItem>>,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = false,
    initialExpanded: Boolean = false,
) {
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
    val stateLog by stateLog.collectAsStateWithLifecycle()

    if (stateLog.isNotEmpty()) {
        SelectionContainer {
            Column(
                modifier,
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                stateLog
                    .filterIsInstance<ConversionStateLogItem.Finished>()
                    .run {
                        forEachIndexed { index, item ->
                            SegmentedListItem(
                                shapes = ListItemDefaults.segmentedShapes(index, size),
                                colors = colors,
                            ) {
                                key(item.id) {
                                    ResultLogItem(
                                        item = item,
                                        animationsEnabled = animationsEnabled,
                                        initialExpanded = initialExpanded,
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
)

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ResultLog(
            stateLog = MutableStateFlow(fakeStateLog()),
            animationsEnabled = false,
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        ResultLog(
            stateLog = MutableStateFlow(fakeStateLog()),
            animationsEnabled = false,
        )
    }
}
