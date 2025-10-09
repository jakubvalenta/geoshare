package page.ooooo.geoshare

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

private interface ValueChangeCallback {
    fun <T> invoke(userPreference: UserPreference<T>, value: T)
}

@Composable
fun UserPreferencesScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onValueChange = object : ValueChangeCallback {
            override fun <T> invoke(userPreference: UserPreference<T>, value: T) {
                viewModel.setUserPreferenceValue(userPreference, value)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
private fun UserPreferencesScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onValueChange: ValueChangeCallback,
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
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            // TODO Put the setting of user preferences in full screen dialogs
            UserPreferencesItem(
                connectionPermission,
                userPreferencesValues.connectionPermissionValue,
                { value -> onValueChange.invoke(connectionPermission, value) },
                Modifier.padding(top = Spacing.tiny),
            )
            UserPreferencesItem(
                automaticAction,
                userPreferencesValues.automaticActionValue,
                { value -> onValueChange.invoke(automaticAction, value) }
            )
            if (BuildConfig.DEBUG) {
                UserPreferencesItem(
                    lastRunVersionCode,
                    userPreferencesValues.introShownForVersionCodeValue,
                    { value -> onValueChange.invoke(lastRunVersionCode, value) }
                )
                UserPreferencesItem(
                    lastInputVersionCode,
                    userPreferencesValues.lastInputVersionCodeValue,
                    { value -> onValueChange.invoke(lastInputVersionCode, value) }
                )
            }
        }
    }
}

@Composable
private fun <T> UserPreferencesItem(
    userPreference: UserPreference<T>,
    value: T,
    onValueChange: (value: T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            userPreference.title(),
            Modifier.padding(bottom = Spacing.small),
            style = MaterialTheme.typography.bodyLarge,
        )
        userPreference.description?.let { ParagraphHtml(it()) }
        userPreference.Component(value, onValueChange)
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onValueChange = object : ValueChangeCallback {
                override fun <T> invoke(userPreference: UserPreference<T>, value: T) {
                    throw NotImplementedError()
                }
            },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onValueChange = object : ValueChangeCallback {
                override fun <T> invoke(userPreference: UserPreference<T>, value: T) {
                    throw NotImplementedError()
                }
            },
        )
    }
}
