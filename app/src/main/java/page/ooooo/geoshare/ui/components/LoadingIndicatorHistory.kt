package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversion.ConversionStateHistoryItem
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.InputMatched
import page.ooooo.geoshare.lib.conversion.PermissionGranted
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.theme.AppTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun LoadingIndicatorHistory(stateHistory: StateFlow<List<ConversionStateHistoryItem>>) {
    var elapsed by remember { mutableStateOf(Duration.ZERO) }
    val stateHistory by stateHistory.collectAsStateWithLifecycle()
    val stateHistoryPairs = stateHistory.zipWithNext()
    val stateHistoryLastItem = stateHistory.lastOrNull()

    LaunchedEffect(stateHistoryLastItem) {
        while (stateHistoryLastItem != null) {
            elapsed = stateHistoryLastItem.timeMark.elapsedNow()
            delay(100.milliseconds)
        }
    }

    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
        LazyColumn {
            if (stateHistoryLastItem != null) {
                item(stateHistoryLastItem.hashCode()) {
                    AnimatedVisibility(true) {
                        LoadingIndicatorHistoryLine(stateHistoryLastItem.conversionState.toString(), elapsed)
                    }
                }
            }
            items(stateHistoryPairs, key = { it.hashCode() }) { (item, nextItem) ->
                LoadingIndicatorHistoryLine(item.conversionState.toString(), nextItem.timeMark - item.timeMark)
            }
        }
    }
}

@Composable
private fun LoadingIndicatorHistoryLine(text: String, elapsed: Duration) {
    Row {
        Text(
            text,
            Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        Text("${elapsed.inWholeMilliseconds}ms")
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val matchedInput: MatchedInput<BasicInput<Uri>> = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            )
            val permission = Permission.ALWAYS
            val timeSource = TestTimeSource()
            LoadingIndicatorHistory(
                stateHistory = MutableStateFlow(
                    listOf(
                        ConversionStateHistoryItem(
                            Initial,
                            timeSource.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            SourceReceived(source),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            InputMatched(source, matchedInput, permission),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGranted(source, matchedInput, permission, results = emptyMap()),
                            timeSource.apply { plusAssign(20.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput,
                                permission,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val matchedInput: MatchedInput<BasicInput<Uri>> = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            )
            val permission = Permission.ALWAYS
            val timeSource = TestTimeSource()
            LoadingIndicatorHistory(
                stateHistory = MutableStateFlow(
                    listOf(
                        ConversionStateHistoryItem(
                            Initial,
                            timeSource.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            SourceReceived(source),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            InputMatched(source, matchedInput, permission),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGranted(source, matchedInput, permission, results = emptyMap()),
                            timeSource.apply { plusAssign(20.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput,
                                permission,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                ),
            )
        }
    }
}
