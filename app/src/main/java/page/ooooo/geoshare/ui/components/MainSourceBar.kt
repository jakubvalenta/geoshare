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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.extensions.trimUrl
import page.ooooo.geoshare.data.inputs.MatchedInput
import page.ooooo.geoshare.data.preferences.Permission
import page.ooooo.geoshare.data.state.ConversionState
import page.ooooo.geoshare.data.state.ConversionSucceeded
import page.ooooo.geoshare.data.state.ExtendedStateLog
import page.ooooo.geoshare.data.state.ExtendedStateLogItem
import page.ooooo.geoshare.data.state.PermissionGrantedBasicInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun MainSourceBar(
    conversionState: ConversionState,
    logExpanded: Boolean,
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>>,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
    onShareSource: (source: String) -> Unit,
) {
    Row {
        MainSourceButton(
            conversionState = conversionState,
            modifier = Modifier.weight(1f),
            onShareSource = onShareSource,
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
    modifier: Modifier = Modifier,
    onShareSource: (source: String) -> Unit,
) {
    val source = (conversionState as? ConversionState.HasSource)?.source ?: return
    val match = (conversionState as? ConversionState.HasMatchedInput)?.matchedInput?.match

    MainSourceButton(source, match, modifier, onShareSource)
}

@Composable
private fun MainSourceButton(
    source: String,
    match: String?,
    modifier: Modifier = Modifier,
    onShareSource: (source: String) -> Unit,
) {
    val spacing = LocalSpacing.current

    Button(
        { onShareSource(source) },
        modifier.testTag("geoShareMainSourceButton"),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = spacing.small),
    ) {
        Text(
            (match ?: source).replace('\n', ' ').trimUrl(),
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
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                onSetLogExpanded = {},
                onShareSource = {},
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
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                onSetLogExpanded = {},
                onShareSource = {},
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
                    matchedInput = MatchedInput(FakeInputRepository.openStreetMapUriInput, source),
                    points = persistentListOf(),
                ),
                logExpanded = false,
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSetLogExpanded = {},
                onShareSource = {},
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
                    matchedInput = MatchedInput(FakeInputRepository.openStreetMapUriInput, source),
                    points = persistentListOf(),
                ),
                logExpanded = false,
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSetLogExpanded = {},
                onShareSource = {},
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
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSetLogExpanded = {},
                onShareSource = {},
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
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
                onSetLogExpanded = {},
                onShareSource = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextAndEmptyLogPreview() {
    AppTheme {
        Surface {
            val source = """
                Vega de Tera Calle Barrio de Abajo
                41.9966006N, 6.1223825W
                https://mapy.com/s/deduduzeha
            """.trimIndent()
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
                onSetLogExpanded = {},
                onShareSource = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedTextAndEmptyLogPreview() {
    AppTheme {
        Surface {
            val source = """
                Vega de Tera Calle Barrio de Abajo
                41.9966006N, 6.1223825W
                https://mapy.com/s/deduduzeha
            """.trimIndent()
            val timeSource = TestTimeSource()
            MainSourceBar(
                conversionState = PermissionGrantedBasicInput(
                    source = source,
                    matchedInput = MatchedInput(FakeInputRepository.coordinateInput, source),
                    permission = Permission.ALWAYS,
                    results = emptyMap(),
                ),
                logExpanded = false,
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
                onSetLogExpanded = {},
                onShareSource = {},
            )
        }
    }
}
