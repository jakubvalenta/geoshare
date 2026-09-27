package page.ooooo.geoshare.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.getUriString
import page.ooooo.geoshare.lib.extensions.zipWithNextLastNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.state.ActionAutomationFailed
import page.ooooo.geoshare.lib.state.ActionAutomationSucceeded
import page.ooooo.geoshare.lib.state.ActionCompleted
import page.ooooo.geoshare.lib.state.ActionFailed
import page.ooooo.geoshare.lib.state.ActionRan
import page.ooooo.geoshare.lib.state.ActionReady
import page.ooooo.geoshare.lib.state.ActionState
import page.ooooo.geoshare.lib.state.ActionStateContext
import page.ooooo.geoshare.lib.state.ActionSucceeded
import page.ooooo.geoshare.lib.state.ActionWaiting
import page.ooooo.geoshare.lib.state.AutomationReceived
import page.ooooo.geoshare.lib.state.AutomationRequested
import page.ooooo.geoshare.lib.state.BasicActionReady
import page.ooooo.geoshare.lib.state.ConversionFailed
import page.ooooo.geoshare.lib.state.ConversionState
import page.ooooo.geoshare.lib.state.ConversionStateContext
import page.ooooo.geoshare.lib.state.ExtendedStateLog
import page.ooooo.geoshare.lib.state.ExtendedStateLogItem
import page.ooooo.geoshare.lib.state.FileActionReady
import page.ooooo.geoshare.lib.state.FileUriRequested
import page.ooooo.geoshare.lib.state.LocationActionReady
import page.ooooo.geoshare.lib.state.LocationFindingFailed
import page.ooooo.geoshare.lib.state.LocationPermissionReceived
import page.ooooo.geoshare.lib.state.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.state.LocationRationaleShown
import page.ooooo.geoshare.lib.state.LocationReceived
import page.ooooo.geoshare.lib.state.SourceReceived
import page.ooooo.geoshare.lib.state.StateLog
import page.ooooo.geoshare.lib.state.append
import page.ooooo.geoshare.lib.state.transitionRecursively
import javax.inject.Inject
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

sealed interface ActionDetail

data class ActionWaitingDetail(
    private val appDetail: AppDetail?,
    val delay: Duration,
    private val output: Output.HasAutomationDelay,
) : ActionDetail {
    @Composable
    fun automationWaitingText(counterSec: Int): String = output.automationWaitingText(counterSec, appDetail)
}

data class ActionSucceededDetail(
    private val appDetail: AppDetail?,
    private val output: Output.HasSuccessText,
) : ActionDetail {
    @Composable
    fun successText(): String = output.successText(appDetail)
}

data class ActionFailedDetail(
    private val appDetail: AppDetail?,
    private val output: Output.HasErrorText,
) : ActionDetail {
    @Composable
    fun errorText(): String = output.errorText(appDetail)
}

data class ActionAutomationSucceededDetail(
    private val appDetail: AppDetail?,
    private val output: Output.HasAutomationSuccessText,
) : ActionDetail {
    @Composable
    fun automationSuccessText(): String = output.automationSuccessText(appDetail)
}

data class ActionAutomationFailedDetail(
    private val appDetail: AppDetail?,
    private val output: Output.HasAutomationErrorText,
) : ActionDetail {
    @Composable
    fun automationErrorText(): String = output.automationErrorText(appDetail)
}

object LocationFindingFailedDetail : ActionDetail

object LocationPermissionReceivedDetail : ActionDetail

