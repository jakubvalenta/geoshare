package page.ooooo.geoshare

import android.content.res.Configuration
import android.widget.Toast
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
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationImpl
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.State
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.ui.components.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ConversionScreen(
    runContext: ConversionRunContext,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val loadingIndicatorTitleResId by viewModel.loadingIndicatorTitleResId.collectAsStateWithLifecycle()
    val changelogShown by viewModel.changelogShown.collectAsState()

    ConversionScreen(
        currentState = currentState,
        changelogShown = changelogShown,
        loadingIndicatorTitleResId = loadingIndicatorTitleResId,
        queryGeoUriApps = { viewModel.intentTools.queryGeoUriApps(runContext.context.packageManager) },
        onBack = {
            viewModel.cancel()
            onBack()
        },
        onCancel = { viewModel.cancel() },
        onCopy = { text ->
            viewModel.cancel()
            coroutineScope.launch {
                viewModel.intentTools.copyToClipboard(runContext.context, runContext.clipboard, text)
            }
        },
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
        onNavigateToIntroScreen = {
            viewModel.cancel()
            onNavigateToIntroScreen()
        },
        onNavigateToUrlConvertersScreen = {
            viewModel.cancel()
            onNavigateToUrlConvertersScreen()
        },
        onNavigateToUserPreferencesScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesScreen()
        },
        onNavigateToUserPreferencesAutomationScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesAutomationScreen()
        },
        onOpenApp = { packageName, uriString ->
            viewModel.cancel()
            if (!viewModel.intentTools.openApp(runContext.context, packageName, uriString)) {
                Toast.makeText(
                    context,
                    R.string.conversion_automation_open_app_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onOpenChooser = { uriString ->
            viewModel.cancel()
            if (!viewModel.intentTools.openChooser(runContext.context, uriString)) {
                Toast.makeText(
                    context,
                    R.string.conversion_succeeded_apps_not_found,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        onRetry = { newUriString ->
            viewModel.updateInput(newUriString)
            viewModel.start(runContext)
        },
        onSave = {
            viewModel.cancel()
            viewModel.intentTools.launchSaveGpx(runContext.context, runContext.saveGpxLauncher)
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConversionScreen(
    currentState: State,
    changelogShown: Boolean,
    @StringRes loadingIndicatorTitleResId: Int?,
    queryGeoUriApps: () -> List<IntentTools.App>,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onCopy: (text: String) -> Unit,
    onDeny: (doNotAsk: Boolean) -> Unit,
    onFinish: () -> Unit,
    onGrant: (doNotAsk: Boolean) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToUrlConvertersScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onOpenApp: (packageName: String, uriString: String) -> Unit,
    onOpenChooser: (uriString: String) -> Unit,
    onRetry: (newUriString: String) -> Unit,
    onSave: () -> Boolean,
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
                onNavigateToIntroScreen = onNavigateToIntroScreen,
                onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
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
                        if (currentState is HasLoadingIndicator) {
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

            currentState is HasError -> {
                {
                    Headline(stringResource(R.string.conversion_error_title))
                    ResultError(
                        currentState.errorMessageResId,
                        currentState.inputUriString,
                        retryLoadingIndicatorVisible,
                        onNavigateToUrlConvertersScreen = onNavigateToUrlConvertersScreen,
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

            currentState is HasResult -> {
                {
                    Headline(stringResource(R.string.conversion_succeeded_title))
                    ResultSuccessCoordinates(
                        position = currentState.position,
                        onCopy = onCopy,
                        onOpenChooser = onOpenChooser,
                        onSave = onSave,
                    )
                }
            }

            else -> null
        },
        secondPane = when {
            (loadingIndicatorTitleResId == null && currentState is HasResult) -> {
                {
                    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                        ResultSuccessAutomation(
                            currentState,
                            onCancel = onCancel,
                            onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                        )
                        ResultSuccessApps(
                            apps = queryGeoUriApps(),
                            position = currentState.position,
                            onOpenApp = onOpenApp,
                            onOpenChooser = onOpenChooser,
                            windowSizeClass = windowSizeClass,
                        )
                    }
                }
            }

            else -> null
        },
        bottomPane = when {
            (loadingIndicatorTitleResId == null && currentState is HasError) -> {
                {
                    TextButton({ onCopy(currentState.inputUriString) }) {
                        Text(
                            stringResource(R.string.conversion_succeeded_skip), Modifier.padding(
                                start = spacing.windowPadding, top = spacing.tiny, bottom = spacing.small
                            )
                        )
                    }
                }
            }

            (loadingIndicatorTitleResId == null && currentState is HasResult) -> {
                {
                    TextButton({ onCopy(currentState.inputUriString) }) {
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
                                    truncateMiddle(currentState.uri.toString()),
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
                                    truncateMiddle(currentState.uri.toString()),
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
            currentState is HasError -> MaterialTheme.colorScheme.errorContainer
            currentState is HasResult -> MaterialTheme.colorScheme.secondaryContainer
            else -> Color.Unspecified
        },
        contentColor = when {
            loadingIndicatorTitleResId != null -> Color.Unspecified
            currentState is HasError -> MaterialTheme.colorScheme.onErrorContainer
            currentState is HasResult -> MaterialTheme.colorScheme.onSecondaryContainer
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
        val context = LocalContext.current
        ConversionScreen(
            currentState = AutomationFinished(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.Noop(),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        val context = LocalContext.current
        ConversionScreen(
            currentState = AutomationFinished(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.Noop(),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        val context = LocalContext.current
        ConversionScreen(
            currentState = AutomationFinished(
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.Noop(),
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.OpenApp(GOOGLE_MAPS_PACKAGE_NAME)
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.OpenApp(GOOGLE_MAPS_PACKAGE_NAME)
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                ConversionRunContext(context, clipboard, saveGpxLauncher),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                Position.example,
                AutomationImpl.OpenApp(GOOGLE_MAPS_PACKAGE_NAME)
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = {
                listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                )
            },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        ConversionScreen(
            currentState = ConversionFailed(
                R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = null,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
                GoogleMapsUrlConverter(),
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            changelogShown = true,
            loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title,
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
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
            queryGeoUriApps = { listOf() },
            onBack = {},
            onCancel = {},
            onCopy = {},
            onDeny = {},
            onFinish = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToUrlConvertersScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onOpenApp = { _, _ -> },
            onOpenChooser = {},
            onRetry = {},
            onSave = { true },
        )
    }
}
