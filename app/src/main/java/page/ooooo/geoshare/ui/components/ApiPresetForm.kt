package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGeoShareApiPreset
import page.ooooo.geoshare.data.di.FakeGoogleMapsApiPreset
import page.ooooo.geoshare.data.local.database.ApiAuthType
import page.ooooo.geoshare.data.local.database.ApiPreset
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiPresetForm(
    apiKey: String,
    apiKeyHeader: String,
    authType: ApiAuthType,
    baseUrl: String,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ApiAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val item = remember(baseUrl, authType, apiKey, apiKeyHeader) {
        ApiPreset(
            baseUrl = baseUrl,
            authType = authType,
            apiKey = apiKey,
            apiKeyHeader = apiKeyHeader,
        )
    }

    Column(modifier) {
        TextField(
            value = baseUrl,
            onValueChange = onSetBaseUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .testTag("geoShareApiPresetFormBaseUrl"),
            label = {
                Text(stringResource(R.string.api_presets_base_url))
            },
            isError = baseUrl.isEmpty(),
            singleLine = true,
        )
        DropdownField(
            value = authType,
            options = ApiAuthType.entries.associateWith { authType ->
                when (authType) {
                    ApiAuthType.API_KEY -> stringResource(R.string.api_presets_api_key)
                    ApiAuthType.ATTESTATION -> stringResource(R.string.api_presets_attestation)
                }
            },
            onValueChange = onSetAuthType,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium),
            label = { Text(stringResource(R.string.api_presets_auth_type)) },
        )
        TextField(
            value = apiKeyHeader,
            onValueChange = onSetApiKeyHeader,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium)
                .testTag("geoShareApiPresetFormApiKeyHeader"),
            enabled = authType == ApiAuthType.API_KEY,
            label = {
                Text(stringResource(R.string.api_presets_api_key_header))
            },
            isError = apiKeyHeader.isEmpty(),
            singleLine = true,
        )
        TextField(
            value = apiKey,
            onValueChange = onSetApiKey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium)
                .testTag("geoShareApiPresetFormApiKey"),
            enabled = authType == ApiAuthType.API_KEY,
            label = {
                Text(stringResource(R.string.api_presets_api_key))
            },
            supportingText = {
                Text(stringResource(R.string.api_presets_api_key_supporting_text))
            },
            isError = apiKey.isEmpty(),
            singleLine = true,
        )
        HorizontalDivider(Modifier.padding(vertical = spacing.medium))
        LargeButton(
            stringResource(R.string.save),
            Modifier.testTag("geoShareApiPresetFormSave"),
            enabled = item.isValid(),
        ) {
            onSaveForm()
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ApiPresetForm(
                apiKey = "",
                apiKeyHeader = "",
                authType = ApiAuthType.API_KEY,
                baseUrl = "",
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ApiPresetForm(
                apiKey = "",
                apiKeyHeader = "",
                authType = ApiAuthType.API_KEY,
                baseUrl = "",
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateApiKeyPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkUpdateApiKeyPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateAttestationPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGeoShareApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkUpdateExpandedPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGeoShareApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}
