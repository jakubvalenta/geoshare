package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import page.ooooo.geoshare.data.local.preferences.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

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
    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_preferences_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        connectionPermission.title(),
                        Modifier.padding(bottom = Spacing.small),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier
                    .clickable(onClick = onNavigateToUserPreferencesDetailConnectionPermissionScreen)
                    .padding(top = Spacing.tiny),
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
                        Modifier.padding(bottom = Spacing.small),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToUserPreferencesDetailAutomaticActionScreen),
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
                            Modifier.padding(bottom = Spacing.small),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    modifier = Modifier.clickable(onClick = onNavigateToUserPreferencesDetailDeveloperOptionsScreen),
                )
            }
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
