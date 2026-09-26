package page.ooooo.geoshare.ui

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.lib.DefaultStateMachine
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.getUriString
import page.ooooo.geoshare.lib.conversion.AutomationRequested
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.extensions.zipWithNextLastNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionAutomationFailed
import page.ooooo.geoshare.lib.outputs.ActionAutomationSucceeded
import page.ooooo.geoshare.lib.outputs.ActionCompleted
import page.ooooo.geoshare.lib.outputs.ActionFailed
import page.ooooo.geoshare.lib.outputs.ActionRan
import page.ooooo.geoshare.lib.outputs.ActionReady
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.ActionState
import page.ooooo.geoshare.lib.outputs.ActionStateContext
import page.ooooo.geoshare.lib.outputs.ActionSucceeded
import page.ooooo.geoshare.lib.outputs.ActionWaiting
import page.ooooo.geoshare.lib.outputs.AutomationReceived
import page.ooooo.geoshare.lib.outputs.BasicActionReady
import page.ooooo.geoshare.lib.outputs.FileActionReady
import page.ooooo.geoshare.lib.outputs.FileUriRequested
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.LocationActionReady
import page.ooooo.geoshare.lib.outputs.LocationFindingFailed
import page.ooooo.geoshare.lib.outputs.LocationPermissionReceived
import page.ooooo.geoshare.lib.outputs.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.outputs.LocationRationaleShown
import page.ooooo.geoshare.lib.outputs.LocationReceived
import page.ooooo.geoshare.lib.outputs.Output
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
class ConversionViewModel @Inject constructor(
    private val actionStateContext: ActionStateContext,
    private val conversionStateContext: ConversionStateContext,
    log: Log,
    resources: Resources,
    appRepository: AppRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // TODO Connect conversionStateMachine to actionStateMachine
    private val conversionStateMachine = DefaultStateMachine(
        initialState = ConversionState.Initial,
        exceptionState = { tr ->
            ConversionFailed(
                _source.value,
                resources.getString(R.string.conversion_failed_reason_exception),
                stackTrace = tr.stackTraceToString(),
            )
        },
        log = log,
    )
    val conversionState = conversionStateMachine.currentState

    private val actionStateMachine = DefaultStateMachine(
        initialState = ActionState.Initial,
        exceptionState = { ActionCompleted(ActionResult.FAILED) },
        log = log,
    )
    val actionState = actionStateMachine.currentState

    init {
        // TODO Connect conversion state to action state
        conversionStateMachine.currentState
            .onEach { conversionState ->
                if (conversionState is AutomationRequested) {
                    actionStateMachine.transition(actionStateContext, viewModelScope, resetLog = true) {
                        AutomationReceived(conversionState.points, conversionState.output)
                    }
                }
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly)
    }

    val actionDetail: StateFlow<ActionDetail?> = actionStateMachine.currentState
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
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    val conversionStateLog: StateFlow<List<ConversionStateLogItem>> = conversionStateMachine.stateLog
        .map { stateLog ->
            stateLog
                .zipWithNextLastNull { logItem, nextLogItem ->
                    if (logItem.state is ConversionState.HasDescription) {
                        if (nextLogItem != null) {
                            ConversionStateLogItem.Finished(
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
                            ConversionStateLogItem.Pending(
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
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val start: StateFlow<ComparableTimeMark?> = conversionStateMachine.stateLog
        .map { it.firstOrNull()?.start }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    private val _source = savedStateHandle.getMutableStateFlow("source", "")
    val source: StateFlow<String> = _source.asStateFlow()

    private val _sourceComesFromIntent = savedStateHandle.getMutableStateFlow("sourceComesFromIntent", false)
    val sourceComesFromIntent: StateFlow<Boolean> = _sourceComesFromIntent.asStateFlow()

    // Methods

    fun start(sourceComesFromIntent: Boolean) {
        _sourceComesFromIntent.value = sourceComesFromIntent
        conversionStateMachine.transition(conversionStateContext, viewModelScope, resetLog = true) {
            SourceReceived(_source.value)
        }
    }

    fun grant(doNotAsk: Boolean) {
        (conversionStateMachine.currentState.value as? ConversionState.HasPermission)?.apply {
            conversionStateMachine.transition(conversionStateContext, viewModelScope) {
                grant(conversionStateContext, doNotAsk)
            }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (conversionStateMachine.currentState.value as? ConversionState.HasPermission)?.apply {
            conversionStateMachine.transition(conversionStateContext, viewModelScope) {
                deny(conversionStateContext, doNotAsk)
            }
        }
    }

    fun cancel() {
        conversionStateMachine.cancel()
    }

    fun reset() {
        conversionStateMachine.reset(conversionStateContext, viewModelScope)
    }

    fun retry() {
        (conversionStateMachine.currentState.value as? ConversionState.HasError)?.apply {
            conversionStateMachine.transition(conversionStateContext, viewModelScope) {
                SourceReceived(source)
            }
        }
    }

    fun setSource(newSource: String) {
        _source.value = newSource
    }

    // Any action

    fun startAction(action: Action<*>) {
        (actionStateMachine.currentState.value as? ConversionState.HasResult)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionReady(action, isAutomation = false)
            }
        }
    }

    fun completeBasicAction(actionResult: ActionResult) {
        (actionStateMachine.currentState.value as? BasicActionReady)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionRan(action, actionResult, isAutomation)
            }
        }
    }

    // File action

    fun receiveFileUri(uri: Uri) {
        (actionStateMachine.currentState.value as? FileUriRequested)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                FileActionReady(action, isAutomation, uri)
            }
        }
    }

    fun cancelFileUriRequest() {
        (actionStateMachine.currentState.value as? FileUriRequested)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionCompleted(ActionResult.FAILED)
            }
        }
    }

    fun completeFileAction(actionResult: ActionResult) {
        (actionStateMachine.currentState.value as? FileActionReady)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionRan(action, actionResult, isAutomation)
            }
        }
    }

    // Location action

    fun showLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (actionStateMachine.currentState.value as? ConversionState.HasResult)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                LocationRationaleShown(action, isAutomation)
            }
        }
    }

    fun skipLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (actionStateMachine.currentState.value as? ConversionState.HasResult)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                LocationPermissionReceived(action, isAutomation)
            }
        }
    }

    fun receiveLocationPermission() {
        (actionStateMachine.currentState.value as? LocationRationaleConfirmed)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                LocationPermissionReceived(action, isAutomation)
            }
        }
    }

    fun receiveLocation(action: LocationAction<*>, isAutomation: Boolean, location: Point?) {
        (actionStateMachine.currentState.value as? ConversionState.HasResult)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                LocationReceived(action, isAutomation, location)
            }
        }
    }

    fun cancelLocationFinding() {
        (actionStateMachine.currentState.value as? LocationPermissionReceived)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionCompleted(ActionResult.FAILED)
            }
        }
    }

    fun completeLocationAction(actionResult: ActionResult) {
        (actionStateMachine.currentState.value as? LocationActionReady)?.apply {
            actionStateMachine.transition(actionStateContext, viewModelScope) {
                ActionRan(action, actionResult, isAutomation)
            }
        }
    }

    // Lifecycle

    fun onCreateOrNewIntent(intent: Intent) {
        setSource(intent.getUriString().orEmpty())
        start(true)
    }
}
