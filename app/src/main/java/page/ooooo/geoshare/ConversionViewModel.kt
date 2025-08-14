package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.compose.ui.platform.Clipboard
import androidx.core.net.toUri
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
        onStateChange = { newState ->
            _currentState.value = newState
            when (newState) {
                is HasLoadingIndicator -> {
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
        })

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
        stateContext.currentState = ReceivedUriString(stateContext, inputUriString.toUri(), inputUriString)
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

    fun queryGeoUriApps(context: Context): List<Pair<String, Int>> =
        context.packageManager.queryIntentActivities(
            stateContext.intentTools.createChooser("geo:0,0".toUri()),
            PackageManager.MATCH_ALL,
        ).map {
            Pair(it.activityInfo.loadLabel(context.packageManager).toString(), it.iconResource)
        }

    fun share(context: Context, settingsLauncherWrapper: ManagedActivityResultLauncherWrapper) {
        assert(stateContext.currentState is HasResult)
        (stateContext.currentState as HasResult).let { currentState ->
            stateContext.currentState = AcceptedSharing(
                stateContext,
                context,
                settingsLauncherWrapper,
                currentState.intentData,
                currentState.geoUri,
            )
            transition()
        }
    }

    fun skip(context: Context, settingsLauncherWrapper: ManagedActivityResultLauncherWrapper) {
        assert(stateContext.currentState is HasResult)
        (stateContext.currentState as HasResult).let { currentState ->
            stateContext.currentState = AcceptedSharing(
                stateContext,
                context,
                settingsLauncherWrapper,
                currentState.intentData,
                currentState.geoUri,
            )
            transition()
        }
    }

    fun copy(context: Context, clipboard: Clipboard) {
        viewModelScope.launch {
            assert(stateContext.currentState is HasResult)
            (stateContext.currentState as HasResult).let { currentState ->
                stateContext.clipboardTools.setPlainText(clipboard, "geo: URI", currentState.geoUri)
                val systemHasClipboardEditor = stateContext.getBuildVersionSdkInt() >= Build.VERSION_CODES.TIRAMISU
                if (!systemHasClipboardEditor) {
                    Toast.makeText(context, R.string.copying_finished, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        }
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
            transition()
        }
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
