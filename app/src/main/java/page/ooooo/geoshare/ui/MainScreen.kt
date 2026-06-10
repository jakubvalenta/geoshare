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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.FileActionReady
import page.ooooo.geoshare.lib.conversion.FileUriRequested
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleRequested
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.PermissionGrantedWebViewInput
import page.ooooo.geoshare.lib.conversion.PermissionRequested
import page.ooooo.geoshare.lib.conversion.State
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
import page.ooooo.geoshare.ui.components.MainForm
import page.ooooo.geoshare.ui.components.MainHeadline
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultApps
import page.ooooo.geoshare.ui.components.ResultCoordinates
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.ResultSuccessSheet
import page.ooooo.geoshare.ui.components.ResultTitle
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
import page.ooooo.geoshare.ui.components.StyledSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.checkeredBackground
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.math.floor

@Composable
fun MainScreen(
    onFinish: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    billingViewModel: BillingViewModel,
    conversionViewModel: ConversionViewModel,
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

    val appDetails by outputViewModel.appDetails.collectAsStateWithLifecycle()
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val billingFeatures = billingViewModel.billingFeatures
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()
    val changelogShown by inputViewModel.changelogShown.collectAsStateWithLifecycle()
    val linkMessage by linkViewModel.message.collectAsStateWithLifecycle()
    val outputsForApps by outputViewModel.outputsForApps.collectAsStateWithLifecycle()
    val outputsForLinks by outputViewModel.outputsForLinks.collectAsStateWithLifecycle()
    val outputsForPoint by outputViewModel.outputsForPoint.collectAsStateWithLifecycle()
    val outputsForPointChips by outputViewModel.outputsForPointChips.collectAsStateWithLifecycle()
    val outputsForPoints by outputViewModel.outputsForPoints.collectAsStateWithLifecycle()
    val outputsForPointsChips by outputViewModel.outputsForPointsChips.collectAsStateWithLifecycle()
    val outputsForSharing by outputViewModel.outputsForSharing.collectAsStateWithLifecycle()
    val userPreferencesMessage by userPreferenceViewModel.message.collectAsStateWithLifecycle()
    val userPreferencesValues by userPreferenceViewModel.values.collectAsStateWithLifecycle()

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
                    val actionContext = ActionContext(
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                    )
                    val actionResult = currentState.action.execute(actionContext)
                    if (actionResult == ActionResult.SucceededAndFinish) {
                        onFinish()
                    }
                    conversionViewModel.finishBasicAction(actionResult)
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
                    val actionContext = ActionContext(
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                    )
                    val actionResult = currentState.action.execute(currentState.uri, actionContext)
                    if (actionResult == ActionResult.SucceededAndFinish) {
                        onFinish()
                    }
                    conversionViewModel.finishFileAction(actionResult)
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
                    val actionContext = ActionContext(
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                    )
                    val actionResult = currentState.action.execute(currentState.location, actionContext)
                    if (actionResult == ActionResult.SucceededAndFinish) {
                        onFinish()
                    }
                    conversionViewModel.finishLocationAction(actionResult)
                }
            }
        }
    }

    // Loading indicator

    var largeLoadingIndicator by remember { mutableStateOf<LoadingIndicator.Large?>(null) }

    LaunchedEffect(currentState) {
        // Wait for 200ms and then show or hide loading indicator. This way we show the loading indicator only if a
        // state lasts longer than 200ms and hide it only if another loading indicator doesn't appear within 200ms.
        delay(200L)
        largeLoadingIndicator = (currentState as? ConversionState.HasLargeLoadingIndicator)?.getLoadingIndicator()
    }

    MainScreen(
        currentState = currentState,
        appDetails = appDetails,
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingStatus = billingStatus,
        changelogShown = changelogShown,
        coordinateConverter = outputViewModel.coordinateConverter,
        coordinateFormat = userPreferencesValues.coordinateFormat,
        inputRepository = inputViewModel.inputRepository,
        largeLoadingIndicator = largeLoadingIndicator,
        linkMessage = linkMessage,
        outputsForApps = outputsForApps,
        outputsForLinks = outputsForLinks,
        outputsForPoint = outputsForPoint,
        outputsForPointChips = outputsForPointChips,
        outputsForPoints = outputsForPoints,
        outputsForPointsChips = outputsForPointsChips,
        outputsForSharing = outputsForSharing,
        source = conversionViewModel.source,
        userPreferenceMessage = userPreferencesMessage,
        onCancel = {
            locationJob?.cancel()
            conversionViewModel.cancel()
        },
        onDeny = { doNotAsk -> conversionViewModel.deny(doNotAsk) },
        onDisableLinkGroup = { group -> linkViewModel.disableGroup(resources, group) },
        onDismissLinkMessage = { linkViewModel.dismissMessage() },
        onDismissUserPreferenceMessage = { userPreferenceViewModel.dismissMessage() },
        onGrant = { doNotAsk -> conversionViewModel.grant(doNotAsk) },
        onExecute = { action ->
            conversionViewModel.cancel()
            conversionViewModel.startAction(action)
        },
        onHideApp = { packageName -> userPreferenceViewModel.hideApp(resources, packageName) },
        onNavigateToAboutScreen = {
            conversionViewModel.cancel()
            onNavigateToAboutScreen()
        },
        onNavigateToBillingScreen = {
            conversionViewModel.cancel()
            onNavigateToBillingScreen()
        },
        onNavigateToFaqScreen = {
            conversionViewModel.cancel()
            onNavigateToFaqScreen()
        },
        onNavigateToInputsScreen = {
            conversionViewModel.cancel()
            onNavigateToInputsScreen()
        },
        onNavigateToIntroScreen = {
            conversionViewModel.cancel()
            onNavigateToIntroScreen()
        },
        onNavigateToLinkScreen = {
            conversionViewModel.cancel()
            onNavigateToLinkScreen()
        },
        onNavigateToUserPreferencesAutomationScreen = {
            conversionViewModel.cancel()
            onNavigateToUserPreferencesAutomationScreen()
        },
        onNavigateToUserPreferencesScreen = {
            conversionViewModel.cancel()
            onNavigateToUserPreferencesScreen()
        },
        onReset = {
            conversionViewModel.cancel()
            conversionViewModel.reset()
        },
        onRetry = { conversionViewModel.retry() },
        onStart = { conversionViewModel.start() },
        onUpdateInput = { conversionViewModel.source = it },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    currentState: State,
    appDetails: AppDetails,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    changelogShown: Boolean,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: CoordinateFormat,
    inputRepository: InputRepository,
    largeLoadingIndicator: LoadingIndicator.Large?,
    linkMessage: Message?,
    outputsForApps: Map<String, List<Output>>,
    outputsForLinks: Map<String?, List<Output>>,
    outputsForPoint: List<PointOutput>,
    outputsForPointChips: List<PointOutput>,
    outputsForPoints: List<PointsOutput>,
    outputsForPointsChips: List<PointsOutput>,
    outputsForSharing: List<Output>,
    source: String,
    userPreferenceMessage: Message?,
    onCancel: () -> Unit,
    onDeny: (Boolean) -> Unit,
    onDisableLinkGroup: (String?) -> Unit,
    onDismissLinkMessage: () -> Unit,
    onDismissUserPreferenceMessage: () -> Unit,
    onGrant: (Boolean) -> Unit,
    onHideApp: (String) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onReset: () -> Unit,
    onRetry: () -> Unit,
    onExecute: (Action<*>) -> Unit,
    onStart: () -> Unit,
    onUpdateInput: (String) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val coroutineScope = rememberCoroutineScope()
    val mainContainerColor = when (currentState) {
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicator != null -> MaterialTheme.colorScheme.surfaceContainer
        is ConversionState.HasError -> MaterialTheme.colorScheme.errorContainer
        is ConversionState.HasResult -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val mainContentColor = contentColorFor(mainContainerColor)
    val spacing = LocalSpacing.current

    val (errorMessageResId, setErrorMessageResId) = retain { mutableStateOf<Int?>(null) }
    val (selectedPointIndex, setSelectedPointIndex) = retain { mutableStateOf<Int?>(null) }
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(currentState !is Initial) {
        onReset()
    }

    // Message

    LaunchedEffect(linkMessage) {
        if (linkMessage != null) {
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(linkMessage))
            onDismissLinkMessage()
        }
    }

    LaunchedEffect(userPreferenceMessage) {
        if (userPreferenceMessage != null) {
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(userPreferenceMessage))
            onDismissUserPreferenceMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            MessageSnackbarHost(snackbarHostState)
        },
    ) {
        StyledSupportingPaneScaffold(
            mainPane = { innerPadding, wide ->
                LargeTopAppBarPane(
                    title = {
                        MainTitle(
                            currentState = currentState,
                            billingAppNameResId = billingAppNameResId,
                            billingStatus = billingStatus,
                            largeLoadingIndicator = largeLoadingIndicator,
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
                                onNavigateToIntroScreen = onNavigateToIntroScreen,
                                onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                            )
                        }
                    },
                    expandedHeight = if (currentState is Initial) {
                        TopAppBarDefaults.LargeAppBarExpandedHeight + spacing.headlineTopAdaptive
                    } else {
                        TopAppBarDefaults.LargeAppBarExpandedHeight
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .testTag("geoShareMainPane"),
                    ) {
                        if (!wide) {
                            when (currentState) {
                                is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicator != null ->
                                    MainLoadingIndicator(
                                        loadingIndicator = largeLoadingIndicator,
                                        onCancel = onCancel,
                                    )

                                is ConversionState.HasError ->
                                    ResultError(
                                        source = currentState.source,
                                        message = currentState.message,
                                        details = currentState.details,
                                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                                        onRetry = onRetry,
                                    )

                                is ConversionState.HasResult -> {
                                    ResultCoordinates(
                                        points = currentState.points,
                                        appDetails = appDetails,
                                        coordinateConverter = coordinateConverter,
                                        coordinateFormat = coordinateFormat,
                                        outputsForPointChips = outputsForPointChips,
                                        outputsForPointsChips = outputsForPointsChips,
                                        onExecute = onExecute,
                                        onSelect = { index ->
                                            onCancel()
                                            setSelectedPointIndex(index)
                                        },
                                    )
                                    Column(
                                        // This column must not have weight(1f), otherwise the last row of app icons gets shrunk
                                        Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                                            ResultTitle(
                                                currentState = currentState,
                                                appDetails = appDetails,
                                                billingFeatures = billingFeatures,
                                                billingStatus = billingStatus,
                                                modifier = Modifier
                                                    .padding(horizontal = spacing.windowPadding)
                                                    .padding(top = spacing.largeAdaptive),
                                                onCancel = onCancel,
                                                onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
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
                                        }
                                    }
                                }

                                is Initial -> {
                                    MainForm(
                                        source = source,
                                        errorMessageResId = errorMessageResId,
                                        inputRepository = inputRepository,
                                        onSetErrorMessageResId = setErrorMessageResId,
                                        onSubmit = onStart,
                                        onUpdateInput = onUpdateInput,
                                    )
                                }
                            }
                        } else if (currentState is Initial) {
                            MainForm(
                                source = source,
                                errorMessageResId = errorMessageResId,
                                inputRepository = inputRepository,
                                onSetErrorMessageResId = setErrorMessageResId,
                                onSubmit = onStart,
                                onUpdateInput = onUpdateInput,
                            )
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = mainContainerColor,
                                    contentColor = mainContentColor,
                                ),
                            ) {
                                Spacer(Modifier.height(spacing.mediumAdaptive))

                                when (currentState) {
                                    is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicator != null ->
                                        MainLoadingIndicator(
                                            loadingIndicator = largeLoadingIndicator,
                                            onCancel = onCancel,
                                        )

                                    is ConversionState.HasError ->
                                        ResultError(
                                            source = currentState.source,
                                            message = currentState.message,
                                            details = currentState.details,
                                            onNavigateToInputsScreen = onNavigateToInputsScreen,
                                            onRetry = onRetry,
                                        )

                                    is ConversionState.HasResult -> {
                                        ResultCoordinates(
                                            points = currentState.points,
                                            appDetails = appDetails,
                                            coordinateConverter = coordinateConverter,
                                            coordinateFormat = coordinateFormat,
                                            outputsForPointChips = outputsForPointChips,
                                            outputsForPointsChips = outputsForPointsChips,
                                            onExecute = onExecute,
                                            onSelect = { index ->
                                                onCancel()
                                                setSelectedPointIndex(index)
                                            },
                                        )
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
                                        onExtractionSettle = { currentState.onExtractionSettle(it) },
                                    )
                                }
                            }
                        }
                    }

                    if (currentState is ConversionState.HasSource) {
                        MainCopySourceButton(
                            source = currentState.source,
                            innerPadding = innerPadding,
                            containerColor = if (!wide) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color.Transparent
                            },
                        )
                    } else {
                        Spacer(Modifier.padding(innerPadding))
                    }
                }
            },
            supportingPane = { wide ->
                LargeTopAppBarPane(
                    title = if (currentState is ConversionState.HasResult) {
                        {
                            ResultTitle(
                                currentState = currentState,
                                appDetails = appDetails,
                                billingFeatures = billingFeatures,
                                billingStatus = billingStatus,
                                onCancel = onCancel,
                                onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
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
                                onNavigateToIntroScreen = onNavigateToIntroScreen,
                                onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                            )
                        }
                    },
                ) {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .testTag("geoShareMainSupportingPane")
                    ) {
                        when (currentState) {
                            is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicator != null -> {}

                            is ConversionState.HasResult ->
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
                    }
                }
            },
            colors = StyledPaneScaffoldDefaults.colors(
                mainContainerColor = mainContainerColor,
                mainContentColor = mainContentColor,
                wideMainContainerColor = Color.Transparent,
                wideMainContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }

    if (currentState is ConversionState.HasResult && selectedPointIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { setSelectedPointIndex(null) },
            modifier = Modifier
                // Set and consume insets to prevent unclickable items when the sheet is expanded (probably a bug in
                // Compose Material 3)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            sheetState = sheetState,
        ) {
            ResultSuccessSheet(
                points = currentState.points,
                selectedPointIndex = selectedPointIndex,
                appDetails = appDetails,
                outputsForPoint = outputsForPoint,
                outputsForPoints = outputsForPoints,
                onExecute = onExecute,
                onHide = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setSelectedPointIndex(null)
                        }
                    }
                },
            )
        }
    }

    when (currentState) {
        is PermissionRequested -> {
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
        }

        is LocationRationaleShown -> {
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
}

@Composable
private fun MainTitle(
    currentState: State,
    billingAppNameResId: Int,
    billingStatus: BillingStatus,
    largeLoadingIndicator: LoadingIndicator.Large?,
) {
    when (currentState) {
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicator != null ->
            currentState.getLoadingIndicator()?.title?.let { title ->
                Text(title)
            }

        is ConversionState.HasError ->
            Text(stringResource(R.string.conversion_error_title))

        is ConversionState.HasResult ->
            Text(
                currentState.points.lastOrNull()?.cleanName?.takeIf { it.isNotEmpty() }
                    ?: if (currentState.points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_last)
                    } else {
                        stringResource(R.string.conversion_succeeded_title)
                    },
                Modifier.testTag("geoShareResultSuccessLastPointName"),
            )

        is Initial ->
            MainHeadline(billingAppNameResId, billingStatus, Modifier.offset(x = -(12).dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MainLoadingIndicator(
    loadingIndicator: LoadingIndicator.Large,
    onCancel: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding)
    ) {
        LoadingIndicator(
            Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = spacing.smallAdaptive),
            color = MaterialTheme.colorScheme.tertiary,
        )
        Button(
            onCancel,
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = spacing.smallAdaptive, bottom = spacing.mediumAdaptive),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(stringResource(R.string.conversion_loading_indicator_cancel))
        }
        loadingIndicator.description?.let { description ->
            Text(
                description,
                Modifier.padding(bottom = spacing.mediumAdaptive),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MainWebView(
    matchedInput: MatchedInput<WebViewInput>,
    onExtractionSettle: (data: String) -> Unit,
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
            unsafeExtractionJavascript = matchedInput.input.unsafeExtractionJavascript,
            onExtractionSettle = onExtractionSettle,
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

@Composable
private fun MainCopySourceButton(
    source: String,
    containerColor: Color,
    innerPadding: PaddingValues,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
    ) {
        TextButton(
            {
                coroutineScope.launch {
                    AndroidTools.copyToClipboard(clipboard, source)
                }
            },
            Modifier
                .padding(start = 4.dp)
                .padding(top = spacing.tinyAdaptive),
        ) {
            Text(stringResource(R.string.conversion_succeeded_skip))
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
            currentState = Initial(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = Initial(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = Initial(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = ActionFinished(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                actionResult = ActionResult.Succeeded,
            ),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.COMAPS_FDROID to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.GMAPS_WV to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.HERE_WEGO to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.MAPY_COM to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.ORGANIC_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.OSMAND_PLUS to setOf(DataType.GPX_DATA),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = ActionFinished(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                actionResult = ActionResult.Succeeded,
            ),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.COMAPS_FDROID to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.GMAPS_WV to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.HERE_WEGO to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.MAPY_COM to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.ORGANIC_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.OSMAND_PLUS to setOf(DataType.GPX_DATA),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = ActionFinished(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                actionResult = ActionResult.Succeeded,
            ),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.COMAPS_FDROID to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.GMAPS_WV to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.HERE_WEGO to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.MAPY_COM to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.ORGANIC_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.OSMAND_PLUS to setOf(DataType.GPX_DATA),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            currentState = ActionFinished(
                source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                actionResult = ActionResult.Succeeded,
            ),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.COMAPS_FDROID to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.GMAPS_WV to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.HERE_WEGO to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.MAPY_COM to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.ORGANIC_MAPS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.OSMAND_PLUS to setOf(DataType.GPX_DATA),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
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
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedBasicInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            ),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedBasicInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            ),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingIndicatorPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedBasicInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            ),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedWebViewInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedWebViewInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletWebViewPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        val currentState = PermissionGrantedWebViewInput(
            stateContext = ConversionStateContext(
                linkRepository = FakeLinkRepository(),
                outputRepository = outputRepository,
                resources = resources,
                userPreferencesRepository = FakeUserPreferencesRepository(),
                billing = BillingImpl(LocalContext.current),
            ),
            source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            matchedInput = MatchedInput(FakeInputRepository.debugWebViewInput, "https://www.example.com/"),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        )
        MainScreen(
            currentState = currentState,
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = currentState.getLoadingIndicator(),
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ConversionSucceeded(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                persistentListOf(),
            ),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
                token = "test_purchased",
            ),
            changelogShown = true,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputRepository = FakeInputRepository,
            largeLoadingIndicator = null,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            source = "",
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onExecute = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinkScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onRetry = {},
            onStart = {},
            onUpdateInput = {},
        )
    }
}
