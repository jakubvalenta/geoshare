package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
    onNavigateToUserPreferencesDetailAutomaticActionScreen: () -> Unit,
    onNavigateToUserPreferencesDetailConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDetailDeveloperOptionsScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesListScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onNavigateToUserPreferencesDetailAutomaticActionScreen = onNavigateToUserPreferencesDetailAutomaticActionScreen,
        onNavigateToUserPreferencesDetailConnectionPermissionScreen = onNavigateToUserPreferencesDetailConnectionPermissionScreen,
        onNavigateToUserPreferencesDetailDeveloperOptionsScreen = onNavigateToUserPreferencesDetailDeveloperOptionsScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesListScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToUserPreferencesDetailAutomaticActionScreen: () -> Unit,
    onNavigateToUserPreferencesDetailConnectionPermissionScreen: () -> Unit,
    onNavigateToUserPreferencesDetailDeveloperOptionsScreen: () -> Unit,
) {
    UserPreferencesScaffold(
        title = stringResource(R.string.user_preferences_title),
        modifier = Modifier.semantics { testTagsAsResourceId = true },
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
                .clickable(onClick = onNavigateToUserPreferencesDetailConnectionPermissionScreen)
                .testTag("geoShareUserPreferencesConnectionPermissionItem"),
            supportingContent = {
                Text(
                    connectionPermission.valueLabel(userPreferencesValues.connectionPermissionValue),
                    style = MaterialTheme.typography.bodyMedium,
                )
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
                .clickable(onClick = onNavigateToUserPreferencesDetailAutomaticActionScreen)
                .testTag("geoShareUserPreferencesAutomaticActionItem"),
            supportingContent = {
                Text(
                    automaticAction.valueLabel(userPreferencesValues.automaticActionValue),
                    style = MaterialTheme.typography.bodyMedium,
                )
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
                modifier = Modifier.clickable(onClick = onNavigateToUserPreferencesDetailDeveloperOptionsScreen),
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
            onNavigateToUserPreferencesDetailAutomaticActionScreen = {},
            onNavigateToUserPreferencesDetailConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDetailDeveloperOptionsScreen = {},
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
            onNavigateToUserPreferencesDetailAutomaticActionScreen = {},
            onNavigateToUserPreferencesDetailConnectionPermissionScreen = {},
            onNavigateToUserPreferencesDetailDeveloperOptionsScreen = {},
        )
    }
}
