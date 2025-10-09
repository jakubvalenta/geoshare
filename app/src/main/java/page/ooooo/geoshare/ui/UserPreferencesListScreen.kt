package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.automation
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.ui.components.UserPreferencesScaffold
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun UserPreferencesListScreen(
    onBack: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDeveloperScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesListScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
        onNavigateToUserPreferencesConnectionPermissionScreen = onNavigateToUserPreferencesConnectionPermissionScreen,
        onNavigateToUserPreferencesDeveloperScreen = onNavigateToUserPreferencesDeveloperScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesListScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDeveloperScreen: () -> Unit,
) {
    UserPreferencesScaffold(
        title = stringResource(R.string.user_preferences_title),
        onBack = onBack,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    connectionPermission.title(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier
                .clickable(onClick = onNavigateToUserPreferencesConnectionPermissionScreen)
                .testTag("geoShareUserPreferencesConnectionPermissionItem"),
            supportingContent = {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    connectionPermission.ValueLabel(userPreferencesValues.connectionPermissionValue)
                }
            }
        )
        ListItem(
            headlineContent = {
                Text(
                    automation.title(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier
                .clickable(onClick = onNavigateToUserPreferencesAutomationScreen)
                .testTag("geoShareUserPreferencesAutomationItem"),
            supportingContent = {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    automation.ValueLabel(userPreferencesValues.automationValue)
                }
            }
        )
        if (BuildConfig.DEBUG) {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.user_preferences_developer_title),
                        Modifier.testTag("geoShareUserPreferencesDeveloperItem"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToUserPreferencesDeveloperScreen),
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesListScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDeveloperScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesListScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDeveloperScreen = {},
        )
    }
}
