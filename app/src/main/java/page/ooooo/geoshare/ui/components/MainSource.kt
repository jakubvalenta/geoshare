package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun MainSource(
    state: ConversionState,
    elapsedTime: StateFlow<Duration>,
    errorMessageResId: Int?,
    startTimeMark: StateFlow<ComparableTimeMark?>,
    finishedStateLog: StateFlow<List<ConversionStateLogItem.Finished>>,
    logExpanded: Boolean,
    source: StateFlow<String>,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val elapsedTime by elapsedTime.collectAsStateWithLifecycle()
    val finishedStateLog by finishedStateLog.collectAsStateWithLifecycle()
    val source by source.collectAsStateWithLifecycle()

    when (state) {
        is Initial -> {
            OutlinedTextField(
                value = source,
                onValueChange = {
                    onSetSource(it)
                    onSetErrorMessageResId(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.windowPadding)
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            onSubmit()
                            true
                        } else {
                            false
                        }
                    }
                    .testTag("geoShareMainSourceTextField"),
                label = {
                    Text(stringResource(R.string.main_input_uri_label))
                },
                trailingIcon = {
                    if (source.isNotEmpty()) {
                        IconButton({
                            onSetSource("")
                            onSetErrorMessageResId(null)
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                stringResource(R.string.main_input_uri_clear_content_description),
                            )
                        }
                    } else {
                        IconButton({
                            coroutineScope.launch {
                                onSetSource(AndroidTools.pasteFromClipboard(clipboard))
                                onSetErrorMessageResId(null)
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.content_paste_24px),
                                stringResource(R.string.main_input_uri_paste_content_description),
                            )
                        }
                    }
                },
                supportingText = {
                    Text(
                        stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text),
                        Modifier.padding(top = spacing.extraTiny),
                    )
                },
                isError = errorMessageResId != null,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() },
                ),
            )
        }

        is ConversionState.HasSource -> {
            Card(Modifier.padding(bottom = spacing.tiny)) {
                TwoSlotRow(
                    modifier = Modifier.height(30.dp),
                    firstContent = { paddingValues ->
                        MainSourceUriButton(
                            source = source,
                            paddingValues = paddingValues,
                        )
                    },
                    secondContent = mainSourceTimeButton(
                        state = state,
                        elapsedTime = elapsedTime,
                        startTimeMark = startTimeMark,
                        finishedStateLog = finishedStateLog,
                        logExpanded = logExpanded,
                        onSetLogExpanded = onSetLogExpanded,
                    ),
                    gap = spacing.tiny,
                    paddingValues = PaddingValues(horizontal = spacing.small),
                )
            }
        }
    }
}

@Composable
private fun RowScope.MainSourceUriButton(
    source: String,
    paddingValues: PaddingValues,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    AndroidTools.copyToClipboard(clipboard, source)
                }
            }
            .weight(1f)
            .fillMaxHeight()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            source,
            textDecoration = TextDecoration.Underline,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun mainSourceTimeButton(
    state: ConversionState,
    elapsedTime: Duration,
    startTimeMark: StateFlow<ComparableTimeMark?>,
    finishedStateLog: List<ConversionStateLogItem.Finished>,
    logExpanded: Boolean,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
): (@Composable RowScope.(paddingValues: PaddingValues) -> Unit)? {
    val text: (@Composable RowScope.(paddingValues: PaddingValues) -> Unit)? = when (state) {
        is ConversionState.HasError,
        is ConversionState.HasResult,
            ->
            if (elapsedTime > 10.milliseconds) {
                { paddingValues ->
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        SecondsTimeText(elapsedTime, Modifier.padding(paddingValues))
                    }
                }
            } else {
                null
            }

        is ConversionState.HasDescription -> {
            { paddingValues ->
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    ElapsedTimeText(startTimeMark, Modifier.padding(paddingValues))
                }
            }
        }

        else -> null
    }
    val icon: (@Composable RowScope.(paddingValues: PaddingValues) -> Unit)? = if (finishedStateLog.isNotEmpty()) {
        { paddingValues ->
            val layoutDirection = LocalLayoutDirection.current
            Icon(
                if (logExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        top = paddingValues.calculateTopPadding(),
                        // Adjust end padding, because the icon visually has empty space at the end
                        end = paddingValues.calculateEndPadding(layoutDirection) - 10.dp,
                        bottom = paddingValues.calculateTopPadding(),
                    )
                ),
            )
        }
    } else {
        null
    }

    return if (icon != null || text != null) {
        { paddingValues ->
            TwoSlotRow(
                Modifier
                    .clickable(
                        enabled = finishedStateLog.isNotEmpty(),
                        onClick = { onSetLogExpanded(!logExpanded) },
                    )
                    .fillMaxHeight(),
                firstContent = text,
                secondContent = icon,
                paddingValues = paddingValues,
                verticalAlignment = Alignment.CenterVertically,
            )
        }
    } else {
        null
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(Duration.ZERO),
                errorMessageResId = null,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow(""),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(Duration.ZERO),
                errorMessageResId = null,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow(""),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilledPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(Duration.ZERO),
                errorMessageResId = null,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFilledPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(Duration.ZERO),
                errorMessageResId = null,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(657.milliseconds),
                errorMessageResId = R.string.conversion_failed_missing_url,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface {
            MainSource(
                state = Initial,
                elapsedTime = MutableStateFlow(657.milliseconds),
                errorMessageResId = R.string.conversion_failed_missing_url,
                finishedStateLog = MutableStateFlow(emptyList()),
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
                onSetLogExpanded = {},
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmittedPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            logExpanded = false,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            logExpanded = false,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmittedExpandedLogPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            logExpanded = true,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedExpandedLogPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(fakeFinishedStateLog()),
            logExpanded = true,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmittedEmptyLogPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(emptyList()),
            logExpanded = false,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedEmptyLogPreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(emptyList()),
            logExpanded = false,
            startTimeMark = MutableStateFlow(timeSource.markNow()),
            source = MutableStateFlow(source),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmittedNoTimePreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        MainSource(
            state = ConversionSucceeded(
                source = source,
                points = persistentListOf(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(emptyList()),
            logExpanded = false,
            source = MutableStateFlow(source),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedNoTimePreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        val timeSource = TestTimeSource()
        MainSource(
            state = ConversionSucceeded(
                source = source,
                points = persistentListOf(),
            ),
            elapsedTime = MutableStateFlow(Duration.ZERO),
            errorMessageResId = null,
            finishedStateLog = MutableStateFlow(emptyList()),
            logExpanded = false,
            startTimeMark = MutableStateFlow(timeSource.markNow()),
            source = MutableStateFlow(source),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}
