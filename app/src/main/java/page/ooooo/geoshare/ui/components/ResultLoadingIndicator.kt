package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultLoadingIndicator(
    state: ConversionState.HasDescription,
    startTimeMark: StateFlow<ComparableTimeMark?>,
    initialExpanded: Boolean = false,
    onCancel: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
        SelectionContainer {
            ResultDescription(
                state,
                initialExpanded = initialExpanded,
                testTag = "geoShareMainLoadingIndicatorDescription",
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                ResultLoadingIndicatorElapsedTime(startTimeMark)
            }
        }
        Button(
            onCancel,
            Modifier
                .align(Alignment.CenterHorizontally)
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

@Composable
private fun ResultLoadingIndicatorElapsedTime(startTime: StateFlow<ComparableTimeMark?>) {
    val startTime by startTime.collectAsStateWithLifecycle()

    var elapsedTime by remember { mutableStateOf(startTime?.elapsedNow() ?: Duration.ZERO) }

    LaunchedEffect(startTime) {
        startTime?.let { startTime ->
            while (true) {
                elapsedTime = startTime.elapsedNow()
                delay(100.milliseconds)
            }
        }
    }

    ResultTime(elapsedTime)
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(3, ConnectTimeoutNetworkException(Exception())),
        )
        val timeSource = TestTimeSource()
        Surface(color = mainContainerColor(state)) {
            ResultLoadingIndicator(
                state = state,
                startTimeMark = MutableStateFlow(timeSource.apply { plusAssign(123.milliseconds) }.markNow()),
                onCancel = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(3, ConnectTimeoutNetworkException(Exception())),
        )
        val timeSource = TestTimeSource()
        Surface(color = mainContainerColor(state)) {
            ResultLoadingIndicator(
                state = state,
                startTimeMark = MutableStateFlow(timeSource.apply { plusAssign(123.milliseconds) }.markNow()),
                onCancel = {},
            )
        }
    }
}
