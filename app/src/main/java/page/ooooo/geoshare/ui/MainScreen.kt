package page.ooooo.geoshare.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.Uri
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
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ContactActionReady
import page.ooooo.geoshare.lib.conversion.ContactRequested
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.FileActionReady
import page.ooooo.geoshare.lib.conversion.FileUriRequested
import page.ooooo.geoshare.lib.conversion.GrantedParseWebPermission
import page.ooooo.geoshare.lib.conversion.GrantedUnshortenPermission
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleRequested
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.conversion.RequestedParseWebPermission
import page.ooooo.geoshare.lib.conversion.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.lib.extensions.truncateMiddle
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionContext
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.NoopAction
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.components.BasicSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.ConfirmationDialog
import page.ooooo.geoshare.ui.components.ConversionWebView
import page.ooooo.geoshare.ui.components.Headline
import page.ooooo.geoshare.ui.components.MainForm
import page.ooooo.geoshare.ui.components.MainFormLinks
import page.ooooo.geoshare.ui.components.MainMenu
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.PermissionDialog
import page.ooooo.geoshare.ui.components.ResultError
import page.ooooo.geoshare.ui.components.ResultSuccessApps
import page.ooooo.geoshare.ui.components.ResultSuccessCoordinates
import page.ooooo.geoshare.ui.components.ResultSuccessMessage
import page.ooooo.geoshare.ui.components.ResultSuccessSheet
import page.ooooo.geoshare.ui.components.checkeredBackground
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.math.floor
import kotlin.time.Duration.Companion.seconds

