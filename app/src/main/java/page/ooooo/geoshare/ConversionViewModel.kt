package page.ooooo.geoshare

import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.*
import page.ooooo.geoshare.lib.SavableDelegate
import page.ooooo.geoshare.lib.conversion.*
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.LocationAction
import page.ooooo.geoshare.lib.position.Point
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val stateContext = ConversionStateContext(
        inputs = allInputs,
        userPreferencesRepository = userPreferencesRepository,
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

    var inputUriString by SavableDelegate(
        savedStateHandle,
        "inputUriString",
        "",
    )

    private val _loadingIndicator = MutableStateFlow<LoadingIndicator?>(null)
    val loadingIndicator: StateFlow<LoadingIndicator?> = _loadingIndicator

    private var loadingIndicatorJob: Job? = null
    private var transitionJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPreferencesValues: StateFlow<UserPreferencesValues> =
        userPreferencesRepository.values.mapLatest { it }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesValues(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val automation: StateFlow<Automation> = userPreferencesValues.mapLatest {
        it.automationValue
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AutomationUserPreference.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val changelogShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.changelogShownForVersionCodeValue?.let { changelogShownForVersionCodeValue ->
            stateContext.inputs.all { input ->
                input.documentation.inputs.all { input ->
                    input.addedInVersionCode <= changelogShownForVersionCodeValue
                }
            }
        } ?: true
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val changelogShownForVersionCode: StateFlow<Int?> = userPreferencesValues.mapLatest {
        it.changelogShownForVersionCodeValue
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChangelogShownForVersionCode.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val introShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.introShownForVersionCodeValue != IntroShowForVersionCode.default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        userPreferencesValues.value.introShownForVersionCodeValue != IntroShowForVersionCode.default,
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
                currentState.inputUriString, currentState.position, i, action,
            )
            transition()
        }
    }

    fun receiveLocationPermission() {
        (stateContext.currentState as? LocationRationaleConfirmed)?.let { currentState ->
            stateContext.currentState = LocationPermissionReceived(
                currentState.inputUriString, currentState.position, currentState.i, currentState.action,
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

    fun runLocationAction(action: LocationAction, i: Int?, location: Point?) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            stateContext.currentState = LocationActionReady(
                currentState.inputUriString, currentState.position, i, action, location
            )
            transition()
        }
    }

    fun finishAction(success: Boolean?) {
        stateContext.currentState.let { currentState ->
            when (currentState) {
                is BasicActionReady -> {
                    stateContext.currentState = ActionRan(
                        currentState.inputUriString, currentState.position, currentState.action, success
                    )
                    transition()
                }

                is LocationActionReady -> {
                    stateContext.currentState = ActionRan(
                        currentState.inputUriString, currentState.position, currentState.action, success
                    )
                    transition()
                }
            }
        }
    }

    fun writeGpx(writer: Appendable) {
        (stateContext.currentState as? ConversionState.HasResult)?.position?.writeGpxPoints(writer)
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
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
        }
    }

    fun setChangelogShownForVersionCode(value: Int) {
        setUserPreferenceValue(ChangelogShownForVersionCode, value)
    }

    fun setIntroShown() {
        setUserPreferenceValue(IntroShowForVersionCode, BuildConfig.VERSION_CODE)
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
}
