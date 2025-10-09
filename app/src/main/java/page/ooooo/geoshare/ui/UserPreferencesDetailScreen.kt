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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.connectionPermission
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userPreference.title()) },
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
            userPreference.description()?.let { ParagraphHtml(it) }
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
