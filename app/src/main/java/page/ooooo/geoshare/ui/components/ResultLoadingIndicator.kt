package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun ResultLoadingIndicator(
    state: ConversionState.HasDescription,
    initialExpanded: Boolean = false,
    onCancel: () -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                Text(
                    state.getDescription(resources),
                    Modifier.testTag("geoShareMainLoadingIndicatorDescription"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                state.getDetails(resources)?.let { details ->
                    ResultDetails(details, initialExpanded = initialExpanded)
                }
                state.uri?.let { uri ->
                    ResultUri(uri)
                }
            }
        }
        Button(
            onCancel,
            Modifier
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
        Surface(color = mainContainerColor(state)) {
            ResultLoadingIndicator(
                state = state,
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
        Surface(color = mainContainerColor(state)) {
            ResultLoadingIndicator(
                state = state,
                onCancel = {},
            )
        }
    }
}
