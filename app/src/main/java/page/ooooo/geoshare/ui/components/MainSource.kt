package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.ExtendedConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun MainSource(
    state: ConversionState,
    errorMessageResId: Int?,
    logExpanded: Boolean,
    source: StateFlow<String>,
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<List<ExtendedConversionStateLogItem>>,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val source by source.collectAsStateWithLifecycle()
    val stateLog by stateLog.collectAsStateWithLifecycle()

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
            Row(Modifier.padding(bottom = spacing.tiny)) {
                ThinButton(
                    {
                        coroutineScope.launch {
                            AndroidTools.copyToClipboard(clipboard, source)
                        }
                    },
                    Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        source,
                        textDecoration = TextDecoration.Underline,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
                MainSourceTimeButton(
                    start = start,
                    stateLog = stateLog,
                    logExpanded = logExpanded,
                    onSetLogExpanded = onSetLogExpanded,
                )
            }
        }
    }
}

@Composable
private fun MainSourceTimeButton(
    start: StateFlow<ComparableTimeMark?>,
    stateLog: List<ExtendedConversionStateLogItem>,
    logExpanded: Boolean,
    textPadding: Dp = LocalSpacing.current.small,
    iconPadding: Dp = textPadding - 10.dp,
    onSetLogExpanded: (logExpanded: Boolean) -> Unit,
) {
    val lastLogItem = stateLog.lastOrNull() ?: return
    val start by start.collectAsStateWithLifecycle()

    val text = start?.let { start ->
        when (lastLogItem) {
            is ExtendedConversionStateLogItem.Finished -> {
                val elapsedTime = lastLogItem.end - start
                if (elapsedTime > 10.milliseconds) {
                    @Composable {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                            SecondsTimeText(elapsedTime)
                        }
                    }
                } else {
                    null
                }
            }

            is ExtendedConversionStateLogItem.Pending -> {
                @Composable {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        ElapsedTimeText(start)
                    }
                }
            }
        }
    }
    val icon = if (stateLog.isNotEmpty()) {
        @Composable {
            Icon(if (logExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
        }
    } else {
        null
    }

    if (icon != null || text != null) {
        ThinButton(
            { onSetLogExpanded(!logExpanded) },
            modifier = Modifier.testTag("geoShareMainSourceIcon"),
            enabled = stateLog.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(
                start = if (text != null) textPadding else iconPadding,
                end = if (icon != null) iconPadding else textPadding,
            ),
        ) {
            if (text != null) {
                text()
            }
            if (icon != null) {
                icon()
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = null,
                logExpanded = false,
                source = MutableStateFlow(""),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = null,
                logExpanded = false,
                source = MutableStateFlow(""),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = null,
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = null,
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = R.string.conversion_failed_missing_url,
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
            val timeSource = TestTimeSource()
            MainSource(
                state = Initial,
                errorMessageResId = R.string.conversion_failed_missing_url,
                logExpanded = false,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                start = MutableStateFlow(timeSource.markNow()),
                stateLog = MutableStateFlow(emptyList()),
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
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            errorMessageResId = null,
            logExpanded = true,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            errorMessageResId = null,
            logExpanded = true,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmittedShortTimePreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        val timeSource = TestTimeSource()
        MainSource(
            state = ConversionSucceeded(
                source = source,
                points = persistentListOf(),
            ),
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSubmittedShortTimePreview() {
    AppTheme {
        val source = "https://www.openstreetmap.org/#map=16/27.092414/30.377172"
        val timeSource = TestTimeSource()
        MainSource(
            state = ConversionSucceeded(
                source = source,
                points = persistentListOf(),
            ),
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource).take(1)),
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
        val timeSource = TestTimeSource()
        MainSource(
            state = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
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
            errorMessageResId = null,
            logExpanded = false,
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
            onSetLogExpanded = {},
            onSetErrorMessageResId = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}
