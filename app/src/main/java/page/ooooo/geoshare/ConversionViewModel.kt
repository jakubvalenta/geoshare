package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionRan
import page.ooooo.geoshare.lib.conversion.ActionReady
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.FileActionReady
import page.ooooo.geoshare.lib.conversion.FileUriRequested
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.LocationReceived
import page.ooooo.geoshare.lib.conversion.ReceivedUriString
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.getOutputsForApps
import page.ooooo.geoshare.lib.outputs.getOutputsForLinks
import page.ooooo.geoshare.lib.outputs.getOutputsForPoint
import page.ooooo.geoshare.lib.outputs.getOutputsForPointChips
import page.ooooo.geoshare.lib.outputs.getOutputsForPoints
import page.ooooo.geoshare.lib.outputs.getOutputsForPointsChips
import page.ooooo.geoshare.lib.outputs.getOutputsForSharing
import page.ooooo.geoshare.lib.point.Point
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class ConversionViewModel @Inject constructor(
    appsRepository: AppsRepository,
    @ApplicationContext context: Context,
    private val linkRepository: LinkRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val billing: Billing,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _currentState = MutableStateFlow<State>(Initial())
    val currentState: StateFlow<State> = _currentState

    val stateContext = ConversionStateContext(
        inputs = allInputs,
        linkRepository = linkRepository,
        resources = context.resources,
        userPreferencesRepository = userPreferencesRepository,
        billing = billing,
    ) { newState ->
        Log.d(TAG, "Current state is ${newState::class.simpleName}")
        _currentState.value = newState
    }

    var inputUriString by savedStateHandle.saveable("inputUriString") { mutableStateOf("") }

    private var transitionJob: Job? = null

    val appDetails = appsRepository.appDetails
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )
    val outputsForPoint: StateFlow<List<PointOutput>> =
        linkRepository.all.map { getOutputsForPoint(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPoints: StateFlow<List<PointsOutput>> =
        flow { emit(getOutputsForPoints()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointChips: StateFlow<List<PointOutput>> =
        linkRepository.all.map { getOutputsForPointChips(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointsChips: StateFlow<List<PointsOutput>> =
        flow { emit(getOutputsForPointsChips()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForApps: StateFlow<Map<String, List<Output>>> =
        appsRepository.apps
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { apps, hiddenApps ->
                getOutputsForApps(apps, hiddenApps)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val outputsForLinks: StateFlow<Map<String?, List<Output>>> =
        linkRepository.all.map { links -> getOutputsForLinks(links) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val outputsForSharing: StateFlow<List<Output>> =
        flow { emit(getOutputsForSharing()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )

    // Methods

    fun start() {
        stateContext.currentState = ReceivedUriString(stateContext, inputUriString)
        transition()
    }

    private fun transition(initialState: (suspend () -> State?)? = null) {
        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            try {
                if (initialState != null) {
                    stateContext.currentState = initialState() ?: return@launch
                }
                stateContext.transition()
            } catch (tr: Exception) {
                stateContext.log.e(TAG, "Exception while transitioning state", tr)
                stateContext.log.e(TAG, tr.stackTraceToString())
                stateContext.currentState = ConversionFailed(
                    stateContext.resources.getString(R.string.conversion_failed_parse_url_error),
                    inputUriString,
                )
            }
        }
    }

    fun grant(doNotAsk: Boolean) {
        (stateContext.currentState as? ConversionState.HasPermission)?.let { currentState ->
            transition { currentState.grant(doNotAsk) }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (stateContext.currentState as? ConversionState.HasPermission)?.let { currentState ->
            transition { currentState.deny(doNotAsk) }
        }
    }

    fun cancel() {
        transitionJob?.cancel()
    }

    fun reset() {
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
        }
    }

    // Any action

    fun startAction(action: Action<*>) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            transition {
                ActionReady(currentState.inputUriString, currentState.points, action, isAutomation = false)
            }
        }
    }

    fun finishBasicAction(success: Boolean?) {
        (stateContext.currentState as? BasicActionReady)?.let { currentState ->
            transition {
                ActionRan(
                    currentState.inputUriString,
                    currentState.points,
                    currentState.action,
                    currentState.isAutomation,
                    success,
                )
            }
        }
    }

    // File action

    fun receiveFileUri(uri: Uri) {
        (stateContext.currentState as? FileUriRequested)?.let { currentState ->
            transition {
                FileActionReady(
                    currentState.inputUriString,
                    currentState.points,
                    currentState.action,
                    currentState.isAutomation,
                    uri,
                )
            }
        }
    }

    fun cancelFileUriRequest() {
        (stateContext.currentState as? FileUriRequested)?.let { currentState ->
            transition {
                ActionFinished(
                    currentState.inputUriString, currentState.points, currentState.action, currentState.isAutomation
                )
            }
        }
    }

    fun finishFileAction(success: Boolean?) {
        (stateContext.currentState as? FileActionReady)?.let { currentState ->
            transition {
                ActionRan(
                    currentState.inputUriString,
                    currentState.points,
                    currentState.action,
                    currentState.isAutomation,
                    success,
                )
            }
        }
    }

    // Location action

    fun showLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            transition {
                LocationRationaleShown(currentState.inputUriString, currentState.points, action, isAutomation)
            }
        }
    }

    fun skipLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            transition {
                LocationPermissionReceived(
                    stateContext,
                    currentState.inputUriString,
                    currentState.points,
                    action,
                    isAutomation,
                )
            }
        }
    }

    fun receiveLocationPermission() {
        (stateContext.currentState as? LocationRationaleConfirmed)?.let { currentState ->
            transition {
                LocationPermissionReceived(
                    stateContext,
                    currentState.inputUriString,
                    currentState.points,
                    currentState.action,
                    currentState.isAutomation,
                )
            }
        }
    }

    fun receiveLocation(action: LocationAction<*>, isAutomation: Boolean, location: Point?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            transition {
                LocationReceived(
                    currentState.inputUriString, currentState.points, action, isAutomation, location
                )
            }
        }
    }

    fun cancelLocationFinding() {
        (stateContext.currentState as? LocationPermissionReceived)?.let { currentState ->
            transition {
                ActionFinished(
                    currentState.inputUriString, currentState.points, currentState.action, currentState.isAutomation
                )
            }
        }
    }

    fun finishLocationAction(success: Boolean?) {
        (stateContext.currentState as? LocationActionReady)?.let { currentState ->
            transition {
                ActionRan(
                    currentState.inputUriString,
                    currentState.points,
                    currentState.action,
                    currentState.isAutomation,
                    success,
                )
            }
        }
    }

    // Lifecycle

    fun onCreateOrNewIntent(intent: Intent) {
        inputUriString = AndroidTools.getIntentUriString(intent) ?: ""
        start()
    }

    companion object {
        const val TAG = "ConversionViewModel"
    }
}
