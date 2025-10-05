package page.ooooo.geoshare

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.lastInputVersionCode
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.data.local.preferences.lastRunVersionCode
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun UserPreferencesScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()
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
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            UserPreferencesItem(
                viewModel,
                connectionPermission,
                userPreferencesValues.connectionPermissionValue,
                Modifier.padding(top = Spacing.tiny),
            )
            if (BuildConfig.DEBUG) {
                UserPreferencesItem(
                    viewModel,
                    lastRunVersionCode,
                    userPreferencesValues.introShownForVersionCodeValue,
                )
                UserPreferencesItem(
                    viewModel,
                    lastInputVersionCode,
                    userPreferencesValues.lastInputVersionCodeValue,
                )
            }
        }
    }
}

@Composable
fun <T> UserPreferencesItem(
    viewModel: ConversionViewModel,
    userPreference: UserPreference<T>,
    value: T,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            userPreference.title(),
            Modifier.padding(bottom = Spacing.small),
            style = MaterialTheme.typography.bodyLarge,
        )
        userPreference.description?.let { ParagraphHtml(it()) }
        userPreference.Component(value) {
            viewModel.setUserPreferenceValue(userPreference, it)
        }
    }
}

// Previews

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}
