package page.ooooo.geoshare.ui

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun MainScreen(
    runContext: ConversionRunContext,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToConversionScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val recentInputsShown by viewModel.changelogShown.collectAsState()

    MainScreen(
        inputUriString = viewModel.inputUriString,
        changelogShown = recentInputsShown,
        onPaste = { block ->
            coroutineScope.launch {
                block(viewModel.intentTools.pasteFromClipboard(runContext.clipboard))
            }
        },
        onSubmit = {
            viewModel.start(runContext)
            onNavigateToConversionScreen()
        },
        onUpdateInput = { viewModel.updateInput(it) },
        onNavigateToAboutScreen = onNavigateToAboutScreen,
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
    onPaste: (block: (String) -> Unit) -> Unit,
    onSubmit: () -> Unit,
    onUpdateInput: (String) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
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
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            onSubmit()
                            true
                        } else {
                            false
                        }
                    }
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
                label = {
                    Text(stringResource(R.string.main_input_uri_label))
                },
                trailingIcon = {
                    if (inputUriString.isNotEmpty()) {
                        IconButton({
                            onUpdateInput("")
                            errorMessageResId = null
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                stringResource(R.string.main_input_uri_clear_content_description),
                            )
                        }
                    } else {
                        IconButton({
                            onPaste { text ->
                                onUpdateInput(text)
                                errorMessageResId = null
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.content_paste_24px),
                                stringResource(R.string.main_input_uri_paste_content_description),
                            )
                        }
                    }
                },
                supportingText = {
                    Text(stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text))
                },
                isError = errorMessageResId != null,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() },
                ),
            )
            Button(
                {
                    if (inputUriString.isEmpty()) {
                        // To show the user immediate feedback on this screen, do a simple validation before
                        // starting the conversion. Else the user would see an error message only on the conversion
                        // screen.
                        errorMessageResId = R.string.conversion_failed_missing_url
                    } else {
                        onSubmit()
                    }
                },
                Modifier
                    .testTag("geoShareMainSubmitButton")
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
            onPaste = {},
            onSubmit = {},
            onUpdateInput = {},
            onNavigateToAboutScreen = {},
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
            onPaste = {},
            onSubmit = {},
            onUpdateInput = {},
            onNavigateToAboutScreen = {},
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
            onPaste = {},
            onSubmit = {},
            onUpdateInput = {},
            onNavigateToAboutScreen = {},
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
            onPaste = {},
            onSubmit = {},
            onUpdateInput = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}
