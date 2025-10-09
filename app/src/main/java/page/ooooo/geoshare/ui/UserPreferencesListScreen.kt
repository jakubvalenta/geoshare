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
import page.ooooo.geoshare.data.local.preferences.automaticAction
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.ui.components.UserPreferencesScaffold
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun UserPreferencesListScreen(
    onBack: () -> Unit,
    onNavigateToUserPreferencesAutomaticActionScreen: () -> Unit,
    onNavigateToUserPreferencesConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDeveloperOptionsScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesListScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onNavigateToUserPreferencesAutomaticActionScreen = onNavigateToUserPreferencesAutomaticActionScreen,
        onNavigateToUserPreferencesConnectionPermissionScreen = onNavigateToUserPreferencesConnectionPermissionScreen,
        onNavigateToUserPreferencesDeveloperOptionsScreen = onNavigateToUserPreferencesDeveloperOptionsScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesListScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToUserPreferencesAutomaticActionScreen: () -> Unit,
    onNavigateToUserPreferencesConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDeveloperOptionsScreen: () -> Unit,
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
                    automaticAction.title(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier
                .clickable(onClick = onNavigateToUserPreferencesAutomaticActionScreen)
                .testTag("geoShareUserPreferencesAutomaticActionItem"),
            supportingContent = {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    automaticAction.ValueLabel(userPreferencesValues.automaticActionValue)
                }
            }
        )
        if (BuildConfig.DEBUG) {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.user_preferences_developer_options_title),
                        Modifier.testTag("geoShareUserPreferencesDeveloperOptionsItem"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToUserPreferencesDeveloperOptionsScreen),
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
            onNavigateToUserPreferencesAutomaticActionScreen = {},
            onNavigateToUserPreferencesConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDeveloperOptionsScreen = {},
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
            onNavigateToUserPreferencesAutomaticActionScreen = {},
            onNavigateToUserPreferencesConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDeveloperOptionsScreen = {},
        )
    }
}