@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToLinksScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    billingViewModel: BillingViewModel,
    conversionViewModel: ConversionViewModel,
    inputViewModel: InputViewModel = hiltViewModel(),
    outputViewModel: OutputViewModel = hiltViewModel(),
    linkViewModel: LinkViewModel = hiltViewModel(),
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
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
    val userPreferencesMessage by userPreferencesViewModel.message.collectAsStateWithLifecycle()
    val userPreferencesValues by userPreferencesViewModel.values.collectAsStateWithLifecycle()

    // Action

    var locationJob by remember { mutableStateOf<Job?>(null) }
    val locationPermissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            conversionViewModel.receiveLocationPermission()
        }
    val pickContactLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
                conversionViewModel.receiveContactUri(uri)
            } ?: conversionViewModel.cancelContactUriRequest()
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
                    val success = currentState.action.execute(actionContext)
                    conversionViewModel.finishBasicAction(success)
                }

                // Contact action

                is ContactRequested -> {
                    try {
                        pickContactLauncher.launch(
                            Intent(Intent.ACTION_PICK).apply {
                                type = ContactsContract.Contacts.CONTENT_TYPE
                            }
                        )
                    } catch (_: ActivityNotFoundException) {
                        conversionViewModel.cancelContactUriRequest()
                    }
                }

                is ContactActionReady -> {
                    val actionContext = ActionContext(
                        context = context,
                        clipboard = clipboard,
                        resources = resources,
                    )
                    val success = currentState.action.execute(currentState.contactUri, actionContext)
                    conversionViewModel.finishContactAction(success)
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
                    val success = currentState.action.execute(currentState.uri, actionContext)
                    conversionViewModel.finishFileAction(success)
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
                    val success = currentState.action.execute(currentState.location, actionContext)
                    conversionViewModel.finishLocationAction(success)
                }
            }
        }
    }

    // Loading indicator

    var largeLoadingIndicatorVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentState) {
        if (currentState is ConversionState.HasLargeLoadingIndicator) {
            // Show loading indicator if the state lasts longer than 200ms.
            delay(200L)
            largeLoadingIndicatorVisible = true
        } else {
            // Hide loading indicator if another loading indicator is not shown within the next 200ms.
            delay(200L)
            largeLoadingIndicatorVisible = false
        }
    }

    MainScreen(
        currentState = currentState,
        allInputs = inputViewModel.allInputs,
        appDetails = appDetails,
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingStatus = billingStatus,
        changelogShown = changelogShown,
        coordinateConverter = outputViewModel.coordinateConverter,
        coordinateFormat = userPreferencesValues.coordinateFormat,
        inputUriString = conversionViewModel.inputUriString,
        largeLoadingIndicatorVisible = largeLoadingIndicatorVisible,
        linkMessage = linkMessage,
        outputsForApps = outputsForApps,
        outputsForLinks = outputsForLinks,
        outputsForPoint = outputsForPoint,
        outputsForPointChips = outputsForPointChips,
        outputsForPoints = outputsForPoints,
        outputsForPointsChips = outputsForPointsChips,
        outputsForSharing = outputsForSharing,
        userPreferenceMessage = userPreferencesMessage,
        onCancel = {
            locationJob?.cancel()
            conversionViewModel.cancel()
        },
        onDeny = { doNotAsk -> conversionViewModel.deny(doNotAsk) },
        onDisableLinkGroup = { group -> linkViewModel.disableGroup(resources, group) },
        onDismissLinkMessage = { linkViewModel.dismissMessage() },
        onDismissUserPreferenceMessage = { userPreferencesViewModel.dismissMessage() },
        onGrant = { doNotAsk -> conversionViewModel.grant(doNotAsk) },
        onHideApp = { packageName -> userPreferencesViewModel.hideApp(resources, packageName) },
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
        onNavigateToLinksScreen = {
            conversionViewModel.cancel()
            onNavigateToLinksScreen()
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
        onExecute = { action ->
            conversionViewModel.cancel()
            conversionViewModel.startAction(action)
        },
        onStart = { conversionViewModel.start() },
    ) { conversionViewModel.inputUriString = it }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    currentState: State,
    allInputs: List<Input>,
    appDetails: AppDetails,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    changelogShown: Boolean,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: CoordinateFormat,
    inputUriString: String,
    largeLoadingIndicatorVisible: Boolean,
    linkMessage: Message?,
    outputsForApps: Map<String, List<Output>>,
    outputsForLinks: Map<String?, List<Output>>,
    outputsForPoint: List<PointOutput>,
    outputsForPointChips: List<PointOutput>,
    outputsForPoints: List<PointsOutput>,
    outputsForPointsChips: List<PointsOutput>,
    outputsForSharing: List<Output>,
    userPreferenceMessage: Message?,
    onCancel: () -> Unit,
    onDeny: (doNotAsk: Boolean) -> Unit,
    onDisableLinkGroup: (group: String?) -> Unit,
    onDismissLinkMessage: () -> Unit,
    onDismissUserPreferenceMessage: () -> Unit,
    onGrant: (doNotAsk: Boolean) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToLinksScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onReset: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
    onStart: () -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val containerColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val (errorMessageResId, setErrorMessageResId) = retain { mutableStateOf<Int?>(null) }
    val (selectedPointIndex, setSelectedPointIndex) = retain { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState()
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
        BasicSupportingPaneScaffold(
            navigationIcon = {
                if (currentState !is Initial) {
                    IconButton(
                        onReset,
                        Modifier.testTag("geoShareMainBackButton"),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            stringResource(R.string.nav_back_content_description)
                        )
                    }
                }
            },
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
                    onNavigateToIntroScreen = onNavigateToIntroScreen,
                    onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
                )
            },
            mainPane = { innerPadding, wide ->
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .testTag("geoShareMainPane"),
                ) {
                    MainMainPane(
                        currentState = currentState,
                        appDetails = appDetails,
                        outputsForPointChips = outputsForPointChips,
                        outputsForPointsChips = outputsForPointsChips,
                        billingAppNameResId = billingAppNameResId,
                        billingStatus = billingStatus,
                        coordinateConverter = coordinateConverter,
                        coordinateFormat = coordinateFormat,
                        errorMessageResId = errorMessageResId,
                        inputUriString = inputUriString,
                        largeLoadingIndicatorVisible = largeLoadingIndicatorVisible,
                        onCancel = onCancel,
                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                        onExecute = onExecute,
                        onSelect = { index ->
                            onCancel()
                            setSelectedPointIndex(index)
                        },
                        onSetErrorMessageResId = setErrorMessageResId,
                        onStart = onStart,
                        onUpdateInput = onUpdateInput,
                    )
                    if (!wide) {
                        Column(
                            // This column must not have weight(1f), otherwise the last row of app icons gets shrunk
                            Modifier
                                .fillMaxWidth()
                                .background(containerColor)
                        ) {
                            CompositionLocalProvider(LocalContentColor provides contentColor) {
                                MainSupportingPane(
                                    allInputs = allInputs,
                                    appDetails = appDetails,
                                    billingFeatures = billingFeatures,
                                    billingStatus = billingStatus,
                                    currentState = currentState,
                                    largeLoadingIndicatorVisible = largeLoadingIndicatorVisible,
                                    outputsForApps = outputsForApps,
                                    outputsForLinks = outputsForLinks,
                                    outputsForSharing = outputsForSharing,
                                    onCancel = onCancel,
                                    onDisableLinkGroup = onDisableLinkGroup,
                                    onNavigateToInputsScreen = onNavigateToInputsScreen,
                                    onNavigateToIntroScreen = onNavigateToIntroScreen,
                                    onNavigateToLinksScreen = onNavigateToLinksScreen,
                                    onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                                    onExecute = onExecute,
                                    onHideApp = onHideApp,
                                    onSetErrorMessageResId = setErrorMessageResId,
                                    onUpdateInput = onUpdateInput,
                                    modifier = Modifier.padding(top = spacing.largeAdaptive),
                                )
                            }
                        }
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(containerColor)
                    ) {
                        MainBottomPane(currentState)
                    }
                }
                MainBottomBar(
                    currentState,
                    largeLoadingIndicatorVisible,
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    containerColor = containerColor,
                    contentColor = contentColor,
                )
            },
            supportingPane = {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .testTag("geoShareMainSupportingPane"),
                ) {
                    MainSupportingPane(
                        allInputs = allInputs,
                        appDetails = appDetails,
                        billingFeatures = billingFeatures,
                        billingStatus = billingStatus,
                        currentState = currentState,
                        largeLoadingIndicatorVisible = largeLoadingIndicatorVisible,
                        outputsForApps = outputsForApps,
                        outputsForLinks = outputsForLinks,
                        outputsForSharing = outputsForSharing,
                        onCancel = onCancel,
                        onDisableLinkGroup = onDisableLinkGroup,
                        onNavigateToInputsScreen = onNavigateToInputsScreen,
                        onNavigateToIntroScreen = onNavigateToIntroScreen,
                        onNavigateToLinksScreen = onNavigateToLinksScreen,
                        onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                        onExecute = onExecute,
                        onHideApp = onHideApp,
                        onSetErrorMessageResId = setErrorMessageResId,
                        onUpdateInput = onUpdateInput,
                        modifier = Modifier.padding(top = spacing.headlineTopAdaptive),
                    )
                }
            },
            mainContainerColor = when (currentState) {
                is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> MaterialTheme.colorScheme.surfaceContainer
                is ConversionState.HasError -> MaterialTheme.colorScheme.errorContainer
                is ConversionState.HasResult -> MaterialTheme.colorScheme.secondaryContainer
                else -> containerColor
            },
            mainContentColor = when (currentState) {
                is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> contentColor
                is ConversionState.HasError -> MaterialTheme.colorScheme.onErrorContainer
                is ConversionState.HasResult -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> contentColor
            },
            shouldAutoFocusCurrentDestination = false,
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
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> {}
        is RequestedUnshortenPermission -> {
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

        is RequestedParseHtmlPermission -> {
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

        is RequestedParseWebPermission -> {
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
private fun MainMainPane(
    currentState: State,
    appDetails: AppDetails,
    outputsForPointChips: List<PointOutput>,
    outputsForPointsChips: List<PointsOutput>,
    billingAppNameResId: Int,
    billingStatus: BillingStatus,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: CoordinateFormat,
    errorMessageResId: Int?,
    inputUriString: String,
    largeLoadingIndicatorVisible: Boolean,
    onCancel: () -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
    onSelect: (index: Int?) -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onStart: () -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    when (currentState) {
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> {
            MainLoadingIndicator(
                loadingIndicator = currentState.getLargeLoadingIndicator(),
                onCancel = onCancel,
            )
        }

        is ConversionState.HasError -> {
            ResultError(
                currentState.message,
                currentState.inputUriString,
                onNavigateToInputsScreen = onNavigateToInputsScreen,
                onRetry = {
                    onUpdateInput(currentState.inputUriString)
                    onStart()
                },
            )
        }

        is ConversionState.HasResult -> {
            ResultSuccessCoordinates(
                points = currentState.points,
                appDetails = appDetails,
                coordinateConverter = coordinateConverter,
                coordinateFormat = coordinateFormat,
                outputsForPointChips = outputsForPointChips,
                outputsForPointsChips = outputsForPointsChips,
                onExecute = onExecute,
                onSelect = onSelect,
            )
        }

        is Initial -> {
            MainForm(
                inputUriString = inputUriString,
                billingAppNameResId = billingAppNameResId,
                billingStatus = billingStatus,
                errorMessageResId = errorMessageResId,
                onSetErrorMessageResId = onSetErrorMessageResId,
                onSubmit = onStart,
                onUpdateInput = onUpdateInput,
            )
        }
    }
}

@Composable
private fun MainBottomPane(currentState: State) {
    if (currentState is GrantedParseWebPermission) {
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
                unsafeUrl = currentState.webUriString,
                onUrlChange = { currentState.onUrlChange(it) },
                extendWebSettings = { currentState.input.extendWebSettings(it) },
                shouldInterceptRequest = { currentState.input.shouldInterceptRequest(it) },
            )
        }
    }
}

@Composable
private fun MainSupportingPane(
    allInputs: List<Input>,
    appDetails: AppDetails,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    currentState: State,
    largeLoadingIndicatorVisible: Boolean,
    outputsForApps: Map<String, List<Output>>,
    outputsForLinks: Map<String?, List<Output>>,
    outputsForSharing: List<Output>,
    onCancel: () -> Unit,
    onDisableLinkGroup: (group: String?) -> Unit,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToLinksScreen: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (currentState) {
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> {}
        is ConversionState.HasResult -> {
            ResultSuccessMessage(
                currentState = currentState,
                appDetails = appDetails,
                billingFeatures = billingFeatures,
                billingStatus = billingStatus,
                modifier = modifier,
                onCancel = onCancel,
                onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
            )
            ResultSuccessApps(
                appDetails = appDetails,
                outputsForApps = outputsForApps,
                outputsForLinks = outputsForLinks,
                outputsForSharing = outputsForSharing,
                points = currentState.points,
                onDisableLinkGroup = onDisableLinkGroup,
                onExecute = onExecute,
                onHideApp = onHideApp,
                onNavigateToLinksScreen = onNavigateToLinksScreen,
            )
        }

        is Initial -> {
            MainFormLinks(
                allInputs = allInputs,
                modifier = modifier,
                onNavigateToInputsScreen = onNavigateToInputsScreen,
                onNavigateToIntroScreen = onNavigateToIntroScreen,
                onSetErrorMessageResId = onSetErrorMessageResId,
                onUpdateInput = onUpdateInput,
            )
        }
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
            .padding(horizontal = spacing.windowPadding),
    ) {
        Headline(loadingIndicator.title)
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
private fun MainBottomBar(
    currentState: State,
    largeLoadingIndicatorVisible: Boolean,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    when (currentState) {
        is ConversionState.HasLargeLoadingIndicator if largeLoadingIndicatorVisible -> {}
        is ConversionState.HasError -> MainSkipButton(
            currentState.inputUriString,
            containerColor,
            contentColor,
            modifier,
        )

        is ConversionState.HasResult -> MainSkipButton(
            currentState.inputUriString,
            containerColor,
            contentColor,
            modifier,
        )
    }
}

@Composable
private fun MainSkipButton(
    inputUriString: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .background(containerColor)
            .fillMaxWidth()
            .then(modifier),
    ) {
        TextButton(
            {
                coroutineScope.launch {
                    AndroidTools.copyToClipboard(clipboard, inputUriString)
                }
            },
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.tinyAdaptive),
            colors = ButtonDefaults.textButtonColors(
                contentColor = contentColor,
            )
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
            allInputs = emptyList(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
            allInputs = emptyList(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
            allInputs = emptyList(),
            appDetails = emptyMap(),
            billingAppNameResId = R.string.app_name,
            billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
            billingStatus = BillingStatus.NotPurchased(),
            changelogShown = false,
            coordinateConverter = coordinateConverter,
            coordinateFormat = CoordinateFormat.DEC,
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                action = NoopAction,
                isAutomation = false,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
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
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                action = NoopAction,
                isAutomation = false,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
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
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                action = NoopAction,
                isAutomation = false,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
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
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                action = NoopAction,
                isAutomation = false,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
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
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
    }
}

@Preview(showBackground = true, device = Devices.TABLET, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkTabletSucceededPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionFinished(
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.example),
                ),
                action = NoopAction,
                isAutomation = false,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                    .toAction(WGS84Point(source = Source.GENERATED)),
                isAutomation = true,
                delay = 3.seconds,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                    .toAction(WGS84Point(source = Source.GENERATED)),
                isAutomation = true,
                delay = 3.seconds,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationPreview() {
    AppTheme {
        val context = LocalContext.current
        val resources = LocalResources.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        MainScreen(
            currentState = ActionWaiting(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                    .toAction(WGS84Point(source = Source.GENERATED)),
                isAutomation = true,
                delay = 3.seconds,
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                emptySet(),
            ),
            outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
            outputsForPoint = emptyList(),
            outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
            outputsForPoints = emptyList(),
            outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
            outputsForSharing = outputRepository.getOutputsForSharing(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedParseWebPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                input = GoogleMapsInput,
                uri = Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                pointsFromUri = persistentListOf(),
                webUriString = "https://www.example.com/",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedParseWebPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                input = GoogleMapsInput,
                uri = Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                pointsFromUri = persistentListOf(),
                webUriString = "https://www.example.com/",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedParseWebPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                input = GoogleMapsInput,
                uri = Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                pointsFromUri = persistentListOf(),
                webUriString = "https://www.example.com/",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                message = stringResource(R.string.conversion_failed_parse_url_error),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                message = stringResource(R.string.conversion_failed_parse_url_error),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
                message = stringResource(R.string.conversion_failed_parse_url_error),
                inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = false,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedUnshortenPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    RecoverableNetworkException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedUnshortenPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    RecoverableNetworkException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
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
        MainScreen(
            currentState = GrantedUnshortenPermission(
                stateContext = ConversionStateContext(
                    linkRepository = FakeLinkRepository(),
                    outputRepository = outputRepository,
                    resources = resources,
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    billing = BillingImpl(LocalContext.current),
                ),
                "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                GoogleMapsInput,
                Uri.parse("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                retry = NetworkTools.Retry(
                    2,
                    RecoverableNetworkException(R.string.network_exception_connect_timeout, Exception()),
                )
            ),
            allInputs = emptyList(),
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
            inputUriString = "",
            largeLoadingIndicatorVisible = true,
            linkMessage = null,
            outputsForApps = emptyMap(),
            outputsForLinks = emptyMap(),
            outputsForPoint = emptyList(),
            outputsForPointChips = emptyList(),
            outputsForPoints = emptyList(),
            outputsForPointsChips = emptyList(),
            outputsForSharing = emptyList(),
            userPreferenceMessage = null,
            onCancel = {},
            onDisableLinkGroup = {},
            onDismissLinkMessage = {},
            onDismissUserPreferenceMessage = {},
            onDeny = {},
            onGrant = {},
            onHideApp = {},
            onNavigateToAboutScreen = {},
            onNavigateToBillingScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToInputsScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToLinksScreen = {},
            onNavigateToUserPreferencesAutomationScreen = {},
            onNavigateToUserPreferencesScreen = {},
            onReset = {},
            onExecute = {},
            onStart = {},
        ) {}
    }
}
