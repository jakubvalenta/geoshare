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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.FakeGoogleMapsAddressServer
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerForm(
    apiKey: StateFlow<String>,
    apiKeyHeader: StateFlow<String>,
    authType: StateFlow<ServerAuthType>,
    challengeUrl: StateFlow<String>,
    loginUrl: StateFlow<String>,
    name: StateFlow<String>,
    registerUrl: StateFlow<String>,
    urlTemplate: StateFlow<String>,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ServerAuthType) -> Unit,
    onSetChallengeUrl: (String) -> Unit,
    onSetLoginUrl: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetRegisterUrl: (String) -> Unit,
    onSetUrlTemplate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    val name by name.collectAsStateWithLifecycle()
    val urlTemplate by urlTemplate.collectAsStateWithLifecycle()
    val authType by authType.collectAsStateWithLifecycle()
    val apiKey by apiKey.collectAsStateWithLifecycle()
    val apiKeyHeader by apiKeyHeader.collectAsStateWithLifecycle()
    val challengeUrl by challengeUrl.collectAsStateWithLifecycle()
    val loginUrl by loginUrl.collectAsStateWithLifecycle()
    val registerUrl by registerUrl.collectAsStateWithLifecycle()
    val item = remember(apiKey, apiKeyHeader, authType, challengeUrl, loginUrl, name, registerUrl, urlTemplate) {
        Server(
            name = name,
            urlTemplate = urlTemplate,
            authType = authType,
            apiKey = apiKey,
            apiKeyHeader = apiKeyHeader,
            challengeUrl = challengeUrl,
            loginUrl = loginUrl,
            registerUrl = registerUrl,
        )
    }

    Column(modifier) {
        TextField(
            value = name,
            onValueChange = onSetName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .testTag("geoShareServerFormName"),
            label = {
                Text(stringResource(R.string.server_name))
            },
            isError = name.isEmpty(),
            singleLine = true,
        )
        TextField(
            value = urlTemplate,
            onValueChange = onSetUrlTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium)
                .testTag("geoShareServerFormUrlTemplate"),
            label = {
                Text(stringResource(R.string.server_url_template))
            },
            supportingText = {
                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.example, ""))
                        append("https://geocode.googleapis.com/v4/geocode/address/")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{q}")
                        }
                    }
                )
            },
            isError = urlTemplate.isEmpty(),
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
            testTagPrefix = "geoShareServerFormAuthType"
        )
        when (authType) {
            ServerAuthType.API_KEY -> {
                TextField(
                    value = apiKeyHeader,
                    onValueChange = onSetApiKeyHeader,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium)
                        .testTag("geoShareServerFormApiKeyHeader"),
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
                    label = {
                        Text(stringResource(R.string.server_api_key))
                    },
                    supportingText = {
                        Text(stringResource(R.string.server_api_key_supporting_text))
                    },
                    isError = apiKey.isEmpty(),
                    singleLine = true,
                )
            }

            ServerAuthType.ATTESTATION -> {
                TextField(
                    value = challengeUrl,
                    onValueChange = onSetChallengeUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium)
                        .testTag("geoShareServerFormChallengeUrl"),
                    label = {
                        Text(stringResource(R.string.server_challenge_url))
                    },
                    isError = challengeUrl.isEmpty(),
                    singleLine = true,
                )
                TextField(
                    value = loginUrl,
                    onValueChange = onSetLoginUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium)
                        .testTag("geoShareServerFormLoginUrl"),
                    label = {
                        Text(stringResource(R.string.server_login_url))
                    },
                    isError = loginUrl.isEmpty(),
                    singleLine = true,
                )
                TextField(
                    value = registerUrl,
                    onValueChange = onSetRegisterUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium)
                        .testTag("geoShareServerFormRegisterUrl"),
                    label = {
                        Text(stringResource(R.string.server_register_url))
                    },
                    isError = registerUrl.isEmpty(),
                    singleLine = true,
                )
            }
        }
        HorizontalDivider(Modifier.padding(vertical = spacing.medium))
        LargeButton(
            stringResource(R.string.links_form_save),
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
                apiKey = MutableStateFlow(""),
                apiKeyHeader = MutableStateFlow(""),
                authType = MutableStateFlow(ServerAuthType.ATTESTATION),
                challengeUrl = MutableStateFlow(""),
                loginUrl = MutableStateFlow(""),
                name = MutableStateFlow(""),
                registerUrl = MutableStateFlow(""),
                urlTemplate = MutableStateFlow(""),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
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
                apiKey = MutableStateFlow(""),
                apiKeyHeader = MutableStateFlow(""),
                authType = MutableStateFlow(ServerAuthType.ATTESTATION),
                challengeUrl = MutableStateFlow(""),
                loginUrl = MutableStateFlow(""),
                name = MutableStateFlow(""),
                registerUrl = MutableStateFlow(""),
                urlTemplate = MutableStateFlow(""),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateApiKeyPreview() {
    AppTheme {
        Surface {
            val item = FakeGoogleMapsAddressServer
            ServerForm(
                apiKey = MutableStateFlow(item.apiKey),
                apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                authType = MutableStateFlow(item.authType),
                challengeUrl = MutableStateFlow(item.challengeUrl),
                loginUrl = MutableStateFlow(item.loginUrl),
                name = MutableStateFlow(item.name),
                registerUrl = MutableStateFlow(item.registerUrl),
                urlTemplate = MutableStateFlow(item.urlTemplate),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkUpdateApiKeyPreview() {
    AppTheme {
        Surface {
            val item = FakeGoogleMapsAddressServer
            ServerForm(
                apiKey = MutableStateFlow(item.apiKey),
                apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                authType = MutableStateFlow(item.authType),
                challengeUrl = MutableStateFlow(item.challengeUrl),
                loginUrl = MutableStateFlow(item.loginUrl),
                name = MutableStateFlow(item.name),
                registerUrl = MutableStateFlow(item.registerUrl),
                urlTemplate = MutableStateFlow(item.urlTemplate),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateAttestationPreview() {
    AppTheme {
        Surface {
            val item = FakeGeoShareGoogleMapsAddressServer
            ServerForm(
                apiKey = MutableStateFlow(item.apiKey),
                apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                authType = MutableStateFlow(item.authType),
                challengeUrl = MutableStateFlow(item.challengeUrl),
                loginUrl = MutableStateFlow(item.loginUrl),
                name = MutableStateFlow(item.name),
                registerUrl = MutableStateFlow(item.registerUrl),
                urlTemplate = MutableStateFlow(item.urlTemplate),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkUpdateExpandedPreview() {
    AppTheme {
        Surface {
            val item = FakeGeoShareGoogleMapsAddressServer
            ServerForm(
                apiKey = MutableStateFlow(item.apiKey),
                apiKeyHeader = MutableStateFlow(item.apiKeyHeader),
                authType = MutableStateFlow(item.authType),
                challengeUrl = MutableStateFlow(item.challengeUrl),
                loginUrl = MutableStateFlow(item.loginUrl),
                name = MutableStateFlow(item.name),
                registerUrl = MutableStateFlow(item.registerUrl),
                urlTemplate = MutableStateFlow(item.urlTemplate),
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetChallengeUrl = {},
                onSetLoginUrl = {},
                onSetName = {},
                onSetRegisterUrl = {},
                onSetUrlTemplate = {},
            )
        }
    }
}
