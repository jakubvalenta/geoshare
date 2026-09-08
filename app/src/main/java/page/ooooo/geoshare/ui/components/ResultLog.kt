package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.delay
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TestTimeSource

@Composable
fun ResultLog(
    stateLog: StateFlow<List<ConversionStateLogItem>>,
    animationsEnabled: Boolean = false,
    initialExpanded: Boolean = false,
) {
    val stateLog by stateLog.collectAsStateWithLifecycle()

    SelectionContainer {
        Column {
            for (item in stateLog) {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultLogItem(
    item: ConversionStateLogItem,
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
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
    ) {
        Column {
            HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.5f))
            Column(
                Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.tiny),
                verticalArrangement = Arrangement.spacedBy(spacing.extraTiny),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                ) {
                    when (item) {
                        is ConversionStateLogItem.Finished ->
                            Icon(if (item.succeeded) Icons.Default.Check else Icons.Default.Close, null)

                        is ConversionStateLogItem.Pending ->
                            LoadingIndicator(Modifier.size(24.dp), color = LocalContentColor.current.copy(alpha = 0.9f))
                    }
                    Text(
                        item.state.getDescription(resources),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    ResultLogItemTimeElapsed(item)
                }
                item.state.getDetails(resources)?.let { details ->
                    ResultDetails(details, Modifier.padding(start = 24.dp + spacing.tiny), initialExpanded)
                }
                item.state.uri?.let { uri ->
                    ResultUri(uri, Modifier.padding(start = 24.dp + spacing.tiny))
                }
            }
        }
    }
}

@Composable
private fun ResultLogItemTimeElapsed(item: ConversionStateLogItem) {
    var elapsedTime by remember {
        mutableStateOf(
            when (item) {
                is ConversionStateLogItem.Finished -> item.elapsedTime
                is ConversionStateLogItem.Pending -> Duration.ZERO
            }
        )
    }

    LaunchedEffect(item) {
        when (item) {
            is ConversionStateLogItem.Finished -> {
                elapsedTime = item.elapsedTime
            }

            is ConversionStateLogItem.Pending -> {
                while (true) {
                    elapsedTime = item.timeMark.elapsedNow()
                    delay(100.milliseconds)
                }
            }
        }
    }

    Text(
        elapsedTime.toString(DurationUnit.SECONDS, 2),
        style = MaterialTheme.typography.bodySmall,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            ResultLog(
                stateLog = MutableStateFlow(
                    listOf(
                        ConversionStateLogItem.Pending(
                            id = 5,
                            state = PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeMark = timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateLogItem.Finished(
                            id = 4,
                            state = ConversionFailed(
                                source,
                                message = stringResource(R.string.conversion_failed_reason_no_points),
                                stackTrace = NotImplementedError().stackTraceToString(),
                            ),
                            succeeded = false,
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
                            elapsedTime = 657.milliseconds,
                        ),
                    ),
                ),
                animationsEnabled = false,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.errorContainer) {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            ResultLog(
                stateLog = MutableStateFlow(
                    listOf(
                        ConversionStateLogItem.Pending(
                            id = 5,
                            state = PermissionGrantedBasicInput(
                                source,
                                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                                permission = Permission.ALWAYS,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeMark = timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateLogItem.Finished(
                            id = 4,
                            state = ConversionFailed(
                                source,
                                message = stringResource(R.string.conversion_failed_reason_no_points),
                                stackTrace = NotImplementedError().stackTraceToString(),
                            ),
                            succeeded = false,
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
                            elapsedTime = 657.milliseconds,
                        ),
                    ),
                ),
                animationsEnabled = false,
            )
        }
    }
}
