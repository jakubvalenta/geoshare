package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.data.di.FakeGoogleMapsApiPreset
import page.ooooo.geoshare.data.local.database.ApiAuthType
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiPresetForm(
    apiKey: String,
    apiKeyHeader: String,
    authType: ApiAuthType,
    baseUrl: String,
    enabled: Boolean,
    onSaveForm: () -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyHeader: (String) -> Unit,
    onSetAuthType: (ApiAuthType) -> Unit,
    onSetBaseUrl: (String) -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                enabled = false,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
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
                enabled = false,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=4000px,dpi=440")
@Composable
private fun UpdatePreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                enabled = apiPreset.enabled,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=4000px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkUpdatePreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                enabled = apiPreset.enabled,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=4000px,dpi=440")
@Composable
private fun UpdateExpandedPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                enabled = apiPreset.enabled,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=4000px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkUpdateExpandedPreview() {
    AppTheme {
        Surface {
            val apiPreset = FakeGoogleMapsApiPreset
            ApiPresetForm(
                apiKey = apiPreset.apiKey,
                apiKeyHeader = apiPreset.apiKeyHeader,
                authType = apiPreset.authType,
                baseUrl = apiPreset.baseUrl,
                enabled = apiPreset.enabled,
                onSaveForm = {},
                onSetApiKey = {},
                onSetApiKeyHeader = {},
                onSetAuthType = {},
                onSetBaseUrl = {},
                onSetEnabled = {},
            )
        }
    }
}
