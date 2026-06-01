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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
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
    apiKey: String,
    apiKeyHeader: String,
    authType: ServerAuthType,
    challengeUrl: String,
    loginUrl: String,
    name: String,
    registerUrl: String,
    urlTemplate: String,
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
                        append("https://api.geoshare-app.net/v1/google-maps/geocode/address/")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{q}")
                        }
                    }
                )
            },
            // TODO Add supporting text
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
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "",
                loginUrl = "",
                name = "",
                registerUrl = "",
                urlTemplate = "",
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
                apiKey = "",
                apiKeyHeader = "",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "",
                loginUrl = "",
                name = "",
                registerUrl = "",
                urlTemplate = "",
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
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                challengeUrl = item.challengeUrl,
                loginUrl = item.loginUrl,
                name = item.name,
                registerUrl = item.registerUrl,
                urlTemplate = item.urlTemplate,
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
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                challengeUrl = item.challengeUrl,
                loginUrl = item.loginUrl,
                name = item.name,
                registerUrl = item.registerUrl,
                urlTemplate = item.urlTemplate,
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
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                challengeUrl = item.challengeUrl,
                loginUrl = item.loginUrl,
                name = item.name,
                registerUrl = item.registerUrl,
                urlTemplate = item.urlTemplate,
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
                apiKey = item.apiKey,
                apiKeyHeader = item.apiKeyHeader,
                authType = item.authType,
                challengeUrl = item.challengeUrl,
                loginUrl = item.loginUrl,
                name = item.name,
                registerUrl = item.registerUrl,
                urlTemplate = item.urlTemplate,
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
