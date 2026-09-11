package page.ooooo.geoshare.ui

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.conversion.ActionCompleted
import page.ooooo.geoshare.lib.conversion.ActionRan
import page.ooooo.geoshare.lib.conversion.ActionReady
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.ExtendedConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.FileActionReady
import page.ooooo.geoshare.lib.conversion.FileUriRequested
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.LocationReceived
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.extensions.zipWithNextLastNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.LocationAction
import javax.inject.Inject
import kotlin.time.ComparableTimeMark

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val stateContext: ConversionStateContext,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val currentState = stateContext.currentState
    val extendedStateLog: StateFlow<List<ExtendedConversionStateLogItem>> = stateContext.stateLog
        .map { stateLog ->
            stateLog
                .zipWithNextLastNull { logItem, nextLogItem ->
                    if (logItem.state is ConversionState.HasDescription) {
                        if (nextLogItem != null) {
                            ExtendedConversionStateLogItem.Finished(
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
                            ExtendedConversionStateLogItem.Pending(
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

    val start: StateFlow<ComparableTimeMark?> = stateContext.stateLog
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

    private var transitionJob: Job? = null
    private val transitionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        stateContext.setExceptionState(
            tr,
            ConversionFailed(
                _source.value,
                stateContext.resources.getString(R.string.conversion_failed_reason_exception),
                stackTrace = tr.stackTraceToString(),
            )
        )
    }

    // Methods

    fun start(sourceComesFromIntent: Boolean) {
        _sourceComesFromIntent.value = sourceComesFromIntent
        transition(resetLog = true) { SourceReceived(_source.value) }
    }

    private fun transition(resetLog: Boolean = false, initialState: (suspend () -> ConversionState)) {
        transitionJob?.cancel()
        transitionJob = viewModelScope.launch(transitionExceptionHandler) {
            stateContext.transition(initialState(), resetLog)
        }
    }

    fun grant(doNotAsk: Boolean) {
        (stateContext.currentState.value as? ConversionState.HasPermission)?.apply {
            transition { grant(stateContext, doNotAsk) }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (stateContext.currentState.value as? ConversionState.HasPermission)?.apply {
            transition { deny(stateContext, doNotAsk) }
        }
    }

    fun cancel() {
        transitionJob?.cancel()
    }

    fun reset() {
        transition(resetLog = true) { Initial }
    }

    fun retry() {
        (stateContext.currentState.value as? ConversionState.HasError)?.apply {
            transition { SourceReceived(source) }
        }
    }

    fun setSource(newSource: String) {
        _source.value = newSource
    }

    // Any action

    fun startAction(action: Action<*>) {
        (stateContext.currentState.value as? ConversionState.HasResult)?.apply {
            transition { ActionReady(source, points, action, isAutomation = false) }
        }
    }

    fun completeBasicAction(actionResult: ActionResult) {
        (stateContext.currentState.value as? BasicActionReady)?.apply {
            transition { ActionRan(source, points, action, actionResult, isAutomation) }
        }
    }

    // File action

    fun receiveFileUri(uri: Uri) {
        (stateContext.currentState.value as? FileUriRequested)?.apply {
            transition { FileActionReady(source, points, action, isAutomation, uri) }
        }
    }

    fun cancelFileUriRequest() {
        (stateContext.currentState.value as? FileUriRequested)?.apply {
            transition { ActionCompleted(source, points, ActionResult.FAILED) }
        }
    }

    fun completeFileAction(actionResult: ActionResult) {
        (stateContext.currentState.value as? FileActionReady)?.apply {
            transition { ActionRan(source, points, action, actionResult, isAutomation) }
        }
    }

    // Location action

    fun showLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (stateContext.currentState.value as? ConversionState.HasResult)?.apply {
            transition { LocationRationaleShown(source, points, action, isAutomation) }
        }
    }

    fun skipLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (stateContext.currentState.value as? ConversionState.HasResult)?.apply {
            transition { LocationPermissionReceived(source, points, action, isAutomation) }
        }
    }

    fun receiveLocationPermission() {
        (stateContext.currentState.value as? LocationRationaleConfirmed)?.apply {
            transition { LocationPermissionReceived(source, points, action, isAutomation) }
        }
    }

    fun receiveLocation(action: LocationAction<*>, isAutomation: Boolean, location: Point?) {
        (stateContext.currentState.value as? ConversionState.HasResult)?.apply {
            transition { LocationReceived(source, points, action, isAutomation, location) }
        }
    }

    fun cancelLocationFinding() {
        (stateContext.currentState.value as? LocationPermissionReceived)?.apply {
            transition { ActionCompleted(source, points, ActionResult.FAILED) }
        }
    }

    fun completeLocationAction(actionResult: ActionResult) {
        (stateContext.currentState.value as? LocationActionReady)?.apply {
            transition { ActionRan(source, points, action, actionResult, isAutomation) }
        }
    }

    // Lifecycle

    fun onCreateOrNewIntent(intent: Intent) {
        setSource(AndroidTools.getIntentUriString(intent).orEmpty())
        start(true)
    }
}
