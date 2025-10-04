package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.FaqItemId
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun MainScreen(
    changelogShown: Boolean,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToConversionScreen: () -> Unit,
    onNavigateToFaqScreen: (FaqItemId?) -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    MainScreen(
        inputUriString = viewModel.inputUriString,
        changelogShown = changelogShown,
        onUpdateInput = { viewModel.updateInput(it) },
        onStart = { viewModel.start() },
        onNavigateToAboutScreen = onNavigateToAboutScreen,
        onNavigateToConversionScreen = onNavigateToConversionScreen,
        onNavigateToFaqScreen = onNavigateToFaqScreen,
        onNavigateToIntroScreen = onNavigateToIntroScreen,
        onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    inputUriString: String,
    changelogShown: Boolean,
    onUpdateInput: (String) -> Unit,
    onStart: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToConversionScreen: () -> Unit,
    onNavigateToFaqScreen: (FaqItemId?) -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    var errorMessageResId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(appName) },
                actions = {
                    MainMenu(
                        changelogShown = changelogShown,
                        onNavigateToAboutScreen = onNavigateToAboutScreen,
                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                        onNavigateToIntroScreen = onNavigateToIntroScreen,
                        onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
                        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                    )
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
        ) {
            OutlinedTextField(
                value = inputUriString,
                onValueChange = {
                    onUpdateInput(it)
                    errorMessageResId = null
                },
                modifier = Modifier
                    .testTag("geoShareMainInputUriStringTextField")
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
                label = {
                    Text(stringResource(R.string.main_input_uri_label))
                },
                trailingIcon = if (inputUriString.isNotEmpty()) {
                    {
                        IconButton({
                            onUpdateInput("")
                            errorMessageResId = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.main_input_uri_clear_content_description),
                            )
                        }
                    }
                } else {
                    null
                },
                supportingText = {
                    Text(stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text))
                },
                isError = errorMessageResId != null,
            )
            Button(
                {
                    if (inputUriString.isEmpty()) {
                        // To show the user immediate feedback on this screen, do a simple validation before
                        // starting the conversion. Else the user would see an error message only on the conversion
                        // screen.
                        errorMessageResId = R.string.conversion_failed_missing_url
                    } else {
                        onStart()
                        onNavigateToConversionScreen()
                    }
                },
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
            ) {
                Text(stringResource(R.string.main_create_geo_uri))
            }
            TextButton(
                { onNavigateToIntroScreen() },
                Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.main_navigate_to_intro, appName))
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MainScreen(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            changelogShown = true,
            onUpdateInput = {},
            onStart = {},
            onNavigateToAboutScreen = {},
            onNavigateToConversionScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainScreen(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            changelogShown = true,
            onUpdateInput = {},
            onStart = {},
            onNavigateToAboutScreen = {},
            onNavigateToConversionScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultChangelogBadgedPreview() {
    AppTheme {
        MainScreen(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            changelogShown = false,
            onUpdateInput = {},
            onStart = {},
            onNavigateToAboutScreen = {},
            onNavigateToConversionScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkChangelogBadgedPreview() {
    AppTheme {
        MainScreen(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            changelogShown = false,
            onUpdateInput = {},
            onStart = {},
            onNavigateToAboutScreen = {},
            onNavigateToConversionScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}
