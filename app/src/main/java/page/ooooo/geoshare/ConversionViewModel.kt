package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.local.preferences.lastRunVersionCode
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val stateContext = ConversionStateContext(
        urlConverters = listOf(
            GoogleMapsUrlConverter(),
            AppleMapsUrlConverter(),
        ),
        intentTools = IntentTools(),
        networkTools = NetworkTools(),
        userPreferencesRepository = userPreferencesRepository,
        xiaomiTools = XiaomiTools(),
        onMessage = { _message.value = it },
        onStateChange = { newState ->
            _currentState.value = newState
            when (newState) {
                is ConversionStateWithLoadingIndicator -> {
                    loadingIndicatorJob?.cancel()
                    loadingIndicatorJob = viewModelScope.launch {
                        // Show loading indicator only if the state lasts longer than 200ms.
                        delay(200L)
                        withMutableSnapshot {
                            loadingIndicatorTitleResId = newState.urlConverter.loadingIndicatorTitleResId
                        }
                    }
                }

                else -> {
                    loadingIndicatorJob?.cancel()
                    loadingIndicatorJob = viewModelScope.launch {
                        // Hide loading indicator only if another loading indicator is not shown within the next 200ms.
                        delay(200L)
                        withMutableSnapshot {
                            loadingIndicatorTitleResId = null
                        }
                    }
                }
            }
            when (newState) {
                is ConversionSucceeded -> {
                    withMutableSnapshot {
                        resultGeoUri = newState.geoUri
                    }
                }

                is ConversionFailed -> {
                    withMutableSnapshot {
                        resultErrorMessageResId = newState.messageResId
                    }
                }
            }
        }
    )

    private val _currentState = MutableStateFlow<State>(Initial())
    val currentState: StateFlow<State> = _currentState

    var inputUriString by SavableDelegate(
        savedStateHandle,
        "inputUriString",
        "",
    )
    var loadingIndicatorTitleResId by SavableDelegate<Int?>(
        savedStateHandle,
        "loadingIndicatorTitleResId",
        null,
    )
    private var originalUri: Uri? = null  // FIXME

    var resultGeoUri by SavableDelegate(
        savedStateHandle,
        "resultGeoUri",
        "",
    )
    var resultErrorMessageResId by SavableDelegate<Int?>(
        savedStateHandle,
        "resultErrorMessageResId",
        null,
    )

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

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
            resultErrorMessageResId = null
        }
        stateContext.currentState = ReceivedUriString(stateContext, inputUriString)
        transition()
    }

    fun start(intent: Intent) {
        stateContext.currentState = ReceivedIntent(stateContext, intent)
        transition()
    }

    fun grant(doNotAsk: Boolean) {
        assert(stateContext.currentState is PermissionState)
        viewModelScope.launch {
            stateContext.currentState = (stateContext.currentState as PermissionState).grant(doNotAsk)
            transition()
        }
    }

    fun deny(doNotAsk: Boolean) {
        assert(stateContext.currentState is PermissionState)
        viewModelScope.launch {
            stateContext.currentState = (stateContext.currentState as PermissionState).deny(doNotAsk)
            transition()
        }
    }

    fun share(context: Context, settingsLauncherWrapper: ManagedActivityResultLauncherWrapper) {
        originalUri?.let { originalUri ->
            stateContext.currentState = AcceptedSharing(
                stateContext,
                context,
                settingsLauncherWrapper,
                resultGeoUri,
                originalUri,
            )
            transition()
        }
    }

    fun copy(clipboard: Clipboard) {
        stateContext.currentState = AcceptedCopying(stateContext, clipboard, resultGeoUri)
        transition()
    }

    private fun transition() {
        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            stateContext.transition()
        }
    }

    fun cancel() {
        transitionJob?.cancel()
    }

    fun updateInput(newUriString: String) {
        withMutableSnapshot {
            inputUriString = newUriString
            resultGeoUri = ""
            resultErrorMessageResId = null
        }
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
            transition()
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
