package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.changelogShownForVersionCode
import page.ooooo.geoshare.data.local.preferences.introShowForVersionCode
import page.ooooo.geoshare.ui.components.UserPreferencesScaffold
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun UserPreferencesDetailDeveloperScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesDetailDeveloperScreen(
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onChangelogShownForVersionCodeValueChange = {
            viewModel.setUserPreferenceValue(changelogShownForVersionCode, it)
        },
        onIntroShownForVersionCodeValueChange = {
            viewModel.setUserPreferenceValue(introShowForVersionCode, it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesDetailDeveloperScreen(
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onChangelogShownForVersionCodeValueChange: (value: Int?) -> Unit,
    onIntroShownForVersionCodeValueChange: (value: Int?) -> Unit,
) {
    UserPreferencesScaffold(
        title = stringResource(R.string.user_preferences_developer_title),
        onBack = onBack,
    ) {
        Column(Modifier.padding(horizontal = Spacing.windowPadding)) {
            ParagraphHtml(changelogShownForVersionCode.title(), Modifier.padding(bottom = Spacing.tiny))
            changelogShownForVersionCode.Component(
                userPreferencesValues.changelogShownForVersionCodeValue,
                onChangelogShownForVersionCodeValueChange,
            )

            ParagraphHtml(
                introShowForVersionCode.title(), Modifier.padding(top = Spacing.medium, bottom = Spacing.tiny)
            )
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
        UserPreferencesDetailDeveloperScreen(
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
        UserPreferencesDetailDeveloperScreen(
            userPreferencesValues = defaultFakeUserPreferences,
            onBack = {},
            onChangelogShownForVersionCodeValueChange = {},
            onIntroShownForVersionCodeValueChange = {},
        )
    }
}
