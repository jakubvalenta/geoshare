package page.ooooo.geoshare.ui

import android.Manifest
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.GrantedUnshortenPermission
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleRequested
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.conversion.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.lib.extensions.truncateMiddle
import page.ooooo.geoshare.lib.features.Features
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.FeatureBadgeSmall
import page.ooooo.geoshare.ui.components.Headline
import page.ooooo.geoshare.ui.components.MainForm
import page.ooooo.geoshare.ui.components.MainInfo
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.ResultSuccessApps
import page.ooooo.geoshare.ui.components.ResultSuccessCoordinates
import page.ooooo.geoshare.ui.components.ResultSuccessMessage
import page.ooooo.geoshare.ui.components.ResultSuccessSheetContent
import page.ooooo.geoshare.ui.components.TwoPaneScaffold
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToSubscriptionScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

    val automationFeatureValid by viewModel.automationFeatureValid.collectAsState()
    val changelogShown by viewModel.changelogShown.collectAsState()
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val loadingIndicator by viewModel.loadingIndicator.collectAsStateWithLifecycle()

    var locationJob by remember { mutableStateOf<Job?>(null) }
    val locationPermissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.receiveLocationPermission()
        }
    val saveGpxLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.writer().use { writer ->
                        viewModel.writeGpx(writer)
                    }
                }
            }
        }

    LaunchedEffect(currentState) {
        currentState.let { currentState ->
            when (currentState) {
                is BasicActionReady -> {
                    val success = currentState.action.runAction(
                        position = currentState.position,
                        i = currentState.i,
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                        saveGpxLauncher = saveGpxLauncher,
                    )
                    viewModel.finishBasicAction(success)
                }

                is LocationActionReady -> {
                    val success = currentState.action.runAction(
                        position = currentState.position,
                        i = currentState.i,
                        location = currentState.location,
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                        saveGpxLauncher = saveGpxLauncher,
                    )
                    viewModel.finishLocationAction(success)
                }

                is LocationRationaleRequested -> {
                    if (AndroidTools.hasLocationPermission(context)) {
                        viewModel.skipLocationRationale(currentState.action, currentState.i)
                    } else {
                        viewModel.showLocationRationale(currentState.action, currentState.i)
                    }
                }

                is LocationRationaleConfirmed -> {
                    locationPermissionRequest.launch(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    )
                }

                is LocationPermissionReceived -> {
                    locationJob?.cancel()
                    locationJob = coroutineScope.launch(Dispatchers.IO) {
                        val location = try {
                            AndroidTools.getLocation(context)
                        } catch (_: CancellationException) {
                            viewModel.cancelLocationFinding()
                            return@launch
                        }
                        viewModel.receiveLocation(currentState.action, currentState.i, location)
                    }
                }
            }
        }
    }

    MainScreen(
        currentState = currentState,
        automationFeatureValid = automationFeatureValid,
        changelogShown = changelogShown,
        inputUriString = viewModel.inputUriString,
        loadingIndicator = loadingIndicator,
        onCancel = {
            locationJob?.cancel()
            viewModel.cancel()
        },
        onDeny = { doNotAsk -> viewModel.deny(doNotAsk) },
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
        onNavigateToSubscriptionScreen = {
            viewModel.cancel()
            onNavigateToSubscriptionScreen()
        },
        onNavigateToUserPreferencesScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesScreen()
        },
        onNavigateToUserPreferencesAutomationScreen = {
            viewModel.cancel()
            onNavigateToUserPreferencesAutomationScreen()
        },
        onReset = { viewModel.reset() },
        onRun = { action, i ->
            viewModel.cancel()
            viewModel.runAction(action, i)
        },
        onStart = { viewModel.start() },
        onUpdateInput = { newInputUriString -> viewModel.updateInput(newInputUriString) }
    )
}

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun MainScreen(
    currentState: State,
    automationFeatureValid: Boolean?,
    changelogShown: Boolean,
    inputUriString: String,
    loadingIndicator: LoadingIndicator?,
    onCancel: () -> Unit,
    onDeny: (doNotAsk: Boolean) -> Unit,
    onGrant: (doNotAsk: Boolean) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToSubscriptionScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onReset: () -> Unit,
    onRun: (action: Action, i: Int?) -> Unit,
    onStart: () -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val (errorMessageResId, setErrorMessageResId) = retain { mutableStateOf<Int?>(null) }
    val (retryLoadingIndicatorVisible, setRetryLoadingIndicator) = retain { mutableStateOf(false) }
    val (selectedPositionAndIndex, setSelectedPositionAndIndex) = retain { mutableStateOf<Pair<Position, Int?>?>(null) }
    val sheetState = rememberModalBottomSheetState()

    BackHandler(currentState !is Initial) {
        onReset()
    }

    TwoPaneScaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        navigationIcon = {
            if (currentState !is Initial) {
                IconButton(
                    onReset,
                    Modifier.testTag("geoShareMainBackButton"),
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        },
        actions = {
            if (automationFeatureValid == true) {
                FeatureBadgeSmall(
                    onClick = onNavigateToSubscriptionScreen,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
            (loadingIndicator is LoadingIndicator.Large) -> {
                {
                    Headline(stringResource(loadingIndicator.titleResId))
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
                            onCancel,
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
                        loadingIndicator.description()?.let { text ->
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
                                onUpdateInput(currentState.inputUriString)
                                onStart()
                            }
                        },
                    )
                }
            }

            currentState is ConversionState.HasResult -> {
                {
                    ResultSuccessCoordinates(
                        position = currentState.position,
                        onRun = onRun,
                        onSelect = { position, i ->
                            onCancel()
                            setSelectedPositionAndIndex(position to i)
                        },
                    )
                }
            }

            currentState is Initial -> {
                {
                    MainForm(
                        inputUriString = inputUriString,
                        errorMessageResId = errorMessageResId,
                        onSetErrorMessageResId = setErrorMessageResId,
                        onSubmit = onStart,
                        onUpdateInput = onUpdateInput,
                    )
                }
            }

            else -> null
        },
        secondPane = when {
            (loadingIndicator !is LoadingIndicator.Large && currentState is ConversionState.HasResult) -> {
                {
                    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                        ResultSuccessMessage(
                            currentState,
                            automationFeatureValid,
                            loadingIndicator,
                            onCancel = onCancel,
                            onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                        )
                        ResultSuccessApps(
                            onRun = onRun,
                            windowSizeClass = windowSizeClass,
                        )
                    }
                }
            }

            currentState is Initial -> {
                {
                    MainInfo(
                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                        onNavigateToIntroScreen = onNavigateToIntroScreen,
                        onSetErrorMessageResId = setErrorMessageResId,
                        onUpdateInput = onUpdateInput,
                    )
                }
            }

            else -> null
        },
        bottomPane = when {
            (loadingIndicator !is LoadingIndicator.Large && currentState is ConversionState.HasError) -> {
                {
                    TextButton({
                        coroutineScope.launch {
                            AndroidTools.copyToClipboard(clipboard, currentState.inputUriString)
                        }
                    }) {
                        Text(
                            stringResource(R.string.conversion_succeeded_skip), Modifier.padding(
                                start = spacing.windowPadding, top = spacing.tiny, bottom = spacing.small
                            )
                        )
                    }
                }
            }

            (loadingIndicator !is LoadingIndicator.Large && currentState is ConversionState.HasResult) -> {
                {
                    TextButton({
                        coroutineScope.launch {
                            AndroidTools.copyToClipboard(clipboard, currentState.inputUriString)
                        }
                    }) {
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
            loadingIndicator is LoadingIndicator.Large -> null

            currentState is RequestedUnshortenPermission -> {
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

            currentState is RequestedParseHtmlPermission -> {
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

            currentState is LocationRationaleShown -> {
                {
                    ConfirmationDialog(
                        title = stringResource(currentState.permissionTitleResId),
                        confirmText = stringResource(R.string.conversion_permission_common_grant),
                        dismissText = stringResource(R.string.conversion_permission_common_deny),
                        onConfirmation = { onGrant(false) },
                        onDismissRequest = { onDeny(false) },
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("geoShareLocationRationaleDialog"),
                    ) {
                        Text(
                            AnnotatedString.fromHtml(currentState.action.permissionText()),
                            style = TextStyle(lineBreak = LineBreak.Paragraph),
                        )
                    }
                }
            }

            else -> null
        },
        containerColor = when {
            loadingIndicator is LoadingIndicator.Large -> MaterialTheme.colorScheme.surfaceContainer
            currentState is ConversionState.HasError -> MaterialTheme.colorScheme.errorContainer
            currentState is ConversionState.HasResult -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        contentColor = when {
            loadingIndicator is LoadingIndicator.Large -> Color.Unspecified
            currentState is ConversionState.HasError -> MaterialTheme.colorScheme.onErrorContainer
            currentState is ConversionState.HasResult -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> Color.Unspecified
        },
        ratio = if (currentState is Initial) 0.6f else 0.5f,
        windowSizeClass = windowSizeClass,
    )

    selectedPositionAndIndex?.let { (position, i) ->
        ModalBottomSheet(
            onDismissRequest = { setSelectedPositionAndIndex(null) },
            modifier = Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareConversionSheet")
                // Set and consume insets to prevent unclickable items when the sheet is expanded (probably a bug in
                // Compose Material 3)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            sheetState = sheetState,
        ) {
            ResultSuccessSheetContent(
                position = position,
                i = i,
                onHide = {
                    coroutineScope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                setSelectedPositionAndIndex(null)
                            }
                        }
                },
                onRun = onRun,
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MainScreen(
            currentState = Initial(),
            automationFeatureValid = true,
            changelogShown = false,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainScreen(
            currentState = Initial(),
            automationFeatureValid = true,
            changelogShown = false,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        MainScreen(
            currentState = Initial(),
            automationFeatureValid = null,
            changelogShown = false,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SucceededPreview() {
    AppTheme {
        MainScreen(
            currentState = ActionFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                action = NoopAutomation,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSucceededPreview() {
    AppTheme {
        MainScreen(
            currentState = ActionFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                action = NoopAutomation,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletSucceededPreview() {
    AppTheme {
        MainScreen(
            currentState = ActionFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                action = NoopAutomation,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                i = null,
                action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                delay = 3.seconds,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                i = null,
                action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                delay = 3.seconds,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                position = Position.example,
                i = null,
                action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                delay = 3.seconds,
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        MainScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        MainScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        MainScreen(
            currentState = ConversionFailed(
                errorMessageResId = R.string.conversion_failed_parse_url_error,
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = null,
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    NetworkTools(),
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = LoadingIndicator.Large(
                titleResId = R.string.converter_google_maps_loading_indicator_title,
                description = { null },
            ),
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    NetworkTools(),
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = LoadingIndicator.Large(
                titleResId = R.string.converter_google_maps_loading_indicator_title,
                description = { null },
            ),
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingIndicatorPreview() {
    AppTheme {
        val userPreferencesRepository = FakeUserPreferencesRepository()
        MainScreen(
            currentState = GrantedUnshortenPermission(
                ConversionStateContext(
                    listOf(),
                    NetworkTools(),
                    userPreferencesRepository = userPreferencesRepository,
                    features = Features(userPreferencesRepository),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    NetworkTools.RecoverableException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            automationFeatureValid = null,
            changelogShown = true,
            inputUriString = "",
            loadingIndicator = LoadingIndicator.Large(
                titleResId = R.string.converter_google_maps_loading_indicator_title,
                description = { null },
            ),
            onCancel = {},
            onDeny = {},
            onGrant = {},
            onNavigateToAboutScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToSubscriptionScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onReset = {},
            onRun = { _, _ -> },
            onStart = {},
            onUpdateInput = {},
        )
    }
}
