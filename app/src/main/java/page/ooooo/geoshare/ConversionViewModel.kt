package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.lastRunVersionCode
import page.ooooo.geoshare.lib.*
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val stateContext = ConversionStateContext(
        googleMapsUrlConverter = GoogleMapsUrlConverter(),
        intentTools = IntentTools(),
        networkTools = NetworkTools(),
        userPreferencesRepository = userPreferencesRepository,
        xiaomiTools = XiaomiTools(),
        onMessage = { _message.value = it },
    )

    private val _currentState = MutableStateFlow<State>(Initial())
    val currentState: StateFlow<State> = _currentState

    var inputUriString by SavableDelegate(
        savedStateHandle,
        "inputUriString",
        "",
    )
    var resultGeoUri by SavableDelegate(
        savedStateHandle,
        "resultGeoUri",
        "",
    )
    var resultUnchanged by SavableDelegate(
        savedStateHandle,
        "resultUnchanged",
        false,
    )
    var resultErrorMessageResId by SavableDelegate<Int?>(
        savedStateHandle,
        "resultErrorMessage",
        null,
    )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPreferencesValues: StateFlow<UserPreferencesValues> =
        userPreferencesRepository.values.mapLatest { it }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesValues(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val introShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.introShownForVersionCodeValue != lastRunVersionCode.default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        userPreferencesValues.value.introShownForVersionCodeValue != lastRunVersionCode.default,
    )

    fun start() {
        withMutableSnapshot {
            resultGeoUri = ""
            resultUnchanged = false
            resultErrorMessageResId = null
        }
        transition(ReceivedUriString(stateContext, inputUriString))
    }

    fun start(intent: Intent) {
        transition(ReceivedIntent(stateContext, intent))
    }

    fun grant(doNotAsk: Boolean) {
        viewModelScope.launch {
            assert(stateContext.currentState is PermissionState)
            transition(
                (stateContext.currentState as PermissionState).grant(doNotAsk)
            )
        }
    }

    fun deny(doNotAsk: Boolean) {
        viewModelScope.launch {
            assert(stateContext.currentState is PermissionState)
            transition(
                (stateContext.currentState as PermissionState).deny(doNotAsk)
            )
        }
    }

    fun share(
        context: Context,
        settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    ) {
        transition(
            AcceptedSharing(
                stateContext,
                context,
                settingsLauncherWrapper,
                resultGeoUri,
                resultUnchanged,
            )
        )
    }

    fun copy(clipboard: Clipboard) {
        transition(
            AcceptedCopying(
                stateContext,
                clipboard,
                resultGeoUri,
                resultUnchanged,
            )
        )
    }

    private fun transition(newState: State) {
        viewModelScope.launch {
            stateContext.currentState = newState
            stateContext.transition()
            _currentState.value = stateContext.currentState
            when (stateContext.currentState) {
                is ConversionSucceeded ->
                    (stateContext.currentState as ConversionSucceeded).let {
                        withMutableSnapshot {
                            resultGeoUri = it.geoUri
                            resultUnchanged = it.unchanged
                        }
                    }

                is ConversionFailed ->
                    (stateContext.currentState as ConversionFailed).let {
                        withMutableSnapshot {
                            resultErrorMessageResId = it.messageResId
                        }
                    }
            }
        }
    }

    fun updateInput(newUriString: String) {
        withMutableSnapshot {
            inputUriString = newUriString
            resultGeoUri = ""
            resultUnchanged = false
            resultErrorMessageResId = null
        }
        if (stateContext.currentState !is Initial) {
            transition(Initial())
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun setIntroShown() {
        setUserPreferenceValue(lastRunVersionCode, BuildConfig.VERSION_CODE)
    }

    fun <T> setUserPreferenceValue(
        userPreference: UserPreference<T>,
        value: T,
    ) {
        viewModelScope.launch {
            userPreferencesRepository.setValue(userPreference, value)
        }
    }
}
