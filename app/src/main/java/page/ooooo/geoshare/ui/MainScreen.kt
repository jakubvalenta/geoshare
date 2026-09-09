package page.ooooo.geoshare.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.shouldAppFinish
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.conversion.ActionCompleted
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.FileActionReady
import page.ooooo.geoshare.lib.conversion.FileUriRequested
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleRequested
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.PermissionGrantedWebViewInput
import page.ooooo.geoshare.lib.conversion.PermissionRequested
import page.ooooo.geoshare.lib.extensions.truncateMiddle
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionContext
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.ConversionWebView
import page.ooooo.geoshare.ui.components.LargeTopAppBarPane
import page.ooooo.geoshare.ui.components.MainHeadline
import page.ooooo.geoshare.ui.components.MainHelp
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.MainSource
import page.ooooo.geoshare.ui.components.MainSubmit
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultApps
import page.ooooo.geoshare.ui.components.ResultCoordinates
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.MainLoadingIndicator
import page.ooooo.geoshare.ui.components.MainLog
import page.ooooo.geoshare.ui.components.ResultAppsHelpMessage
import page.ooooo.geoshare.ui.components.ResultSheet
import page.ooooo.geoshare.ui.components.ResultTitle
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
import page.ooooo.geoshare.ui.components.StyledSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.checkeredBackground
import page.ooooo.geoshare.ui.components.fakeStateLog
import page.ooooo.geoshare.ui.components.mainContainerColor
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.math.floor
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun MainScreen(
    onFinish: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId?) -> Unit,
    billingViewModel: BillingViewModel,
    conversionViewModel: ConversionViewModel,
    helpViewModel: HelpViewModel = hiltViewModel(),
    inputViewModel: InputViewModel = hiltViewModel(),
    outputViewModel: OutputViewModel = hiltViewModel(),
    linkViewModel: LinkViewModel = hiltViewModel(),
    userPreferenceViewModel: UserPreferenceViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

    val currentState by conversionViewModel.currentState.collectAsStateWithLifecycle()

    // Action

    var locationJob by remember { mutableStateOf<Job?>(null) }
    val locationPermissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            conversionViewModel.receiveLocationPermission()
        }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
                conversionViewModel.receiveFileUri(uri)
            } ?: conversionViewModel.cancelFileUriRequest()
        }

    LaunchedEffect(currentState) {
        currentState.let { currentState ->
            when (currentState) {
                // Basic action

                is BasicActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = currentState.action.execute(actionContext)
                    if (userPreferenceViewModel.values.value.finish.shouldAppFinish(actionResult)) {
                        onFinish()
                    }
                    conversionViewModel.completeBasicAction(actionResult)
                }

                // File action

                is FileUriRequested -> {
                    try {
                        saveFileLauncher.launch(
                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = currentState.action.mimeType
                                putExtra(Intent.EXTRA_TITLE, currentState.action.getFilename(resources))
                            }
                        )
                    } catch (_: ActivityNotFoundException) {
                        conversionViewModel.cancelFileUriRequest()
                    }
                }

                is FileActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = currentState.action.execute(currentState.uri, actionContext)
                    if (userPreferenceViewModel.values.value.finish.shouldAppFinish(actionResult)) {
                        onFinish()
                    }
                    conversionViewModel.completeFileAction(actionResult)
                }

                // Location action

                is LocationRationaleRequested -> {
                    if (AndroidTools.hasLocationPermission(context)) {
                        conversionViewModel.skipLocationRationale(currentState.action, currentState.isAutomation)
                    } else {
                        conversionViewModel.showLocationRationale(currentState.action, currentState.isAutomation)
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
                            conversionViewModel.cancelLocationFinding()
                            return@launch
                        }
                        conversionViewModel.receiveLocation(currentState.action, currentState.isAutomation, location)
                    }
                }

                is LocationActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = currentState.action.execute(currentState.location, actionContext)
                    if (userPreferenceViewModel.values.value.finish.shouldAppFinish(actionResult)) {
                        onFinish()
                    }
                    conversionViewModel.completeLocationAction(actionResult)
                }
            }
        }
    }

    MainScreen(
        currentState = currentState,
        appDetails = userPreferenceViewModel.appDetails,
        billingAppNameResId = billingViewModel.billingAppNameResId,
        billingFeatures = billingViewModel.billingFeatures,
        billingStatus = billingViewModel.billingStatus,
        changelogShown = inputViewModel.changelogShown,
        coordinateConverter = outputViewModel.coordinateConverter,
        dismissedHelpMessages = helpViewModel.dismissedHelpMessages,
        elapsedTime = conversionViewModel.elapsedTime,
        finishedStateLog = conversionViewModel.finishedStateLog,
        inputRepository = inputViewModel.inputRepository,
        linkMessage = linkViewModel.message,
        outputsForApps = outputViewModel.outputsForApps,
        outputsForLinks = outputViewModel.outputsForLinks,
        outputsForPoint = outputViewModel.outputsForPoint,
        outputsForPointChips = outputViewModel.outputsForPointChips,
        outputsForPoints = outputViewModel.outputsForPoints,
        outputsForPointsChips = outputViewModel.outputsForPointsChips,
        outputsForSharing = outputViewModel.outputsForSharing,
        startTimeMark = conversionViewModel.startTimeMark,
        source = conversionViewModel.source,
        sourceComesFromIntent = conversionViewModel.sourceComesFromIntent,
        userPreferenceMessage = userPreferenceViewModel.message,
        userPreferencesValues = userPreferenceViewModel.values,
        onCancel = {
            locationJob?.cancel()
            conversionViewModel.cancel()
        },
        onDeny = { doNotAsk -> conversionViewModel.deny(doNotAsk) },
        onDisableLinkGroup = { group -> linkViewModel.disableGroup(resources, group) },
        onDismissHelpMessage = { helpMessage -> helpViewModel.dismissHelpMessage(helpMessage) },
        onDismissLinkMessage = { linkViewModel.dismissMessage() },
        onDismissUserPreferenceMessage = { userPreferenceViewModel.dismissMessage() },
        onExecute = { action ->
            conversionViewModel.cancel()
            conversionViewModel.startAction(action)
        },
        onGrant = { doNotAsk -> conversionViewModel.grant(doNotAsk) },
        onHideApp = { packageName -> userPreferenceViewModel.hideApp(resources, packageName) },
        onNavigateToAboutScreen = {
            conversionViewModel.cancel()
            onNavigateToAboutScreen()
        },
        onNavigateToBillingScreen = {
            conversionViewModel.cancel()
            onNavigateToBillingScreen()
        },
        onNavigateToFaqScreen = { itemId ->
            conversionViewModel.cancel()
            onNavigateToFaqScreen(itemId)
        },
        onNavigateToInputsScreen = {
            conversionViewModel.cancel()
            onNavigateToInputsScreen()
        },
        onNavigateToLinkScreen = {
            conversionViewModel.cancel()
            onNavigateToLinkScreen()
        },
        onNavigateToUserPreferencesScreen = { groupId ->
            conversionViewModel.cancel()
            onNavigateToUserPreferencesScreen(groupId)
        },
        onReset = {
            conversionViewModel.cancel()
            conversionViewModel.reset()
        },
        onRetry = { conversionViewModel.retry() },
        onSetSource = { conversionViewModel.setSource(it) },
        onSubmit = { conversionViewModel.start(false) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    currentState: ConversionState,
    appDetails: StateFlow<AppDetails>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: StateFlow<BillingStatus>,
    changelogShown: StateFlow<Boolean>,
    coordinateConverter: CoordinateConverter,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    elapsedTime: StateFlow<Duration>,
    finishedStateLog: StateFlow<List<ConversionStateLogItem.Finished>>,
    inputRepository: InputRepository,
    linkMessage: StateFlow<Message?>,
    outputsForApps: StateFlow<Map<String, List<Output>>>,
    outputsForLinks: StateFlow<Map<String?, List<Output>>>,
    outputsForPoint: StateFlow<List<PointOutput>>,
    outputsForPointChips: StateFlow<List<PointOutput>>,
    outputsForPoints: StateFlow<List<PointsOutput>>,
    outputsForPointsChips: StateFlow<List<PointsOutput>>,
    outputsForSharing: StateFlow<List<Output>>,
    source: StateFlow<String>,
    sourceComesFromIntent: StateFlow<Boolean>,
    startTimeMark: StateFlow<ComparableTimeMark?>,
    userPreferenceMessage: StateFlow<Message?>,
    userPreferencesValues: StateFlow<UserPreferencesValues>,
    onCancel: () -> Unit,
    onDeny: (Boolean) -> Unit,
    onDisableLinkGroup: (String?) -> Unit,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onDismissLinkMessage: () -> Unit,
    onDismissUserPreferenceMessage: () -> Unit,
    onExecute: (Action<*>) -> Unit,
    onGrant: (Boolean) -> Unit,
    onHideApp: (String) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId?) -> Unit,
    onReset: () -> Unit,
    onRetry: () -> Unit,
    onSetSource: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val mainContainerColor = mainContainerColor(currentState)
    val mainContentColor = contentColorFor(mainContainerColor)
    val spacing = LocalSpacing.current

    val linkMessage by linkMessage.collectAsStateWithLifecycle()
    val userPreferenceMessage by userPreferenceMessage.collectAsStateWithLifecycle()

    val (errorMessageResId, setErrorMessageResId) = retain { mutableStateOf<Int?>(null) }
    val (logExpanded, setLogExpanded) = retain { mutableStateOf(false) }
    val (selectedPointIndex, setSelectedPointIndex) = retain { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(currentState !is Initial) {
        onReset()
    }

    // Message

    LaunchedEffect(linkMessage) {
        linkMessage?.let { linkMessage ->
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(linkMessage))
            onDismissLinkMessage()
        }
    }

    LaunchedEffect(userPreferenceMessage) {
        userPreferenceMessage?.let { userPreferenceMessage ->
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(userPreferenceMessage))
            onDismissUserPreferenceMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                MessageSnackbarHost(snackbarHostState)
            },
        ) {
            StyledSupportingPaneScaffold(
                mainPane = { innerPadding, wide -> // TODO Use inner padding
                    Column(Modifier.weight(1f)) {
                        LargeTopAppBarPane(
                            modifier = Modifier.testTag("geoShareMainPane"),
                            title = { maxLines ->
                                MainTitle(
                                    currentState = currentState,
                                    billingAppNameResId = billingAppNameResId,
                                    billingStatus = billingStatus,
                                    maxLines = maxLines,
                                )
                            },
                            onBack = if (currentState !is Initial) {
                                onReset
                            } else {
                                null
                            },
                            actions = {
                                if (!wide) {
                                    MainMenu(
                                        currentState = currentState,
                                        billingAppNameResId = billingAppNameResId,
                                        billingStatus = billingStatus,
                                        changelogShown = changelogShown,
                                        onNavigateToAboutScreen = onNavigateToAboutScreen,
                                        onNavigateToBillingScreen = onNavigateToBillingScreen,
                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                                        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                                    )
                                }
                            },
                            expandedHeight = if (currentState is Initial) {
                                spacing.largeTopAppBarExpandedHeight + spacing.medium
                            } else {
                                spacing.largeTopAppBarExpandedHeight
                            },
                        ) {
                            // Both phone and tablet

                            item {
                                MainSource(
                                    state = currentState,
                                    elapsedTime = elapsedTime,
                                    errorMessageResId = errorMessageResId,
                                    finishedStateLog = finishedStateLog,
                                    logExpanded = logExpanded,
                                    source = source,
                                    startTimeMark = startTimeMark,
                                    onSetLogExpanded = setLogExpanded,
                                    onSetErrorMessageResId = setErrorMessageResId,
                                    onSetSource = onSetSource,
                                    onSubmit = onSubmit,
                                )
                            }
                            item {
                                MainLog(
                                    expanded = logExpanded,
                                    finishedStateLog = finishedStateLog,
                                )
                            }

                            if (!wide) {
                                // Phone

                                when (currentState) {
                                    is ConversionState.HasError ->
                                        item {
                                            ResultError(
                                                state = currentState,
                                                onNavigateToInputsScreen = onNavigateToInputsScreen,
                                                onRetry = onRetry,
                                            )
                                        }

                                    is ConversionState.HasResult ->
                                        item {
                                            ResultCoordinates(
                                                points = currentState.points,
                                                appDetails = appDetails,
                                                coordinateConverter = coordinateConverter,
                                                dismissedHelpMessages = dismissedHelpMessages,
                                                outputsForApps = outputsForApps,
                                                outputsForPointChips = outputsForPointChips,
                                                outputsForPointsChips = outputsForPointsChips,
                                                sourceComesFromIntent = sourceComesFromIntent,
                                                userPreferencesValues = userPreferencesValues,
                                                onDismissHelpMessage = onDismissHelpMessage,
                                                onExecute = onExecute,
                                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                onSelect = { index ->
                                                    onCancel()
                                                    setSelectedPointIndex(index)
                                                },
                                            )
                                        }

                                    is Initial -> {
                                        item {
                                            MainSubmit(
                                                source = source,
                                                onSetErrorMessageResId = setErrorMessageResId,
                                                onSubmit = onSubmit,
                                            )
                                        }
                                        item {
                                            MainHelp(
                                                dismissedHelpMessages = dismissedHelpMessages,
                                                inputRepository = inputRepository,
                                                modifier = Modifier.padding(top = spacing.medium),
                                                onDismissHelpMessage = onDismissHelpMessage,
                                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                onNavigateToInputsScreen = onNavigateToInputsScreen,
                                                onSetErrorMessageResId = setErrorMessageResId,
                                                onSetSource = onSetSource,
                                            )
                                        }
                                    }

                                    is ConversionState.HasDescription ->
                                        item {
                                            MainLoadingIndicator(
                                                state = currentState,
                                                onCancel = onCancel,
                                            )
                                        }
                                }

                                when (currentState) {
                                    is ConversionState.HasResult ->
                                        item {
                                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                                                Column(
                                                    // This column must not have weight(1f), otherwise the last row of app
                                                    // icons gets shrunk
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surface)
                                                ) {
                                                    ResultTitle(
                                                        currentState = currentState,
                                                        appDetails = appDetails,
                                                        billingFeatures = billingFeatures,
                                                        billingStatus = billingStatus,
                                                        modifier = Modifier
                                                            .padding(horizontal = spacing.windowPadding)
                                                            .padding(top = spacing.medium),
                                                        onCancel = onCancel,
                                                        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                                                    )
                                                    ResultApps(
                                                        appDetails = appDetails,
                                                        outputsForApps = outputsForApps,
                                                        outputsForLinks = outputsForLinks,
                                                        outputsForSharing = outputsForSharing,
                                                        points = currentState.points,
                                                        onDisableLinkGroup = onDisableLinkGroup,
                                                        onExecute = onExecute,
                                                        onHideApp = onHideApp,
                                                        onNavigateToLinkScreen = onNavigateToLinkScreen,
                                                    )
                                                    ResultAppsHelpMessage(
                                                        dismissedHelpMessages = dismissedHelpMessages,
                                                        sourceComesFromIntent = sourceComesFromIntent,
                                                        onDismissHelpMessage = onDismissHelpMessage,
                                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                    )
                                                }
                                            }
                                        }
                                }
                            } else {
                                // Tablet

                                when (currentState) {
                                    is Initial ->
                                        item {
                                            MainSubmit(
                                                source = source,
                                                onSetErrorMessageResId = setErrorMessageResId,
                                                onSubmit = onSubmit,
                                            )
                                        }

                                    else ->
                                        item {
                                            Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = mainContainerColor,
                                                        contentColor = mainContentColor,
                                                    ),
                                                ) {
                                                    Spacer(Modifier.height(spacing.small))

                                                    when (currentState) {
                                                        is ConversionState.HasError ->
                                                            ResultError(
                                                                state = currentState,
                                                                onNavigateToInputsScreen = onNavigateToInputsScreen,
                                                                onRetry = onRetry,
                                                            )

                                                        is ConversionState.HasResult ->
                                                            ResultCoordinates(
                                                                points = currentState.points,
                                                                appDetails = appDetails,
                                                                coordinateConverter = coordinateConverter,
                                                                dismissedHelpMessages = dismissedHelpMessages,
                                                                outputsForApps = outputsForApps,
                                                                outputsForPointChips = outputsForPointChips,
                                                                outputsForPointsChips = outputsForPointsChips,
                                                                sourceComesFromIntent = sourceComesFromIntent,
                                                                userPreferencesValues = userPreferencesValues,
                                                                onDismissHelpMessage = onDismissHelpMessage,
                                                                onExecute = onExecute,
                                                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                                onSelect = { index ->
                                                                    onCancel()
                                                                    setSelectedPointIndex(index)
                                                                },
                                                            )

                                                        is ConversionState.HasDescription ->
                                                            MainLoadingIndicator(
                                                                state = currentState,
                                                                onCancel = onCancel,
                                                            )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }

                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (currentState is PermissionGrantedWebViewInput) {
                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                                    MainWebView(
                                        matchedInput = currentState.matchedInput,
                                        pendingData = currentState.pendingData,
                                    )
                                }
                            }
                        }
                    }
                },
                supportingPane = { wide ->
                    LargeTopAppBarPane(
                        modifier = Modifier.testTag("geoShareMainSupportingPane"),
                        title = if (currentState is ConversionState.HasResult) {
                            {
                                ResultTitle(
                                    currentState = currentState,
                                    appDetails = appDetails,
                                    billingFeatures = billingFeatures,
                                    billingStatus = billingStatus,
                                    onCancel = onCancel,
                                    onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                                )
                            }
                        } else {
                            null
                        },
                        actions = {
                            if (wide) {
                                MainMenu(
                                    currentState = currentState,
                                    billingAppNameResId = billingAppNameResId,
                                    billingStatus = billingStatus,
                                    changelogShown = changelogShown,
                                    onNavigateToAboutScreen = onNavigateToAboutScreen,
                                    onNavigateToBillingScreen = onNavigateToBillingScreen,
                                    onNavigateToFaqScreen = onNavigateToFaqScreen,
                                    onNavigateToInputsScreen = onNavigateToInputsScreen,
                                    onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                                )
                            }
                        },
                    ) {
                        when (currentState) {
                            is ConversionState.HasResult -> {
                                item {
                                    ResultApps(
                                        appDetails = appDetails,
                                        outputsForApps = outputsForApps,
                                        outputsForLinks = outputsForLinks,
                                        outputsForSharing = outputsForSharing,
                                        points = currentState.points,
                                        onDisableLinkGroup = onDisableLinkGroup,
                                        onExecute = onExecute,
                                        onHideApp = onHideApp,
                                        onNavigateToLinkScreen = onNavigateToLinkScreen,
                                    )
                                }
                                item {
                                    ResultAppsHelpMessage(
                                        dismissedHelpMessages = dismissedHelpMessages,
                                        sourceComesFromIntent = sourceComesFromIntent,
                                        onDismissHelpMessage = onDismissHelpMessage,
                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                    )
                                }
                            }

                            is Initial ->
                                item {
                                    MainHelp(
                                        dismissedHelpMessages = dismissedHelpMessages,
                                        inputRepository = inputRepository,
                                        modifier = Modifier.padding(top = spacing.medium),
                                        onDismissHelpMessage = onDismissHelpMessage,
                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                                        onSetErrorMessageResId = setErrorMessageResId,
                                        onSetSource = onSetSource,
                                    )
                                }
                        }
                    }
                },
                colors = StyledPaneScaffoldDefaults.colors(
                    mainContainerColor = mainContainerColor,
                    mainContentColor = mainContentColor,
                    wideMainContainerColor = Color.Transparent,
                    wideMainContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                shouldAutoFocusCurrentDestination = false,
            )
        }
    }

    if (currentState is ConversionState.HasResult && selectedPointIndex != null) {
        ResultSheet(
            points = currentState.points,
            selectedPointIndex = selectedPointIndex,
            appDetails = appDetails,
            outputsForPoint = outputsForPoint,
            outputsForPoints = outputsForPoints,
            onExecute = onExecute,
            onSelectPointIndex = setSelectedPointIndex,
        )
    }

    when (currentState) {
        is PermissionRequested ->
            PermissionDialog(
                title = stringResource(currentState.permissionTitleResId),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = onGrant,
                onDismissRequest = onDeny,
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareConnectionPermissionDialog"),
            ) {
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(
                            R.string.conversion_permission_common_text,
                            currentState.matchedInput.match.truncateMiddle(),
                            appName,
                        )
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }

        is LocationRationaleShown ->
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
                when (currentState.action) {
                    is LocationAction.WithPoint -> currentState.action.output.permissionText()
                    is LocationAction.WithPoints -> currentState.action.output.permissionText()
                }.let { text ->
                    Text(
                        AnnotatedString.fromHtml(text),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
            }
    }
}

