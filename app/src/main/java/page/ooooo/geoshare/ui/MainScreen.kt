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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.shouldAppFinish
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.android.getLocation
import page.ooooo.geoshare.lib.android.hasLocationPermission
import page.ooooo.geoshare.lib.android.isMessagingApp
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.conversion.ActionCompleted
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.ExtendedConversionStateLogItem
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
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.ConversionStateLogList
import page.ooooo.geoshare.ui.components.ConversionUriSheet
import page.ooooo.geoshare.ui.components.ConversionWebView
import page.ooooo.geoshare.ui.components.HelpOpenByDefaultMessage
import page.ooooo.geoshare.ui.components.HelpShareSourceMessage
import page.ooooo.geoshare.ui.components.HelpWelcomeMessage
import page.ooooo.geoshare.ui.components.MainHeadline
import page.ooooo.geoshare.ui.components.MainHelp
import page.ooooo.geoshare.ui.components.MainLoadingIndicator
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.MainScaffold
import page.ooooo.geoshare.ui.components.MainSourceBar
import page.ooooo.geoshare.ui.components.MainSubmit
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultApps
import page.ooooo.geoshare.ui.components.ResultCoordinates
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.ResultSheet
import page.ooooo.geoshare.ui.components.ResultTitle
import page.ooooo.geoshare.ui.components.checkeredBackground
import page.ooooo.geoshare.ui.components.fakeStateLog
import page.ooooo.geoshare.ui.components.mainContainerColor
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.math.floor
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
                    if (context.hasLocationPermission()) {
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
                            context.getLocation()
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
        actionDetail = conversionViewModel.actionDetail,
        billingAppNameResId = billingViewModel.billingAppNameResId,
        billingFeatures = billingViewModel.billingFeatures,
        billingStatus = billingViewModel.billingStatus,
        changelogShown = inputViewModel.changelogShown,
        coordinateConverter = outputViewModel.coordinateConverter,
        dismissedHelpMessages = helpViewModel.dismissedHelpMessages,
        inputRepository = inputViewModel.inputRepository,
        linkMessage = linkViewModel.message,
        outputsForAppsByCategory = outputViewModel.outputsForAppsByCategory,
        outputsForLinks = outputViewModel.outputsForLinks,
        outputsForPoint = outputViewModel.outputsForPoint,
        outputsForPointChips = outputViewModel.outputsForPointChips,
        outputsForPoints = outputViewModel.outputsForPoints,
        outputsForPointsChips = outputViewModel.outputsForPointsChips,
        outputsForSharing = outputViewModel.outputsForSharing,
        outputsForUriByCategory = outputViewModel.outputsForUriByCategory,
        selectedUri = outputViewModel.selectedUri,
        start = conversionViewModel.start,
        stateLog = conversionViewModel.extendedStateLog,
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
        onReset = { conversionViewModel.reset() },
        onRetry = { conversionViewModel.retry() },
        onSelectUri = { outputViewModel.setSelectedUri(it) },
        onSetSource = { conversionViewModel.setSource(it) },
        onSubmit = { conversionViewModel.start(false) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    currentState: ConversionState,
    actionDetail: StateFlow<ActionDetail?>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: StateFlow<BillingStatus>,
    changelogShown: StateFlow<Boolean>,
    coordinateConverter: CoordinateConverter,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    inputRepository: InputRepository,
    linkMessage: StateFlow<Message?>,
    outputsForAppsByCategory: StateFlow<OutputDetailsForAppsByCategory>,
    outputsForLinks: StateFlow<List<OutputDetailsForLink>>,
    outputsForPoint: StateFlow<List<OutputDetail<PointOutput>>>,
    outputsForPointChips: StateFlow<List<OutputDetail<PointOutput>>>,
    outputsForPoints: StateFlow<List<OutputDetail<PointsOutput>>>,
    outputsForPointsChips: StateFlow<List<OutputDetail<PointsOutput>>>,
    outputsForSharing: StateFlow<OutputDetailsForSharing?>,
    outputsForUriByCategory: StateFlow<OutputDetailsForUriByCategory>,
    selectedUri: StateFlow<String?>,
    source: StateFlow<String>,
    sourceComesFromIntent: StateFlow<Boolean>,
    start: StateFlow<ComparableTimeMark?>,
    stateLog: StateFlow<List<ExtendedConversionStateLogItem>>,
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
    onSelectUri: (String?) -> Unit,
    onSetSource: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val resources = LocalResources.current

    val linkMessage by linkMessage.collectAsStateWithLifecycle()
    val userPreferenceMessage by userPreferenceMessage.collectAsStateWithLifecycle()

    var errorMessageResId by retain { mutableStateOf<Int?>(null) }
    var logExpanded by retain { mutableStateOf(false) }
    var selectedPointIndex by retain { mutableStateOf<Int?>(null) }
    val selectedUri by selectedUri.collectAsStateWithLifecycle()
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

    Scaffold(
        snackbarHost = {
            MessageSnackbarHost(snackbarHostState)
        },
    ) {
        MainScaffold(
            actions = {
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
            },
            topContent = {
                item {
                    Column {
                        MainSourceBar(
                            currentState = currentState,
                            errorMessageResId = errorMessageResId,
                            logExpanded = logExpanded,
                            source = source,
                            start = start,
                            stateLog = stateLog,
                            onSelectUri = onSelectUri,
                            onSetLogExpanded = { logExpanded = it },
                            onSetErrorMessageResId = { errorMessageResId = it },
                            onSetSource = onSetSource,
                            onSubmit = onSubmit,
                        )
                        ConversionStateLogList(
                            expanded = logExpanded,
                            stateLog = stateLog,
                            onUriClick = onSelectUri,
                        )
                    }
                }

                item {
                    if (currentState is Initial) {
                        MainSubmit(
                            source = source,
                            onSetErrorMessageResId = { errorMessageResId = it },
                            onSubmit = onSubmit,
                        )
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = mainContainerColor(currentState),
                            ),
                        ) {
                            when (currentState) {
                                is ConversionState.HasError ->
                                    ResultError(
                                        currentState = currentState,
                                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                                        onRetry = onRetry,
                                    )

                                is ConversionState.HasResult ->
                                    ResultCoordinates(
                                        points = currentState.points,
                                        coordinateConverter = coordinateConverter,
                                        outputsForPointChips = outputsForPointChips,
                                        outputsForPointsChips = outputsForPointsChips,
                                        userPreferencesValues = userPreferencesValues,
                                        onExecute = onExecute,
                                        onNavigateToFaqScreen = onNavigateToFaqScreen,
                                        onSelect = { index ->
                                            onCancel()
                                            selectedPointIndex = index
                                        },
                                    ) { paddingValues ->
                                        HelpShareSourceMessage(
                                            dismissedHelpMessages = dismissedHelpMessages,
                                            outputsForAppsByCategory = outputsForAppsByCategory,
                                            sourceComesFromIntent = sourceComesFromIntent,
                                            modifier = Modifier.padding(paddingValues),
                                            onDismissHelpMessage = onDismissHelpMessage,
                                            onExecute = onExecute,
                                        )
                                    }

                                is ConversionState.HasDescription ->
                                    currentState.getLoadingIndicatorTitle(resources)?.let { title ->
                                        MainLoadingIndicator(
                                            currentState = currentState,
                                            title = title,
                                            onCancel = onCancel,
                                        )
                                    }
                            }
                        }
                    }
                }

                if (currentState is PermissionGrantedWebViewInput) {
                    item {
                        MainWebView(
                            matchedInput = currentState.matchedInput,
                            pendingData = currentState.pendingData,
                        )
                    }
                }
            },
            bottomContent = {
                when (currentState) {
                    is Initial ->
                        item {
                            MainHelp(
                                inputRepository = inputRepository,
                                onNavigateToFaqScreen = onNavigateToFaqScreen,
                                onNavigateToInputsScreen = onNavigateToInputsScreen,
                                onSetErrorMessageResId = { errorMessageResId = it },
                                onSetSource = onSetSource,
                            ) { paddingValues ->
                                HelpWelcomeMessage(
                                    dismissedHelpMessages = dismissedHelpMessages,
                                    modifier = Modifier.padding(paddingValues),
                                    onDismissHelpMessage = onDismissHelpMessage,
                                )
                            }
                        }

                    is ConversionState.HasResult ->
                        item {
                            ResultApps(
                                outputsForAppsByCategory = outputsForAppsByCategory,
                                outputsForLinks = outputsForLinks,
                                outputsForSharing = outputsForSharing,
                                points = currentState.points,
                                source = source,
                                modifier = Modifier
                                    .padding(horizontal = spacing.windowPadding)
                                    .padding(top = spacing.tiny),
                                onDisableLinkGroup = onDisableLinkGroup,
                                onExecute = onExecute,
                                onHideApp = onHideApp,
                                onNavigateToLinkScreen = onNavigateToLinkScreen,
                            ) { paddingValues ->
                                HelpOpenByDefaultMessage(
                                    dismissedHelpMessages = dismissedHelpMessages,
                                    sourceComesFromIntent = sourceComesFromIntent,
                                    modifier = Modifier.padding(paddingValues),
                                    onDismissHelpMessage = onDismissHelpMessage,
                                    onNavigateToFaqScreen = onNavigateToFaqScreen,
                                )
                            }
                        }
                }
            },
            mainExpandedHeight = if (currentState is Initial) {
                spacing.largeTopAppBarExpandedHeight + spacing.medium
            } else {
                spacing.largeTopAppBarExpandedHeight
            },
            mainTitle = if (currentState is Initial) {
                {
                    val billingStatus by billingStatus.collectAsStateWithLifecycle()
                    MainHeadline(
                        appNameResId = if (billingStatus is BillingStatus.Purchased) {
                            billingAppNameResId
                        } else {
                            R.string.app_name
                        },
                        modifier = Modifier.offset(x = -(12).dp),
                    )
                }
            } else {
                null
            },
            supportingTitle = if (currentState is ConversionState.HasResult) {
                {
                    ResultTitle(
                        actionDetail = actionDetail,
                        billingFeatures = billingFeatures,
                        billingStatus = billingStatus,
                        onCancel = onCancel,
                        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                    )
                }
            } else {
                null
            },
            onBack = if (currentState !is Initial) {
                onReset
            } else {
                null
            },
        )
    }

    selectedUri?.let { uriString ->
        ConversionUriSheet(
            outputsForUriByCategory = outputsForUriByCategory,
            uriString = uriString,
            onDismissRequest = { onSelectUri(null) },
            onExecute = onExecute,
        )
    }

    if (currentState is ConversionState.HasResult) {
        selectedPointIndex?.let { index ->
            ResultSheet(
                points = currentState.points,
                selectedPointIndex = index,
                outputsForPoint = outputsForPoint,
                outputsForPoints = outputsForPoints,
                onExecute = onExecute,
                onSelectPointIndex = { selectedPointIndex = it },
            )
        }
    }

    when (currentState) {
        is PermissionRequested ->
            PermissionDialog(
                title = stringResource(
                    R.string.conversion_permission,
                    currentState.matchedInput.input.group.getName(resources),
                ),
                confirmText = stringResource(R.string.conversion_permission_common_grant),
                dismissText = stringResource(R.string.conversion_permission_common_deny),
                onConfirmation = onGrant,
                onDismissRequest = onDeny,
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareConnectionPermissionDialog"),
            ) {
                val appName = stringResource(R.string.app_name)
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
                title = stringResource(R.string.conversion_succeeded_location_rationale_dialog_title),
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
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = Initial,
            actionDetail = MutableStateFlow(null),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
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
            onSelectUri = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = Initial,
            actionDetail = MutableStateFlow(null),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
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
            onSelectUri = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = Initial,
            actionDetail = MutableStateFlow(null),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
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
            onSelectUri = {},
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
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = Initial,
            actionDetail = MutableStateFlow(null),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
            changelogShown = MutableStateFlow(false),
            coordinateConverter = coordinateConverter,
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(""),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(emptyList()),
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
            onSelectUri = {},
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
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val appDetails = getFakeAppDetails(context)
        MainScreen(
            currentState = ActionCompleted(
                source = source,
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
            actionDetail = MutableStateFlow(null),
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
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputDetailsForAppsByCategory(appDetails)
            ),
            outputsForLinks = MutableStateFlow(
                outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
            ),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(
                outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
            ),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(
                outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
            ),
            outputsForSharing = MutableStateFlow(
                outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
            ),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val appDetails = getFakeAppDetails(context)
        MainScreen(
            currentState = ActionCompleted(
                source = source,
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
            actionDetail = MutableStateFlow(null),
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
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputDetailsForAppsByCategory(appDetails)
            ),
            outputsForLinks = MutableStateFlow(
                outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
            ),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(
                outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
            ),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(
                outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
            ),
            outputsForSharing = MutableStateFlow(
                outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
            ),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val appDetails = getFakeAppDetails(context)
        MainScreen(
            currentState = ActionCompleted(
                source = source,
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = "Wikimedia Foundation, Inc.",
                    ),
                ),
                actionResult = ActionResult.SUCCEEDED,
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputDetailsForAppsByCategory(appDetails)
            ),
            outputsForLinks = MutableStateFlow(
                outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
            ),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(
                outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
            ),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(
                outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
            ),
            outputsForSharing = MutableStateFlow(
                outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
            ),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val appDetails = getFakeAppDetails(context)
        MainScreen(
            currentState = ActionCompleted(
                source = source,
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
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputDetailsForAppsByCategory(appDetails)
            ),
            outputsForLinks = MutableStateFlow(
                outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
            ),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(
                outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
            ),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(
                outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
            ),
            outputsForSharing = MutableStateFlow(
                outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
            ),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_reason_no_points),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://share.google/diIxnYa8dIA6dZfpy"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://share.google/diIxnYa8dIA6dZfpy"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = ConversionFailed(
                source = source,
                message = stringResource(R.string.conversion_failed_unsupported_source_google_search),
                warning = true,
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = PermissionGrantedBasicInput(
                source = source,
                matchedInput = MatchedInput(
                    FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
                ),
                permission = Permission.ALWAYS,
                results = emptyMap(),
                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = PermissionGrantedWebViewInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        MainScreen(
            currentState = PermissionGrantedWebViewInput(
                source = source,
                matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
                permission = Permission.ALWAYS,
                results = emptyMap(),
            ),
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val currentState = PermissionGrantedWebViewInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        )
        MainScreen(
            currentState = currentState,
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
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
        val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        val timeSource = TestTimeSource()
        val currentState = ConversionSucceeded(
            source = source,
            points = persistentListOf(),
        )
        MainScreen(
            currentState = currentState,
            actionDetail = MutableStateFlow(null),
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
            inputRepository = FakeInputRepository,
            linkMessage = MutableStateFlow(null),
            outputsForAppsByCategory = MutableStateFlow(
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
            ),
            outputsForLinks = MutableStateFlow(emptyList()),
            outputsForPoint = MutableStateFlow(emptyList()),
            outputsForPointChips = MutableStateFlow(emptyList()),
            outputsForPoints = MutableStateFlow(emptyList()),
            outputsForPointsChips = MutableStateFlow(emptyList()),
            outputsForSharing = MutableStateFlow(null),
            outputsForUriByCategory = MutableStateFlow(
                OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList())
            ),
            selectedUri = MutableStateFlow(null),
            source = MutableStateFlow(source),
            start = MutableStateFlow(timeSource.markNow()),
            stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
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
            onSelectUri = {},
            onSetSource = {},
            onSubmit = {},
        )
    }
}
