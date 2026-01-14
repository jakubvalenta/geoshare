package page.ooooo.geoshare

import android.app.Activity
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.lib.billing.Plan
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionRan
import page.ooooo.geoshare.lib.conversion.ActionReady
import page.ooooo.geoshare.lib.conversion.BasicActionReady
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationActionReady
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.conversion.LocationRationaleConfirmed
import page.ooooo.geoshare.lib.conversion.LocationRationaleShown
import page.ooooo.geoshare.lib.conversion.LocationReceived
import page.ooooo.geoshare.lib.conversion.ReceivedUriString
import page.ooooo.geoshare.lib.conversion.State
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.ui.SavableDelegate
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val billing: Billing,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val stateContext = ConversionStateContext(
        inputs = allInputs,
        userPreferencesRepository = userPreferencesRepository,
        billing = billing,
    ) { newState ->
        _currentState.value = newState
        when (newState) {
            is ConversionState.HasLoadingIndicator -> {
                loadingIndicatorJob?.cancel()
                loadingIndicatorJob = viewModelScope.launch {
                    // Show loading indicator only if the state lasts longer than 200ms.
                    delay(200L)
                    _loadingIndicator.value = newState.loadingIndicator
                }
            }

            else -> {
                loadingIndicatorJob?.cancel()
                loadingIndicatorJob = viewModelScope.launch {
                    // Hide loading indicator only if another loading indicator is not shown within the next 200ms.
                    delay(200L)
                    _loadingIndicator.value = null
                }
            }
        }
    }

    private val _currentState = MutableStateFlow<State>(Initial())
    val currentState: StateFlow<State> = _currentState

    var inputUriString by SavableDelegate(savedStateHandle, "inputUriString", "")

    private val _loadingIndicator = MutableStateFlow<LoadingIndicator?>(null)
    val loadingIndicator: StateFlow<LoadingIndicator?> = _loadingIndicator

    private var loadingIndicatorJob: Job? = null
    private var transitionJob: Job? = null

    val availablePlans: List<Plan> = billing.availablePlans
    val billingErrorMessageResId: StateFlow<Int?> = billing.errorMessageResId
    val offers: StateFlow<List<Offer>> = billing.offers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )
    val plan: StateFlow<Plan?> = billing.status.map {
        (it as? BillingStatus.Done)?.plan
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null,
    )
    val automationFeatureStatus: StateFlow<FeatureStatus> = billing.status.map {
        it.getFeatureStatus(AutomationFeature)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FeatureStatus.LOADING,
    )

    val userPreferencesValues: StateFlow<UserPreferencesValues> = userPreferencesRepository.values
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesValues(),
        )

    val automation: StateFlow<Automation> = userPreferencesRepository.values
        .map { it.automation }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AutomationPreference.default,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val changelogShown: StateFlow<Boolean> = userPreferencesRepository.values
        .map { it.changelogShownForVersionCode }
        .distinctUntilChanged()
        .mapLatest { changelogShownForVersionCodeValue ->
            if (changelogShownForVersionCodeValue != null) {
                stateContext.inputs.all { input ->
                    input.documentation.items.all { inputDocumentationItem ->
                        inputDocumentationItem.addedInVersionCode <= changelogShownForVersionCodeValue
                    }
                }
            } else {
                true
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true,
        )

    val changelogShownForVersionCode: StateFlow<Int?> = userPreferencesRepository.values
        .map { it.changelogShownForVersionCode }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ChangelogShownForVersionCodePreference.default,
        )

    val introShown: StateFlow<Boolean> = userPreferencesRepository.values
        .map { it.introShownForVersionCode != IntroShowForVersionCodePreference.default }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true,
        )

    fun start() {
        stateContext.currentState = ReceivedUriString(stateContext, inputUriString)
        transition()
    }

    fun grant(doNotAsk: Boolean) {
        (stateContext.currentState as? ConversionState.HasPermission)?.let { currentState ->
            transitionJob?.cancel()
            transitionJob = viewModelScope.launch {
                try {
                    stateContext.currentState = currentState.grant(doNotAsk)
                    stateContext.transition()
                } catch (tr: Exception) {
                    stateContext.log.e(null, "Exception while transitioning state", tr)
                    stateContext.currentState = ConversionFailed(
                        R.string.conversion_failed_parse_url_error,
                        inputUriString,
                    )
                }
            }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (stateContext.currentState as? ConversionState.HasPermission)?.let { currentState ->
            transitionJob?.cancel()
            transitionJob = viewModelScope.launch {
                try {
                    stateContext.currentState = currentState.deny(doNotAsk)
                    stateContext.transition()
                } catch (tr: Exception) {
                    stateContext.log.e(null, "Exception while transitioning state", tr)
                    stateContext.currentState = ConversionFailed(
                        R.string.conversion_failed_parse_url_error,
                        inputUriString,
                    )
                }
            }
        }
    }

    fun showLocationRationale(action: LocationAction, i: Int?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            stateContext.currentState = LocationRationaleShown(
                currentState.inputUriString, currentState.position, i, action
            )
            transition()
        }
    }

    fun skipLocationRationale(action: LocationAction, i: Int?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            stateContext.currentState = LocationPermissionReceived(
                currentState.inputUriString, currentState.position, i, action
            )
            transition()
        }
    }

    fun receiveLocationPermission() {
        (stateContext.currentState as? LocationRationaleConfirmed)?.let { currentState ->
            stateContext.currentState = LocationPermissionReceived(
                currentState.inputUriString, currentState.position, currentState.i, currentState.action
            )
            transition()
        }
    }

    fun receiveLocation(action: LocationAction, i: Int?, location: Point?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            stateContext.currentState = LocationReceived(
                currentState.inputUriString, currentState.position, i, action, location
            )
            transition()
        }
    }

    fun cancelLocationFinding() {
        (stateContext.currentState as? LocationPermissionReceived)?.let { currentState ->
            stateContext.currentState = ActionFinished(
                currentState.inputUriString, currentState.position, currentState.action
            )
            transition()
        }
    }

    fun runAction(action: Action, i: Int?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            stateContext.currentState = ActionReady(
                currentState.inputUriString, currentState.position, i, action
            )
            transition()
        }
    }

    fun finishBasicAction(success: Boolean?) {
        (stateContext.currentState as? BasicActionReady)?.let { currentState ->
            stateContext.currentState = ActionRan(
                currentState.inputUriString, currentState.position, currentState.action, success
            )
            transition()
        }
    }

    fun finishLocationAction(success: Boolean?) {
        (stateContext.currentState as? LocationActionReady)?.let { currentState ->
            stateContext.currentState = ActionRan(
                currentState.inputUriString, currentState.position, currentState.action, success
            )
            transition()
        }
    }

    fun writeGpx(writer: Appendable) {
        (stateContext.currentState as? ConversionState.HasResult)?.position?.writeGpxPoints(writer)
    }

    fun launchBillingFlow(activity: Activity, offerToken: String) {
        viewModelScope.launch {
            billing.launchBillingFlow(activity, offerToken)
        }
    }

    private fun transition() {
        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            try {
                stateContext.transition()
            } catch (tr: Exception) {
                stateContext.log.e(null, "Exception while transitioning state", tr)
                stateContext.currentState = ConversionFailed(
                    R.string.conversion_failed_parse_url_error,
                    inputUriString,
                )
            }
        }
    }

    fun cancel() {
        transitionJob?.cancel()
    }

    fun updateInput(value: String) {
        withMutableSnapshot {
            inputUriString = value
        }
    }

    fun reset() {
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
        }
    }

    fun setChangelogShown() {
        val newestInputAddedInVersionCode = allInputs.maxOf { input ->
            input.documentation.items.maxOf { it.addedInVersionCode }
        }
        setUserPreferenceValue(ChangelogShownForVersionCodePreference, newestInputAddedInVersionCode)
    }

    fun setIntroShown() {
        setUserPreferenceValue(IntroShowForVersionCodePreference, BuildConfig.VERSION_CODE)
    }

    fun <T> setUserPreferenceValue(userPreference: UserPreference<T>, value: T) {
        viewModelScope.launch {
            userPreferencesRepository.setValue(userPreference, value)
        }
    }

    fun editUserPreferences(transform: (preferences: MutablePreferences) -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.edit(transform)
        }
    }

    fun onResume() {
        billing.startConnection()
    }

    fun onPause() {
        billing.endConnection()
    }

    override fun onCleared() {
        super.onCleared()
        billing.endConnection()
    }
}
