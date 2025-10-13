package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.ui.components.UserPreferencesScaffold
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UserPreferencesDetailScreen(
    userPreference: UserPreference<T>,
    value: T,
    onBack: () -> Unit,
    onValueChange: (value: T) -> Unit,
) {
    UserPreferencesScaffold(
        title = userPreference.title(),
        onBack = onBack,
    ) {
        Column(Modifier.padding(horizontal = Spacing.windowPadding)) {
            userPreference.description()?.let { ParagraphHtml(it, Modifier.padding(bottom = Spacing.tiny)) }
            userPreference.Component(value, onValueChange)
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesDetailScreen(
            userPreference = connectionPermission,
            value = defaultFakeUserPreferences.connectionPermissionValue,
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
            userPreference = connectionPermission,
            value = defaultFakeUserPreferences.connectionPermissionValue,
            onBack = {},
            onValueChange = {},
        )
    }
}
