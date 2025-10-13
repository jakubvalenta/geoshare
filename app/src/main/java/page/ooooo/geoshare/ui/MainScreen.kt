package page.ooooo.geoshare.ui

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.toScale
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import kotlin.random.Random

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
            TopAppBar(
                title = {},
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                Modifier.padding(
                    start = 13.dp,
                    top = Spacing.large,
                    end = Spacing.windowPadding,
                    bottom = Spacing.large,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.about_app_icon_content_description),
                    modifier = Modifier.size(72.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(appName, style = MaterialTheme.typography.headlineLarge)
            }
            Column(
                Modifier.padding(horizontal = Spacing.windowPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
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
                            .fillMaxWidth(),
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
                            Text(
                                stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text),
                                Modifier.padding(top = Spacing.tiny),
                            )
                        },
                        isError = errorMessageResId != null,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onSubmit() },
                        ),
                    )
                    Column(Modifier.padding(horizontal = 9.dp)) {
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
                                .align(Alignment.CenterHorizontally)
                                .width(400.dp)
                                .height(50.dp),
                        ) {
                            Text(
                                stringResource(R.string.main_create_geo_uri),
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
                Column {
                    TextButton(onNavigateToUrlConvertersScreen) {
                        Icon(
                            Icons.Outlined.Info,
                            null,
                            Modifier.padding(end = Spacing.tiny),
                        )
                        Text(stringResource(R.string.url_converters_title))
                    }
                    TextButton(onNavigateToIntroScreen) {
                        Icon(
                            painterResource(R.drawable.rocket_launch_24px),
                            null,
                            Modifier.padding(end = Spacing.tiny),
                        )
                        Text(stringResource(R.string.main_navigate_to_intro))
                    }
                    TextButton({
                        onUpdateInput(
                            Position(
                                Random.nextDouble(-50.0, 80.0).toScale(6).toString(),
                                Random.nextDouble(-180.0, 180.0).toScale(6).toString(),
                                z = "8",
                            ).toGoogleMapsUriString()
                        )
                        errorMessageResId = null
                    }) {
                        Icon(
                            painterResource(R.drawable.ifl_24px),
                            null,
                            Modifier.padding(end = Spacing.tiny),
                        )
                        Text(stringResource(R.string.main_random))
                    }
                }
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
            inputUriString = "",
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
            inputUriString = "",
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
private fun FilledAndChangelogBadgedPreview() {
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
private fun DarkFilledAndChangelogBadgedPreview() {
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
