package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.changelogShownForVersionCode
import page.ooooo.geoshare.data.local.preferences.introShowForVersionCode
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun UserPreferencesDetailDeveloperOptionsScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesDetailDeveloperOptionsScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onChangelogShownForVersionCodeValueChange = { viewModel.setUserPreferenceValue(introShowForVersionCode, it) },
        onIntroShownForVersionCodeValueChange = { viewModel.setUserPreferenceValue(changelogShownForVersionCode, it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesDetailDeveloperOptionsScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onChangelogShownForVersionCodeValueChange: (value: Int?) -> Unit,
    onIntroShownForVersionCodeValueChange: (value: Int?) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_preferences_developer_options_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_content_description)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            changelogShownForVersionCode.description()?.let { ParagraphHtml(it) }
            changelogShownForVersionCode.Component(
                userPreferencesValues.changelogShownForVersionCodeValue,
                onChangelogShownForVersionCodeValueChange,
            )

            introShowForVersionCode.description()?.let { ParagraphHtml(it) }
            introShowForVersionCode.Component(
                userPreferencesValues.introShownForVersionCodeValue,
                onIntroShownForVersionCodeValueChange,
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesDetailDeveloperOptionsScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onChangelogShownForVersionCodeValueChange = {},
            onIntroShownForVersionCodeValueChange = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesDetailDeveloperOptionsScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onChangelogShownForVersionCodeValueChange = {},
            onIntroShownForVersionCodeValueChange = {},
        )
    }
}
