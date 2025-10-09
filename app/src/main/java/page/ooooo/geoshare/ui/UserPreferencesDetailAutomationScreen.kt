package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.automation
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun UserPreferencesDetailAutomationScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesDetailScreen(
        userPreference = automation,
        value = userPreferencesValues.automationValue,
        onBack = onBack,
        onValueChange = { viewModel.setUserPreferenceValue(automation, it) }
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesDetailScreen(
            userPreference = automation,
            value = defaultFakeUserPreferences.automationValue,
            onBack = {},
            onValueChange = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesDetailScreen(
            userPreference = automation,
            value = defaultFakeUserPreferences.automationValue,
            onBack = {},
            onValueChange = {},
        )
    }
}
