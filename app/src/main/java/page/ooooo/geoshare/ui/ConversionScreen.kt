package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.lib.conversion.AutomationFinished
import page.ooooo.geoshare.lib.conversion.AutomationWaiting
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionRunContext
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.GrantedUnshortenPermission
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.conversion.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.extensions.truncateMiddle
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.components.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ConversionScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val saveGpxLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.saveGpx(context, it)
        }
    val runContext = ConversionRunContext(context, clipboard, saveGpxLauncher)

    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val intent by viewModel.intent.collectAsState()
    val loadingIndicatorTitleResId by viewModel.loadingIndicatorTitleResId.collectAsStateWithLifecycle()
    val changelogShown by viewModel.changelogShown.collectAsState()

    LaunchedEffect(intent) {
        intent?.let { viewModel.start(runContext, it) }
    }

    ConversionScreen(
        currentState = currentState,
        changelogShown = changelogShown,
        loadingIndicatorTitleResId = loadingIndicatorTitleResId,
        onBack = {
            viewModel.cancel()
            onBack()
        },
        onCancel = { viewModel.cancel() },
        onDeny = { doNotAsk -> viewModel.deny(doNotAsk) },
        onFinish = onFinish,
        onGrant = { doNotAsk -> viewModel.grant(doNotAsk) },
        onNavigateToAboutScreen = {
            viewModel.cancel()
            onNavigateToAboutScreen()
        },
        onNavigateToFaqScreen = {
            viewModel.cancel()
            onNavigateToFaqScreen()
        },
        onNavigateToInputsScreen = {
            viewModel.cancel()
            onNavigateToInputsScreen()
        },
        onNavigateToIntroScreen = {
            viewModel.cancel()
            onNavigateToIntroScreen()
        },
        onNavigateToUserPreferencesScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesScreen()
        },
        onNavigateToUserPreferencesAutomationScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesAutomationScreen()
        },
        onRetry = { newUriString ->
            viewModel.updateInput(newUriString)
            viewModel.start(runContext)
        },
        onRun = { action ->
            viewModel.cancel()
            viewModel.runAction(runContext, action)
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionScreen(
    currentState: State,
    changelogShown: Boolean,
    @StringRes loadingIndicatorTitleResId: Int?,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onDeny: (doNotAsk: Boolean) -> Unit,
    onFinish: () -> Unit,
    onGrant: (doNotAsk: Boolean) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onRetry: (newUriString: String) -> Unit,
    onRun: (action: Action) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val (retryLoadingIndicatorVisible, setRetryLoadingIndicator) = remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    TwoPaneScaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        navigationIcon = {
            IconButton(
                onBack,
                Modifier.testTag("geoShareConversionBackButton"),
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.nav_back_content_description)
                )
            }
        },
        actions = {
            MainMenu(
                changelogShown = changelogShown,
                onNavigateToAboutScreen = onNavigateToAboutScreen,
                onNavigateToFaqScreen = onNavigateToFaqScreen,
                onNavigateToInputsScreen = onNavigateToInputsScreen,
                onNavigateToIntroScreen = onNavigateToIntroScreen,
                onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
            )
        },
        firstPane = when {
            (loadingIndicatorTitleResId != null) -> {
                {
                    Headline(stringResource(loadingIndicatorTitleResId))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.small)
                            .padding(horizontal = spacing.windowPadding),
                    ) {
                        LoadingIndicator(
                            Modifier
                                .size(96.dp)
                                .align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Button(
                            {
                                onCancel()
                                onFinish()
                            },
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = spacing.small, bottom = spacing.medium),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(stringResource(R.string.conversion_loading_indicator_cancel))
                        }
                        if (currentState is ConversionState.HasLoadingIndicator) {
                            currentState.loadingIndicatorDescription()?.let { text ->
                                Text(
                                    text,
                                    Modifier.padding(bottom = spacing.medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            currentState is ConversionState.HasError -> {
                {
                    Headline(stringResource(R.string.conversion_error_title))
                    ResultError(
                        currentState.errorMessageResId,
                        currentState.inputUriString,
                        retryLoadingIndicatorVisible,
                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                        onRetry = {
                            coroutineScope.launch {
                                // Show a loading indicator for a while to indicate that conversion is being retried.
                                setRetryLoadingIndicator(true)
                                delay(1000)
                                setRetryLoadingIndicator(false)
                                onRetry(currentState.inputUriString)
                            }
                        },
                    )
                }
            }

            currentState is ConversionState.HasResult -> {
                {
                    Headline(stringResource(R.string.conversion_succeeded_title))
                    ResultSuccessCoordinates(
                        position = currentState.position,
                        onRun = onRun,
                    )
                }
            }

            else -> null
        },
        secondPane = when {
            (loadingIndicatorTitleResId == null && currentState is ConversionState.HasResult) -> {
                {
                    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                        ResultSuccessAutomation(
                            currentState,
                            onCancel = onCancel,
                            onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                        )
                        ResultSuccessApps(
                            position = currentState.position,
                            onRun = onRun,
                            windowSizeClass = windowSizeClass,
                        )
                    }
                }
            }

            else -> null
        },
        bottomPane = when {
            (loadingIndicatorTitleResId == null && currentState is ConversionState.HasError) -> {
                {
                    TextButton({ onRun(Action.Copy(currentState.inputUriString)) }) {
                        Text(
                            stringResource(R.string.conversion_succeeded_skip), Modifier.padding(
                                start = spacing.windowPadding, top = spacing.tiny, bottom = spacing.small
                            )
                        )
                    }
                }
            }

            (loadingIndicatorTitleResId == null && currentState is ConversionState.HasResult) -> {
                {
                    TextButton({ onRun(Action.Copy(currentState.inputUriString)) }) {
                        Text(
                            stringResource(R.string.conversion_succeeded_skip), Modifier.padding(
                                start = spacing.windowPadding, top = spacing.tiny, bottom = spacing.small
                            )
                        )
                    }
                }
            }

            else -> null
        },
        dialog = when {
            (loadingIndicatorTitleResId == null && currentState is RequestedUnshortenPermission) -> {
                {
                    PermissionDialog(
                        title = stringResource(currentState.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_common_deny),
                        onConfirmation = onGrant,
                        onDismissRequest = onDeny,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareUnshortenPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_common_text,
                                    currentState.uri.toString().truncateMiddle(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }
            }

            (loadingIndicatorTitleResId == null && currentState is RequestedParseHtmlPermission) -> {
                {
                    PermissionDialog(
                        title = stringResource(currentState.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_common_deny),
                        onConfirmation = onGrant,
                        onDismissRequest = onDeny,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareParseHtmlPermissionDialog"),
                    ) {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(
                                    R.string.conversion_permission_common_text,
                                    currentState.uri.toString().truncateMiddle(),
                                    appName,
                                )
                            ),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }
            }

            else -> null
        },
        containerColor = when {
            loadingIndicatorTitleResId != null -> MaterialTheme.colorScheme.surfaceContainer
            currentState is ConversionState.HasError -> MaterialTheme.colorScheme.errorContainer
            currentState is ConversionState.HasResult -> MaterialTheme.colorScheme.secondaryContainer
            else -> Color.Unspecified
        },
        contentColor = when {
            loadingIndicatorTitleResId != null -> Color.Unspecified
            currentState is ConversionState.HasError -> MaterialTheme.colorScheme.onErrorContainer
            currentState is ConversionState.HasResult -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> Color.Unspecified
        },
        windowSizeClass = windowSizeClass,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConversionScreen(
            currentState = AutomationFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = Automation.Noop,
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        ConversionScreen(
            currentState = AutomationFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = Automation.Noop,
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        ConversionScreen(
            currentState = AutomationFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = Automation.Noop,
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = AutomationWaiting(
                stateContext = ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                runContext = ConversionRunContext(context, clipboard, saveGpxLauncher),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = AutomationWaiting(
                stateContext = ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                runContext = ConversionRunContext(context, clipboard, saveGpxLauncher),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = AutomationWaiting(
                stateContext = ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                runContext = ConversionRunContext(context, clipboard, saveGpxLauncher),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                automation = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val saveGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        ConversionScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    IntentTools(),
                    NetworkTools(),
                    FakeUserPreferencesRepository(),
                ),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkInitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletInitialPreview() {
    AppTheme {
        ConversionScreen(
            currentState = Initial(),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            onBack = {},
            onCancel = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onRetry = {},
            onRun = {},
        )
    }
}
