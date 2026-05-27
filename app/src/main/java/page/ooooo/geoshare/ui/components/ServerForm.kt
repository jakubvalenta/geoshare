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
import page.ooooo.geoshare.data.di.FakeGeoShareServer
import page.ooooo.geoshare.data.di.FakeGoogleMapsServer
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerForm(
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    baseUrl: String,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val item = remember(baseUrl, authType, apiKey, apiKeyHeader) {
        Server(
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
                .testTag("geoShareServerFormBaseUrl"),
            label = {
                Text(stringResource(R.string.server_base_url))
            },
            isError = baseUrl.isEmpty(),
            singleLine = true,
        )
        DropdownField(
            value = authType,
            options = ServerAuthType.entries.associateWith { authType ->
                when (authType) {
                    ServerAuthType.API_KEY -> stringResource(R.string.server_api_key)
                    ServerAuthType.ATTESTATION -> stringResource(R.string.server_attestation)
                }
            },
            onValueChange = onSetAuthType,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium),
            label = { Text(stringResource(R.string.server_auth_type)) },
        )
        TextField(
            value = apiKeyHeader,
            onValueChange = onSetApiKeyHeader,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium)
                .testTag("geoShareServerFormApiKeyHeader"),
            enabled = authType == ServerAuthType.API_KEY,
            label = {
                Text(stringResource(R.string.server_api_key_header))
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
                .testTag("geoShareServerFormApiKey"),
            enabled = authType == ServerAuthType.API_KEY,
            label = {
                Text(stringResource(R.string.server_api_key))
            },
            supportingText = {
                Text(stringResource(R.string.server_api_key_supporting_text))
            },
            isError = apiKey.isEmpty(),
            singleLine = true,
        )
        HorizontalDivider(Modifier.padding(vertical = spacing.medium))
        LargeButton(
            stringResource(R.string.save),
            Modifier.testTag("geoShareServerFormSave"),
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
            ServerForm(
                apiKey = "",
                apiKeyHeader = "",
                authType = ServerAuthType.API_KEY,
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
            ServerForm(
                apiKey = "",
                apiKeyHeader = "",
                authType = ServerAuthType.API_KEY,
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
            val item = FakeGoogleMapsServer
            ServerForm(
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                baseUrl = item.baseUrl,
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
            val item = FakeGoogleMapsServer
            ServerForm(
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                baseUrl = item.baseUrl,
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
            val item = FakeGeoShareServer
            ServerForm(
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                baseUrl = item.baseUrl,
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
            val item = FakeGeoShareServer
            ServerForm(
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                baseUrl = item.baseUrl,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
            )
        }
    }
}
