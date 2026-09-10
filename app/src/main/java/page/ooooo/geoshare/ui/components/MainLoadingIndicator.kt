package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainLoadingIndicator(
    state: ConversionState.HasDescription,
    title: String,
    initialExpanded: Boolean = false,
    onCancel: () -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.small, bottom = spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
        )
        LoadingIndicator(
            Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.tertiary,
        )
        Button(
            onCancel,
            Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("geoShareMainLoadingIndicatorCancel"),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(stringResource(R.string.conversion_loading_indicator_cancel))
        }
        state.getDetails(resources)?.let { details ->
            ResultDetails(
                details,
                Modifier.testTag("geoShareMainLoadingIndicatorDescription"),
                initialExpanded = initialExpanded,
            )
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
            MainLoadingIndicator(
                state = state,
                title = stringResource(
                    R.string.conversion_connecting,
                    state.matchedInput.input.group.getName(LocalResources.current),
                ),
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
            MainLoadingIndicator(
                state = state,
                title = stringResource(
                    R.string.conversion_connecting,
                    state.matchedInput.input.group.getName(LocalResources.current),
                ),
                onCancel = {},
            )
        }
    }
}