@Composable
private fun MainTitle(
    currentState: ConversionState,
    billingAppNameResId: Int,
    billingStatus: StateFlow<BillingStatus>,
    maxLines: Int,
) {
    val resources = LocalResources.current

    val billingStatus by billingStatus.collectAsStateWithLifecycle()

    when (currentState) {
        is ConversionState.HasError ->
            Text(
                stringResource(R.string.conversion_error_title),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
            )

        is ConversionState.HasResult ->
            Text(
                currentState.points.lastOrNull()?.cleanName?.takeIf { it.isNotEmpty() }
                    ?: if (currentState.points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_last)
                    } else {
                        stringResource(R.string.conversion_succeeded_title)
                    },
                Modifier.testTag("geoShareResultLastPointName"),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
            )

        is Initial ->
            MainHeadline(
                appNameResId = if (billingStatus is BillingStatus.Purchased) {
                    billingAppNameResId
                } else {
                    R.string.app_name
                },
                modifier = Modifier.offset(x = -(12).dp),
            )

        is ConversionState.HasDescription ->
            Text(
                currentState.getDescription(resources),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
            )
    }
}

@Composable
private fun MainWebView(
    matchedInput: MatchedInput<WebViewInput>,
    pendingData: CompletableDeferred<String>,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            // Clip to bounds, so that the WebView inside this box never overflows the top edge of the box, which
            // can happen due to how Modifier.requiredSize, which we use in ConversionWebView, works
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val wholeSquaresCount = floor(maxWidth.value / 30)
        val squarePx = with(density) { (maxWidth / wholeSquaresCount).toPx() }
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.1f }
                .checkeredBackground(squarePx)
        )
        ConversionWebView(
            unsafeUrl = matchedInput.match,
            unsafeExtractionJavascript = matchedInput.input.getUnsafeExtractionJavaScript(matchedInput.match),
            pendingExtractionResult = pendingData,
            extendWebSettings = { matchedInput.input.extendWebSettings(it) },
            shouldInterceptRequest = { matchedInput.input.shouldInterceptRequest(it) },
        )
        if (!BuildConfig.DEBUG) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(emptyList()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(emptyList()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun SmallPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(emptyList()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(emptyList()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SucceededPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionCompleted(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
                actionResult = ActionResult.SUCCEEDED,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.COMAPS_FDROID to App(
                            packageName = PackageNames.COMAPS_FDROID,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.GMAPS_WV to App(
                            packageName = PackageNames.GMAPS_WV,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                        PackageNames.GOOGLE_MAPS to App(
                            packageName = PackageNames.GOOGLE_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.HERE_WEGO to App(
                            packageName = PackageNames.HERE_WEGO,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.MAGIC_EARTH to App(
                            packageName = PackageNames.MAGIC_EARTH,
                            dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                        ),
                        PackageNames.MAPY_COM to App(
                            packageName = PackageNames.MAPY_COM,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.ORGANIC_MAPS to App(
                            packageName = PackageNames.ORGANIC_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GPX_DATA)
                        ),
                        PackageNames.TOMTOM to App(
                            packageName = PackageNames.TOMTOM,
                            dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                        ),
                    ),
                    emptySet(),
                )
            ),
            outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(outputRepository.getOutputsForPointChips(defaultFakeLinks)),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(outputRepository.getOutputsForPointsChips()),
            outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(true),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSucceededPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionCompleted(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
                actionResult = ActionResult.SUCCEEDED,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.COMAPS_FDROID to App(
                            packageName = PackageNames.COMAPS_FDROID,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.GMAPS_WV to App(
                            packageName = PackageNames.GMAPS_WV,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                        PackageNames.GOOGLE_MAPS to App(
                            packageName = PackageNames.GOOGLE_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.HERE_WEGO to App(
                            packageName = PackageNames.HERE_WEGO,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.MAGIC_EARTH to App(
                            packageName = PackageNames.MAGIC_EARTH,
                            dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                        ),
                        PackageNames.MAPY_COM to App(
                            packageName = PackageNames.MAPY_COM,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.ORGANIC_MAPS to App(
                            packageName = PackageNames.ORGANIC_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GPX_DATA)
                        ),
                        PackageNames.TOMTOM to App(
                            packageName = PackageNames.TOMTOM,
                            dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                        ),
                    ),
                    emptySet(),
                )
            ),
            outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(outputRepository.getOutputsForPointChips(defaultFakeLinks)),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(outputRepository.getOutputsForPointsChips()),
            outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(true),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun SmallSucceededPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionCompleted(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = "Wikimedia Foundation, Inc.",
                    ),
                ),
                actionResult = ActionResult.SUCCEEDED,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.COMAPS_FDROID to App(
                            packageName = PackageNames.COMAPS_FDROID,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.GMAPS_WV to App(
                            packageName = PackageNames.GMAPS_WV,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                        PackageNames.GOOGLE_MAPS to App(
                            packageName = PackageNames.GOOGLE_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.HERE_WEGO to App(
                            packageName = PackageNames.HERE_WEGO,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.MAGIC_EARTH to App(
                            packageName = PackageNames.MAGIC_EARTH,
                            dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                        ),
                        PackageNames.MAPY_COM to App(
                            packageName = PackageNames.MAPY_COM,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.ORGANIC_MAPS to App(
                            packageName = PackageNames.ORGANIC_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GPX_DATA)
                        ),
                        PackageNames.TOMTOM to App(
                            packageName = PackageNames.TOMTOM,
                            dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                        ),
                    ),
                    emptySet(),
                )
            ),
            outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(outputRepository.getOutputsForPointChips(defaultFakeLinks)),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(outputRepository.getOutputsForPointsChips()),
            outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(true),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletSucceededPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionCompleted(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
                actionResult = ActionResult.SUCCEEDED,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.COMAPS_FDROID to App(
                            packageName = PackageNames.COMAPS_FDROID,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.GMAPS_WV to App(
                            packageName = PackageNames.GMAPS_WV,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                        PackageNames.GOOGLE_MAPS to App(
                            packageName = PackageNames.GOOGLE_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.HERE_WEGO to App(
                            packageName = PackageNames.HERE_WEGO,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.MAGIC_EARTH to App(
                            packageName = PackageNames.MAGIC_EARTH,
                            dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                        ),
                        PackageNames.MAPY_COM to App(
                            packageName = PackageNames.MAPY_COM,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.ORGANIC_MAPS to App(
                            packageName = PackageNames.ORGANIC_MAPS,
                            dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                        ),
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GPX_DATA)
                        ),
                        PackageNames.TOMTOM to App(
                            packageName = PackageNames.TOMTOM,
                            dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                        ),
                    ),
                    emptySet(),
                )
            ),
            outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(outputRepository.getOutputsForPointChips(defaultFakeLinks)),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(outputRepository.getOutputsForPointsChips()),
            outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = ConversionFailed(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = ConversionFailed(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = ConversionFailed(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = ConversionFailed(
                source = "https://share.google/diIxnYa8dIA6dZfpy",
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWarningPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = ConversionFailed(
                source = "https://share.google/diIxnYa8dIA6dZfpy",
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = PermissionGrantedWebViewInput(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        MainScreen(
            currentState = PermissionGrantedWebViewInput(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletWebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val currentState = PermissionGrantedWebViewInput(
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        )
        MainScreen(
            currentState = currentState,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(fakeStateLog()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val currentState = ConversionSucceeded(
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            points = persistentListOf(),
        )
        MainScreen(
            currentState = currentState,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                )
            ),
            changelogShown = MutableStateFlow(true),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(null),
            elapsedTime = MutableStateFlow(3200.milliseconds),
            finishedStateLog = MutableStateFlow(emptyList()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForApps = MutableStateFlow(emptyMap()),
            outputsForLinks = MutableStateFlow(emptyMap()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(emptyList()),
            startTimeMark = MutableStateFlow(TestTimeSource().markNow()),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
            onCancel = {},
            onDeny = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onExecute = {},
            onDismissUserPreferenceMessage = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}
