package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.ManagedActivityResultLauncherWrapper
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    onNavigateToIntroScreen: () -> Unit = {},
    onNavigateToUserPreferencesScreen: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val appName = stringResource(R.string.app_name)
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.grant(doNotAsk = false)
    }

    ShareScreen(loadingIndicatorEnabled = false, viewModel = viewModel)

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(appName) },
                actions = {
                    Box {
                        IconButton(
                            { menuExpanded = true },
                            Modifier.padding(end = Spacing.windowPadding - Spacing.builtInTopBarPaddingEnd),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.nav_menu_content_description),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.user_preferences_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToUserPreferencesScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.faq_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToFaqScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.intro_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToIntroScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToAboutScreen()
                                },
                            )
                        }
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
        ) {
            OutlinedTextField(
                value = viewModel.inputUriString,
                onValueChange = { viewModel.updateInput(it) },
                modifier = Modifier
                    .testTag("geoShareMainInputUriStringTextField")
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
                label = {
                    Text(
                        stringResource(R.string.main_input_uri_label),
                    )
                },
                trailingIcon = if (viewModel.inputUriString.isNotEmpty()) {
                    {
                        IconButton({ viewModel.updateInput("") }) {
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
                    Text(
                        stringResource(
                            viewModel.resultErrorMessageResId ?: R.string.main_input_uri_supporting_text
                        )
                    )
                },
                isError = viewModel.resultErrorMessageResId != null,
            )
            if (viewModel.loadingIndicatorTitleResId != null) {
                LoadingIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Spacing.small),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            } else if (viewModel.resultGeoUri.isNotEmpty()) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.small),
                    shape = OutlinedTextFieldDefaults.shape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Row(
                        Modifier.padding(
                            start = Spacing.small,
                            top = Spacing.tiny,
                            end = 4.dp,
                            bottom = Spacing.tiny,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(Modifier.weight(1f)) {
                            SelectionContainer {
                                Text(
                                    viewModel.resultGeoUri,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                        IconButton({ viewModel.copy(clipboard) }) {
                            Icon(
                                painterResource(R.drawable.content_copy_24px),
                                contentDescription = stringResource(R.string.main_result_geo_uri_copy_content_description)
                            )
                        }
                        IconButton({
                            viewModel.share(
                                context,
                                ManagedActivityResultLauncherWrapper(
                                    settingsLauncher
                                ),
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.main_result_geo_uri_share_content_description)
                            )
                        }
                    }
                }
            }
            Button(
                { coroutineScope.launch { viewModel.start() } },
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
            Card(
                Modifier.padding(top = Spacing.medium),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            ) {
                Row(
                    Modifier.Companion.padding(Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.lightbulb_24px), null)
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(R.string.main_info_box, appName),
                            linkStyles = TextLinkStyles(
                                SpanStyle(textDecoration = TextDecoration.Underline)
                            ),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
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
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "loadingIndicatorVisible" to true
                    )
                ),
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "loadingIndicatorVisible" to true
                    )
                ),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonePreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDonePreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultGeoUri" to "geo:50.123456,11.123456",
                    )
                ),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultErrorMessage" to R.string.conversion_failed_parse_url_error
                    )
                ),
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        MainScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(
                    mapOf(
                        "inputUriString" to "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        "resultErrorMessage" to R.string.conversion_failed_parse_url_error
                    )
                ),
            )
        )
    }
}
