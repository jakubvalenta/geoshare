package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.state.ConversionState
import page.ooooo.geoshare.lib.state.ConversionSucceeded
import page.ooooo.geoshare.lib.state.ExtendedStateLog
import page.ooooo.geoshare.lib.state.ExtendedStateLogItem
import page.ooooo.geoshare.lib.state.PermissionGrantedBasicInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun MainSourceBar(
    conversionState: ConversionState,
    logExpanded: Boolean,
    source: StateFlow<String>,
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>>,
    onSelectUri: (uriString: String) -> Unit,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
) {
    Row {
        MainSourceButton(
            conversionState = conversionState,
            source = source,
            modifier = Modifier.weight(1f),
            onSelectUri = onSelectUri,
        )
        MainSourceTimeButton(
            logExpanded = logExpanded,
            start = start,
            stateLog = stateLog,
            onSetLogExpanded = onSetLogExpanded,
        )
    }
}

@Composable
private fun MainSourceButton(
    conversionState: ConversionState,
    source: StateFlow<String>,
    modifier: Modifier = Modifier,
    onSelectUri: (uriString: String) -> Unit,
) {
    val spacing = LocalSpacing.current

    val source by source.collectAsStateWithLifecycle()
    val match = remember(conversionState) {
        when (conversionState) {
            is ConversionState.HasMatchedInput -> conversionState.matchedInput.match
            is ConversionState.HasSource -> conversionState.source
            else -> ""
        }
            .replace('\n', ' ')
            .removePrefix("https://")
    }

    Button(
        { onSelectUri(source) },
        modifier.testTag("geoShareMainSourceButton"),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = spacing.small),
    ) {
        Text(
            match,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun MainSourceTimeButton(
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>>,
    logExpanded: Boolean,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current

    val stateLog by stateLog.collectAsStateWithLifecycle()
    val lastLogItem = stateLog.lastOrNull() ?: return
    val start by start.collectAsStateWithLifecycle()

    start?.let { start ->
        when (lastLogItem) {
            is ExtendedStateLogItem.Finished -> {
                val elapsedTime = lastLogItem.end - start
                if (logExpanded || elapsedTime > 10.milliseconds) {
                    Button(
                        { onSetLogExpanded(!logExpanded) },
                        modifier = Modifier.testTag("geoShareMainSourceIcon"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        contentPadding = PaddingValues(horizontal = spacing.tiny),
                    ) {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                            SecondsTimeText(elapsedTime)
                        }
                    }
                } else {
                    null
                }
            }

            is ExtendedStateLogItem.Pending ->
                Button(
                    { onSetLogExpanded(!logExpanded) },
                    modifier = Modifier.testTag("geoShareMainSourceIcon"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    contentPadding = PaddingValues(horizontal = spacing.tiny),
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                        ElapsedTimeText(start)
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
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShortTimePreview() {
    AppTheme {
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = ConversionSucceeded(
                    source = source,
                    points = persistentListOf(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkShortTimePreview() {
    AppTheme {
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = ConversionSucceeded(
                    source = source,
                    points = persistentListOf(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedShortTimePreview() {
    AppTheme {
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = true,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkExpandedShortTimePreview() {
    AppTheme {
        Surface {
            val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = true,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextAndEmptyLogPreview() {
    AppTheme {
        Surface {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedTextAndEmptyLogPreview() {
    AppTheme {
        Surface {
            val source = "41°24′12.2″N 2°10′26.5″E"
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                source = MutableStateFlow(source),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
                onSelectUri = {},
                onSetLogExpanded = {},
            )
        }
    }
}