@HiltViewModel
class MainViewModel @Inject constructor(
    private val actionStateContext: ActionStateContext,
    private val conversionStateContext: ConversionStateContext,
    private val log: Log,
    appRepository: AppRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // Source

    private val _source = savedStateHandle.getMutableStateFlow("source", "")
    val source: StateFlow<String> = _source.asStateFlow()

    private val _sourceComesFromIntent = savedStateHandle.getMutableStateFlow("sourceComesFromIntent", false)
    val sourceComesFromIntent: StateFlow<Boolean> = _sourceComesFromIntent.asStateFlow()

    // Conversion

    private var _conversionState: MutableStateFlow<ConversionState> = MutableStateFlow(ConversionState.Initial)
    val conversionState: StateFlow<ConversionState> = _conversionState.asStateFlow()
    private var conversionJob: Job? = null
    private val conversionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        log.e(TAG, "Exception when transitioning state", tr)
        val newState = ConversionFailed(
            _source.value,
            conversionStateContext.resources.getString(R.string.conversion_failed_reason_exception),
            stackTrace = tr.stackTraceToString(),
        )
        _conversionState.value = newState
        _conversionStateLog.append(newState)
    }

    private fun transitionConversion(clearLog: Boolean = false, newState: (suspend () -> ConversionState)) {
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch(conversionExceptionHandler) {
            val newState = newState()
            _conversionState.value = newState
            log.d(TAG, "Set conversion state to $newState")
            _conversionStateLog.append(newState, clear = clearLog)
            newState.transitionRecursively(conversionStateContext) { newState ->
                _conversionState.value = newState as ConversionState
                log.d(TAG, "Transitioned action state to $newState")
                _conversionStateLog.append(newState)
            }
        }
    }

    // Action

    private var _actionState: MutableStateFlow<ActionState> = MutableStateFlow(ActionState.Initial)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()
    private var actionJob: Job? = null
    private val actionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        log.e(TAG, "Exception when transitioning state", tr)
        val newState = ActionCompleted(ActionResult.FAILED)
        _actionState.value = newState
    }
    val actionDetail: StateFlow<ActionDetail?> = _actionState
        .combine(appRepository.appDetails) { currentState, appDetails ->
            when (currentState) {
                is ActionWaiting -> ActionWaitingDetail(
                    (currentState.output as? Output.HasActivity<*>)?.getAppDetail(appDetails),
                    currentState.delay,
                    currentState.output,
                )

                is ActionSucceeded -> ActionSucceededDetail(
                    (currentState.output as? Output.HasActivity<*>)?.getAppDetail(appDetails),
                    currentState.output,
                )

                is ActionFailed -> ActionFailedDetail(
                    (currentState.output as? Output.HasActivity<*>)?.getAppDetail(appDetails),
                    currentState.output,
                )

                is ActionAutomationSucceeded -> ActionAutomationSucceededDetail(
                    (currentState.output as? Output.HasActivity<*>)?.getAppDetail(appDetails),
                    currentState.output,
                )

                is ActionAutomationFailed -> ActionAutomationFailedDetail(
                    (currentState.output as? Output.HasActivity<*>)?.getAppDetail(appDetails),
                    currentState.output,
                )

                is LocationFindingFailed -> LocationFindingFailedDetail

                is LocationPermissionReceived -> LocationPermissionReceivedDetail

                else -> null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun transitionAction(newState: (suspend () -> ActionState)) {
        actionJob?.cancel()
        actionJob = viewModelScope.launch(actionExceptionHandler) {
            val newState = newState()
            _actionState.value = newState
            log.d(TAG, "Set action state to $newState")
            newState.transitionRecursively(actionStateContext) { newState ->
                log.d(TAG, "Transitioned action state to $newState")
                _actionState.value = newState as ActionState
            }
        }
    }

    init {
        // Start automation action when conversion succeeds
        _conversionState
            .filterIsInstance<AutomationRequested>()
            .onEach { conversionState ->
                transitionAction { AutomationReceived(conversionState.points, conversionState.output) }
            }
            .launchIn(viewModelScope)
    }

    // Conversion state log

    private var _conversionStateLog: MutableStateFlow<StateLog<ConversionState>> = MutableStateFlow(emptyList())
    val conversionStateLog: StateFlow<StateLog<ConversionState>> = _conversionStateLog.asStateFlow()
    val extendedConversionStateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>> = _conversionStateLog
        .map { it.toExtendedLog() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val conversionStart: StateFlow<ComparableTimeMark?> = conversionStateLog
        .map { it.firstOrNull()?.start }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Methods

    fun start(sourceComesFromIntent: Boolean) {
        _sourceComesFromIntent.value = sourceComesFromIntent
        transitionConversion(clearLog = true) { SourceReceived(_source.value) }
    }

    fun grant(doNotAsk: Boolean) {
        (_conversionState.value as? ConversionState.HasPermission)?.apply {
            transitionConversion { grant(conversionStateContext, doNotAsk) }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (_conversionState.value as? ConversionState.HasPermission)?.apply {
            transitionConversion { deny(conversionStateContext, doNotAsk) }
        }
    }

    fun cancelConversion() {
        conversionJob?.cancel()
    }

    fun reset() {
        transitionConversion(clearLog = true) { ConversionState.Initial }
    }

    fun retry() {
        (_conversionState.value as? ConversionState.HasError)?.apply {
            transitionConversion {
                SourceReceived(source)
            }
        }
    }

    fun setSource(newSource: String) {
        _source.value = newSource
    }

    // Any action

    fun startAction(action: Action<*>) {
        transitionAction { ActionReady(action, isAutomation = false) }
    }

    fun completeBasicAction(actionResult: ActionResult) {
        (_actionState.value as? BasicActionReady)?.apply {
            transitionAction { ActionRan(action, actionResult, isAutomation) }
        }
    }

    fun cancelAction() {
        actionJob?.cancel()
    }

    // File action

    fun receiveFileUri(uri: Uri) {
        (_actionState.value as? FileUriRequested)?.apply {
            transitionAction { FileActionReady(action, isAutomation, uri) }
        }
    }

    fun cancelFileUriRequest() {
        (_actionState.value as? FileUriRequested)?.apply {
            transitionAction { ActionCompleted(ActionResult.FAILED) }
        }
    }

    fun completeFileAction(actionResult: ActionResult) {
        (_actionState.value as? FileActionReady)?.apply {
            transitionAction { ActionRan(action, actionResult, isAutomation) }
        }
    }

    // Location action

    fun showLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        transitionAction { LocationRationaleShown(action, isAutomation) }
    }

    fun skipLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        transitionAction { LocationPermissionReceived(action, isAutomation) }
    }

    fun receiveLocationPermission() {
        (_actionState.value as? LocationRationaleConfirmed)?.apply {
            transitionAction { LocationPermissionReceived(action, isAutomation) }
        }
    }

    fun receiveLocation(action: LocationAction<*>, isAutomation: Boolean, location: Point?) {
        transitionAction { LocationReceived(action, isAutomation, location) }
    }

    fun cancelLocationFinding() {
        (_actionState.value as? LocationPermissionReceived)?.apply {
            transitionAction { ActionCompleted(ActionResult.FAILED) }
        }
    }

    fun completeLocationAction(actionResult: ActionResult) {
        (_actionState.value as? LocationActionReady)?.apply {
            transitionAction { ActionRan(action, actionResult, isAutomation) }
        }
    }

    // Lifecycle

    fun onCreateOrNewIntent(intent: Intent) {
        setSource(intent.getUriString().orEmpty())
        start(true)
    }

    private companion object {
        private const val TAG = "ConversionViewModel"
    }
}

fun StateLog<ConversionState>.toExtendedLog(): ExtendedStateLog<ConversionState.HasDescription> =
    zipWithNextLastNull { logItem, nextLogItem ->
        if (logItem.state is ConversionState.HasDescription) {
            if (nextLogItem != null) {
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = logItem.id,
                    state = logItem.state,
                    start = logItem.start,
                    end = nextLogItem.start,
                    succeeded = when (nextLogItem.state) {
                        is ConversionState.HasError -> false
                        is ConversionState.HasAttempt if nextLogItem.state.lastAttempt != null -> false
                        else -> true
                    },
                )
            } else {
                ExtendedStateLogItem.Pending<ConversionState.HasDescription>(
                    id = logItem.id,
                    state = logItem.state,
                    start = logItem.start,
                )
            }
        } else {
            null
        }
    }
        .filterNotNull()
