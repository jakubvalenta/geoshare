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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.getLocation
import page.ooooo.geoshare.data.android.hasLocationPermission
import page.ooooo.geoshare.data.billing.AutomationFeature
import page.ooooo.geoshare.data.billing.BillingProduct
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.billing.CustomLinkFeature
import page.ooooo.geoshare.data.billing.Feature
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.extensions.truncateMiddle
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.inputs.MatchedInput
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.message.Message
import page.ooooo.geoshare.data.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.ActionContext
import page.ooooo.geoshare.data.outputs.LocationAction
import page.ooooo.geoshare.data.preferences.CoordinateFormat
import page.ooooo.geoshare.data.preferences.HelpMessage
import page.ooooo.geoshare.data.preferences.Permission
import page.ooooo.geoshare.data.preferences.shouldAppFinish
import page.ooooo.geoshare.data.state.ActionState
import page.ooooo.geoshare.data.state.Attempt
import page.ooooo.geoshare.data.state.BasicActionReady
import page.ooooo.geoshare.data.state.ConversionFailed
import page.ooooo.geoshare.data.state.ConversionState
import page.ooooo.geoshare.data.state.ConversionSucceeded
import page.ooooo.geoshare.data.state.ExtendedStateLog
import page.ooooo.geoshare.data.state.FileActionReady
import page.ooooo.geoshare.data.state.FileUriRequested
import page.ooooo.geoshare.data.state.LocationActionReady
import page.ooooo.geoshare.data.state.LocationPermissionReceived
import page.ooooo.geoshare.data.state.LocationRationaleConfirmed
import page.ooooo.geoshare.data.state.LocationRationaleRequested
import page.ooooo.geoshare.data.state.LocationRationaleShown
import page.ooooo.geoshare.data.state.PermissionGrantedBasicInput
import page.ooooo.geoshare.data.state.PermissionGrantedWebViewInput
import page.ooooo.geoshare.data.state.PermissionRequested
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.ConversionStateLogList
import page.ooooo.geoshare.ui.components.ConversionWebView
import page.ooooo.geoshare.ui.components.HelpOpenByDefaultMessage
import page.ooooo.geoshare.ui.components.HelpShareSourceMessage
import page.ooooo.geoshare.ui.components.HelpWelcomeMessage
import page.ooooo.geoshare.ui.components.MainForm
import page.ooooo.geoshare.ui.components.MainHeadline
import page.ooooo.geoshare.ui.components.MainHelp
import page.ooooo.geoshare.ui.components.MainLoadingIndicator
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.MainScaffold
import page.ooooo.geoshare.ui.components.MainSourceBar
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultApps
import page.ooooo.geoshare.ui.components.ResultCoordinates
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.ResultLinks
import page.ooooo.geoshare.ui.components.ResultPoints
import page.ooooo.geoshare.ui.components.ResultSheet
import page.ooooo.geoshare.ui.components.ResultTitle
import page.ooooo.geoshare.ui.components.fakeStateLog
import page.ooooo.geoshare.ui.components.helpLifecycleMessage
import page.ooooo.geoshare.ui.string.FormatArg
import page.ooooo.geoshare.ui.string.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.defaultLinkStyles
import kotlin.time.ComparableTimeMark
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
    mainViewModel: MainViewModel,
    helpViewModel: HelpViewModel = hiltViewModel(),
    inputViewModel: InputViewModel = hiltViewModel(),
    linkViewModel: LinkViewModel = hiltViewModel(),
    userPreferenceViewModel: UserPreferenceViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

    val conversionState by mainViewModel.conversionState.collectAsStateWithLifecycle()
    val actionState by mainViewModel.actionState.collectAsStateWithLifecycle()

    // Action

    var locationJob by remember { mutableStateOf<Job?>(null) }
    val locationPermissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            mainViewModel.receiveLocationPermission()
        }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
                mainViewModel.receiveFileUri(uri)
            } ?: mainViewModel.cancelFileUriRequest()
        }

    LaunchedEffect(actionState) {
        actionState.let { actionState ->
            when (actionState) {
                // Basic action

                is BasicActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = actionState.action.execute(actionContext)
                    if (userPreferenceViewModel.getFinish().shouldAppFinish(actionResult)) {
                        onFinish() // TODO Don't finish if conversion is running
                    }
                    mainViewModel.completeBasicAction(actionResult)
                }

                // File action

                is FileUriRequested -> {
                    try {
                        saveFileLauncher.launch(
                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = actionState.action.mimeType
                                putExtra(Intent.EXTRA_TITLE, actionState.action.getFilename(resources))
                            }
                        )
                    } catch (_: ActivityNotFoundException) {
                        mainViewModel.cancelFileUriRequest()
                    }
                }

                is FileActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = actionState.action.execute(actionState.uri, actionContext)
                    if (userPreferenceViewModel.getFinish().shouldAppFinish(actionResult)) {
                        onFinish()
                    }
                    mainViewModel.completeFileAction(actionResult)
                }

                // Location action

                is LocationRationaleRequested -> {
                    if (context.hasLocationPermission()) {
                        mainViewModel.skipLocationRationale(actionState.action, actionState.isAutomation)
                    } else {
                        mainViewModel.showLocationRationale(actionState.action, actionState.isAutomation)
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
                            context.getLocation()
                        } catch (_: CancellationException) {
                            mainViewModel.cancelLocationFinding()
                            return@launch
                        }
                        mainViewModel.receiveLocation(actionState.action, actionState.isAutomation, location)
                    }
                }

                is LocationActionReady -> {
                    val actionContext = ActionContext(context = context, clipboard = clipboard, resources = resources)
                    val actionResult = actionState.action.execute(actionState.location, actionContext)
                    if (userPreferenceViewModel.getFinish().shouldAppFinish(actionResult)) {
                        onFinish()
                    }
                    mainViewModel.completeLocationAction(actionResult)
                }
            }
        }
    }

    MainScreen(
        conversionState = conversionState,
        actionState = actionState,
        appDetails = mainViewModel.appDetails,
        billingAppNameResId = billingViewModel.billingAppNameResId,
        billingFeatures = billingViewModel.billingFeatures,
        billingStatus = billingViewModel.billingStatus,
        changelogShown = inputViewModel.changelogShown,
        coordinateConverter = mainViewModel.coordinateConverter,
        coordinateFormat = userPreferenceViewModel.coordinateFormat,
        dismissedHelpMessages = helpViewModel.dismissedHelpMessages,
        error = mainViewModel.error,
        linkMessage = linkViewModel.message,
        placeholderUris = inputViewModel.placeholderUris,
        resultAppsState = mainViewModel.resultAppsState,
        resultCoordinatesState = mainViewModel.resultCoordinatesState,
        resultLinksState = mainViewModel.resultLinksState,
        resultPointsState = mainViewModel.resultPointsState,
        resultSheetState = mainViewModel.resultSheetState,
        source = mainViewModel.source,
        sourceComesFromIntent = mainViewModel.sourceComesFromIntent,
        start = mainViewModel.conversionStart,
        stateLog = mainViewModel.extendedConversionStateLog,
        userPreferenceMessage = userPreferenceViewModel.message,
        onCancelAction = {
            locationJob?.cancel()
            mainViewModel.cancelAction()
        },
        onCancelConversion = { mainViewModel.cancelConversion() },
        onCopyPoint = { point, pointCount, pointIndex -> mainViewModel.copyPoint(point, pointCount, pointIndex) },
        onDenyAction = { doNotAsk -> mainViewModel.denyAction(doNotAsk) },
        onDenyConversion = { doNotAsk -> mainViewModel.deny(doNotAsk) },
        onDisableLinkGroup = { group -> linkViewModel.disableGroup(resources, group) },
        onDismissHelpMessage = { helpMessage -> helpViewModel.dismissHelpMessage(helpMessage) },
        onDismissLinkMessage = { linkViewModel.dismissMessage() },
        onDismissUserPreferenceMessage = { userPreferenceViewModel.dismissMessage() },
        onExecute = { action -> mainViewModel.startAction(action) },
        onGrantAction = { doNotAsk -> mainViewModel.grantAction(doNotAsk) },
        onGrantConversion = { doNotAsk -> mainViewModel.grant(doNotAsk) },
        onHideApp = { packageName -> userPreferenceViewModel.hideApp(resources, packageName) },
        onHideSheet = { mainViewModel.hideSheet() },
        onOpenPoint = { point, pointCount, pointIndex -> mainViewModel.openPoint(point, pointCount, pointIndex) },
        onNavigateToAboutScreen = {
            mainViewModel.cancelAction()
            onNavigateToAboutScreen()
        },
        onNavigateToBillingScreen = {
            mainViewModel.cancelAction()
            onNavigateToBillingScreen()
        },
        onNavigateToFaqScreen = { itemId ->
            mainViewModel.cancelAction()
            onNavigateToFaqScreen(itemId)
        },
        onNavigateToInputsScreen = {
            mainViewModel.cancelAction()
            onNavigateToInputsScreen()
        },
        onNavigateToLinkScreen = {
            mainViewModel.cancelAction()
            onNavigateToLinkScreen()
        },
        onNavigateToUserPreferencesScreen = { groupId ->
            mainViewModel.cancelAction()
            onNavigateToUserPreferencesScreen(groupId)
        },
        onReset = { mainViewModel.reset() },
        onRetry = { mainViewModel.retry() },
        onSavePoint = { point, pointCount, pointIndex -> mainViewModel.savePoint(point, pointCount, pointIndex) },
        onSavePoints = { points -> mainViewModel.savePoints(points) },
        onSharePoints = { points -> mainViewModel.sharePoints(points) },
        onShareSource = { source -> mainViewModel.shareSource(source) },
        onSetSource = { mainViewModel.setSource(it) },
        onSetRandomSource = { mainViewModel.setRandomSource() },
        onSubmit = { mainViewModel.start(false) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainScreen(
    conversionState: ConversionState,
    actionState: ActionState,
    appDetails: StateFlow<AppDetails>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: StateFlow<BillingStatus>,
    changelogShown: StateFlow<Boolean>,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: StateFlow<CoordinateFormat>,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    error: StateFlow<String?>,
    linkMessage: StateFlow<Message?>,
    placeholderUris: StateFlow<List<String>>,
    resultAppsState: StateFlow<ResultAppsState>,
    resultCoordinatesState: StateFlow<ResultCoordinatesState>,
    resultLinksState: StateFlow<ResultLinksState>,
    resultPointsState: StateFlow<ResultPointsState>,
    resultSheetState: StateFlow<ResultSheetState?>,
    source: StateFlow<String>,
    sourceComesFromIntent: StateFlow<Boolean>,
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>>,
    userPreferenceMessage: StateFlow<Message?>,
    onCancelAction: () -> Unit,
    onCancelConversion: () -> Unit,
    onCopyPoint: (point: Point, pointCount: Int, pointIndex: Int) -> Unit,
    onDenyAction: (Boolean) -> Unit,
    onDenyConversion: (Boolean) -> Unit,
    onDisableLinkGroup: (String?) -> Unit,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onDismissLinkMessage: () -> Unit,
    onDismissUserPreferenceMessage: () -> Unit,
    onExecute: (Action<*>) -> Unit,
    onGrantAction: (Boolean) -> Unit,
    onGrantConversion: (Boolean) -> Unit,
    onHideApp: (String) -> Unit,
    onHideSheet: () -> Unit,
    onOpenPoint: (point: Point, pointCount: Int, pointIndex: Int) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId?) -> Unit,
    onReset: () -> Unit,
    onRetry: () -> Unit,
    onSavePoint: (point: Point, pointCount: Int, pointIndex: Int) -> Unit,
    onSavePoints: (points: Points) -> Unit,
    onSetRandomSource: () -> Unit,
    onSetSource: (String) -> Unit,
    onSharePoints: (points: Points) -> Unit,
    onShareSource: (source: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val resources = LocalResources.current

    val linkMessage by linkMessage.collectAsStateWithLifecycle()
    val userPreferenceMessage by userPreferenceMessage.collectAsStateWithLifecycle()

    var logExpanded by retain { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(conversionState !is ConversionState.Initial) {
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

    Scaffold(
        snackbarHost = {
            MessageSnackbarHost(snackbarHostState)
        },
    ) { _ ->
        MainScaffold(
            actions = {
                MainMenu(
                    conversionState = conversionState,
                    billingAppNameResId = billingAppNameResId,
                    billingStatus = billingStatus,
                    changelogShown = changelogShown,
                    onNavigateToAboutScreen = onNavigateToAboutScreen,
                    onNavigateToBillingScreen = onNavigateToBillingScreen,
                    onNavigateToFaqScreen = onNavigateToFaqScreen,
                    onNavigateToInputsScreen = onNavigateToInputsScreen,
                    onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                )
            },
            topContent = {
                when (conversionState) {
                    is ConversionState.Initial -> {
                        item(key = "main_form", contentType = "main_form") {
                            MainForm(
                                error = error,
                                placeholderUris = placeholderUris,
                                source = source,
                                onSetSource = onSetSource,
                                onSubmit = onSubmit,
                            )
                        }
                    }

                    else -> {
                        item(key = "result", contentType = "column") {
                            Column(
                                Modifier.run {
                                    if (conversionState is PermissionGrantedWebViewInput) {
                                        Modifier.fillParentMaxSize()
                                    } else {
                                        this
                                    }
                                }
                            ) {
                                ConversionStateLogList(
                                    expanded = logExpanded,
                                    stateLog = stateLog,
                                    onShareSource = onShareSource,
                                )
                                when (conversionState) {
                                    is ConversionState.HasError ->
                                        ResultError(
                                            conversionState = conversionState,
                                            onNavigateToInputsScreen = onNavigateToInputsScreen,
                                            onRetry = onRetry,
                                        )

                                    is ConversionState.HasResult -> {
                                        conversionState.points.lastOrNull()?.let { point ->
                                            ResultCoordinates(
                                                appDetails = appDetails,
                                                state = resultCoordinatesState,
                                                point = point,
                                                pointCount = conversionState.points.size,
                                                pointIndex = conversionState.points.size - 1,
                                                coordinateConverter = coordinateConverter,
                                                coordinateFormat = coordinateFormat,
                                                inputGroup = conversionState.matchedInput.input.group,
                                                onCopyPoint = onCopyPoint,
                                                onExecute = onExecute,
                                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                onSavePoint = onSavePoint,
                                            ) {
                                                if (conversionState.points.size > 1) {
                                                    ResultPoints(
                                                        appDetails = appDetails,
                                                        state = resultPointsState,
                                                        points = conversionState.points,
                                                        coordinateConverter = coordinateConverter,
                                                        coordinateFormat = coordinateFormat,
                                                        onCopyPoint = onCopyPoint,
                                                        onExecute = onExecute,
                                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                                        onOpenPoint = onOpenPoint,
                                                        onSavePoint = onSavePoint,
                                                        onSavePoints = onSavePoints,
                                                        onSharePoints = onSharePoints,
                                                    )
                                                }
                                            }
                                        }
                                        if (helpLifecycleMessage != null) {
                                            helpLifecycleMessage(
                                                Modifier.padding(top = spacing.tiny)
                                            )
                                        } else {
                                            HelpShareSourceMessage(
                                                appDetails = appDetails,
                                                state = resultAppsState,
                                                dismissedHelpMessages = dismissedHelpMessages,
                                                sourceComesFromIntent = sourceComesFromIntent,
                                                modifier = Modifier.padding(top = spacing.tiny),
                                                onDismissHelpMessage = onDismissHelpMessage,
                                                onExecute = onExecute,
                                            )
                                        }
                                    }

                                    is ConversionState.HasDescription ->
                                        conversionState.getLoadingIndicatorTitle(resources)?.let { title ->
                                            MainLoadingIndicator(
                                                conversionState = conversionState,
                                                title = title,
                                                onCancel = onCancelConversion,
                                            )
                                        }
                                }

                                if (conversionState is PermissionGrantedWebViewInput) {
                                    Box(
                                        Modifier
                                            // Clip to bounds, so that the WebView inside this box never overflows the
                                            // top edge of the box. The overflowing can happen due to how
                                            // Modifier.requiredSize, which we use in ConversionWebView, works.
                                            .clipToBounds()
                                    ) {
                                        ConversionWebView(
                                            url = conversionState.matchedInput.match,
                                            allowedUrlPattern = conversionState.matchedInput.input.allowedUrlPattern,
                                            pendingResult = conversionState.pendingData,
                                            unsafeClientJavascript = conversionState.matchedInput.input.unsafeClientJavaScript,
                                            extendWebSettings = {
                                                conversionState.matchedInput.input.extendWebSettings(it)
                                            },
                                            shouldInterceptRequest = {
                                                conversionState.matchedInput.input.shouldInterceptRequest(it)
                                            },
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
                            }
                        }
                    }
                }
            },
            bottomContent = when (conversionState) {
                is ConversionState.Initial -> {
                    {
                        if (helpLifecycleMessage != null) {
                            item(key = "help_lifecycle_message", contentType = "help_lifecycle_message") {
                                helpLifecycleMessage(
                                    Modifier.padding(
                                        horizontal = spacing.windowPadding,
                                        vertical = spacing.tiny,
                                    )
                                )
                            }
                        } else {
                            item(key = "help_welcome_message", contentType = "help_welcome_message") {
                                HelpWelcomeMessage(
                                    dismissedHelpMessages = dismissedHelpMessages,
                                    modifier = Modifier.padding(
                                        horizontal = spacing.windowPadding,
                                        vertical = spacing.tiny,
                                    ),
                                    onDismissHelpMessage = onDismissHelpMessage,
                                )
                            }
                        }
                        item(key = "main_help", contentType = "column") {
                            MainHelp(
                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                onNavigateToInputsScreen = onNavigateToInputsScreen,
                                onSetRandomSource = onSetRandomSource,
                            )
                        }
                    }
                }

                is ConversionState.HasResult -> {
                    {
                        item(key = "result_apps", contentType = "result_apps") {
                            ResultApps(
                                appDetails = appDetails,
                                state = resultAppsState,
                                points = conversionState.points,
                                source = source,
                                modifier = Modifier.padding(top = spacing.tiny),
                                onExecute = onExecute,
                                onHideApp = onHideApp,
                            )
                        }
                        item(key = "result_links", contentType = "result_links") {
                            ResultLinks(
                                state = resultLinksState,
                                points = conversionState.points,
                                source = source,
                                onDisableLinkGroup = onDisableLinkGroup,
                                onExecute = onExecute,
                                onNavigateToLinkScreen = onNavigateToLinkScreen,
                            )
                        }
                        item(key = "help_open_by_default_message", contentType = "help_open_by_default_message") {
                            HelpOpenByDefaultMessage(
                                dismissedHelpMessages = dismissedHelpMessages,
                                sourceComesFromIntent = sourceComesFromIntent,
                                modifier = Modifier
                                    .padding(horizontal = spacing.windowPadding)
                                    .padding(top = spacing.small),
                                onDismissHelpMessage = onDismissHelpMessage,
                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                            )
                        }
                    }
                }

                else -> null
            },
            mainExpandable = conversionState is ConversionState.Initial,
            mainTitle = {
                if (conversionState is ConversionState.Initial) {
                    val billingStatus by billingStatus.collectAsStateWithLifecycle()
                    MainHeadline(
                        appNameResId = if (billingStatus is BillingStatus.Purchased) {
                            billingAppNameResId
                        } else {
                            R.string.app_name
                        },
                        modifier = Modifier.offset((-12).dp),
                    )
                } else {
                    MainSourceBar(
                        conversionState = conversionState,
                        logExpanded = logExpanded,
                        start = start,
                        stateLog = stateLog,
                        onSetLogExpanded = { logExpanded = it },
                        onShareSource = onShareSource,
                    )
                }
            },
            supportingTitle = if (conversionState is ConversionState.HasResult) {
                {
                    ResultTitle(
                        appDetails = appDetails,
                        actionState = actionState,
                        billingFeatures = billingFeatures,
                        billingStatus = billingStatus,
                        onCancelAction = onCancelAction,
                        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                    )
                }
            } else {
                null
            },
            onBack = if (conversionState !is ConversionState.Initial) {
                onReset
            } else {
                null
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    ResultSheet(
        appDetails = appDetails,
        state = resultSheetState,
        onExecute = onExecute,
        onDismissRequest = onHideSheet,
    )

    when (conversionState) {
        is PermissionRequested ->
            PermissionDialog(
                title = stringResource(
                    R.string.conversion_permission,
                    conversionState.matchedInput.input.group.getName(resources),
                ),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = onGrantConversion,
                onDismissRequest = onDenyConversion,
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareConnectionPermissionDialog"),
            ) {
                val appName = stringResource(R.string.app_name)
                Text(
                    annotatedStringResource(
                        R.string.conversion_permission_common_text,
                        textLinkStyles = defaultLinkStyles(),
                        FormatArg.Text(
                            conversionState.matchedInput.match.truncateMiddle(),
                            style = SpanStyle(fontFamily = FontFamily.Monospace),
                        ),
                        FormatArg.Text(appName),
                    ),
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
    }

    when (actionState) {
        is LocationRationaleShown ->
            ConfirmationDialog(
                title = stringResource(R.string.conversion_succeeded_location_rationale_dialog_title),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = { onGrantAction(false) },
                onDismissRequest = { onDenyAction(false) },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareLocationRationaleDialog"),
            ) {
                when (actionState.action) {
                    is LocationAction.WithPoint -> actionState.action.output.permissionText()
                    is LocationAction.WithPoints -> actionState.action.output.permissionText()
                }.let { text ->
                    Text(
                        AnnotatedString.fromHtml(text),
                        style = TextStyle(lineBreak = LineBreak.Paragraph),
                    )
                }
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
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionState.Initial,
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionState.Initial,
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
            sourceComesFromIntent = MutableStateFlow(false),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionState.Initial,
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionState.Initial,
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(emptyMap()),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(""),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val log = DefaultLog
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionSucceeded(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
            ),
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            resultCoordinatesState = MutableStateFlow(
                ResultCoordinatesState(chipsOutputs = outputRepository.getPointChipsOutputs(defaultFakeLinks))
            ),
            resultLinksState = MutableStateFlow(
                ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
            ),
            resultPointsState = MutableStateFlow(ResultPointsState(chipsOutputs = outputRepository.pointsChipsOutputs)),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val log = DefaultLog
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionSucceeded(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
            ),
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            resultCoordinatesState = MutableStateFlow(
                ResultCoordinatesState(chipsOutputs = outputRepository.getPointChipsOutputs(defaultFakeLinks))
            ),
            resultLinksState = MutableStateFlow(
                ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
            ),
            resultPointsState = MutableStateFlow(ResultPointsState(chipsOutputs = outputRepository.pointsChipsOutputs)),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val log = DefaultLog
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionSucceeded(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = "Wikimedia Foundation, Inc.",
                    ),
                ),
            ),
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            resultCoordinatesState = MutableStateFlow(
                ResultCoordinatesState(chipsOutputs = outputRepository.getPointChipsOutputs(defaultFakeLinks))
            ),
            resultLinksState = MutableStateFlow(
                ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
            ),
            resultPointsState = MutableStateFlow(ResultPointsState(chipsOutputs = outputRepository.pointsChipsOutputs)),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(true),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val log = DefaultLog
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionSucceeded(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
            ),
            actionState = ActionState.Initial,
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(setOf(HelpMessage.SHARE_SOURCE)),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            resultCoordinatesState = MutableStateFlow(
                ResultCoordinatesState(chipsOutputs = outputRepository.getPointChipsOutputs(defaultFakeLinks))
            ),
            resultLinksState = MutableStateFlow(
                ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
            ),
            resultPointsState = MutableStateFlow(ResultPointsState(chipsOutputs = outputRepository.pointsChipsOutputs)),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://share.google/diIxnYa8dIA6dZfpy"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://share.google/diIxnYa8dIA6dZfpy"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedWebViewInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedWebViewInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = PermissionGrantedWebViewInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, source),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            conversionState = ConversionSucceeded(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
                points = persistentListOf(),
            ),
            actionState = ActionState.Initial,
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
            coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
            dismissedHelpMessages = MutableStateFlow(null),
            error = MutableStateFlow(null),
            linkMessage = MutableStateFlow(null),
            placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
            resultAppsState = MutableStateFlow(ResultAppsState()),
            resultCoordinatesState = MutableStateFlow(ResultCoordinatesState()),
            resultLinksState = MutableStateFlow(ResultLinksState()),
            resultPointsState = MutableStateFlow(ResultPointsState()),
            resultSheetState = MutableStateFlow(null),
            source = MutableStateFlow(source),
            sourceComesFromIntent = MutableStateFlow(false),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
            userPreferenceMessage = MutableStateFlow(null),
            onCancelAction = {},
            onCancelConversion = {},
            onCopyPoint = { _, _, _ -> },
            onDenyAction = {},
            onDenyConversion = {},
            onDisableLinkGroup = {},
            onDismissHelpMessage = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onExecute = {},
            onGrantAction = {},
            onGrantConversion = {},
            onHideApp = {},
            onHideSheet = {},
            onOpenPoint = { _, _, _ -> },
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onSavePoint = { _, _, _ -> },
            onSavePoints = {},
            onSetSource = {},
            onSetRandomSource = {},
            onSharePoints = {},
            onShareSource = {},
            onSubmit = {},
        )
    }
}
