package page.ooooo.geoshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
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
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.*
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val urlConverters = listOf(
        GeoUrlConverter(),
        GoogleMapsUrlConverter(),
        AppleMapsUrlConverter(),
        HereWeGoUrlConverter(),
        MagicEarthUrlConverter(),
        MapyComUrlConverter(),
        OpenStreetMapUrlConverter(),
        OsmAndUrlConverter(),
        WazeUrlConverter(),
        YandexMapsUrlConverter(),
        CoordinatesUrlConverter(),
    )
    val intentTools = IntentTools()
    val stateContext = ConversionStateContext(
        urlConverters = urlConverters,
        intentTools = intentTools,
        userPreferencesRepository = userPreferencesRepository,
    ) { newState ->
        _currentState.value = newState
        when (newState) {
            is HasLoadingIndicator -> {
                loadingIndicatorJob?.cancel()
                loadingIndicatorJob = viewModelScope.launch {
                    // Show loading indicator only if the state lasts longer than 200ms.
                    delay(200L)
                    _loadingIndicatorTitleResId.value = newState.loadingIndicatorTitleResId
                }
            }

            else -> {
                loadingIndicatorJob?.cancel()
                loadingIndicatorJob = viewModelScope.launch {
                    // Hide loading indicator only if another loading indicator is not shown within the next 200ms.
                    delay(200L)
                    _loadingIndicatorTitleResId.value = null
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

    private val _loadingIndicatorTitleResId = MutableStateFlow<Int?>(null)
    val loadingIndicatorTitleResId: StateFlow<Int?> = _loadingIndicatorTitleResId

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
        page.ooooo.geoshare.data.local.preferences.automation.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val changelogShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.changelogShownForVersionCodeValue?.let { changelogShownForVersionCodeValue ->
            urlConverters.all { urlConverter ->
                urlConverter.documentation.inputs.all { input ->
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
        page.ooooo.geoshare.data.local.preferences.changelogShownForVersionCode.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val introShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.introShownForVersionCodeValue != page.ooooo.geoshare.data.local.preferences.introShowForVersionCode.default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        userPreferencesValues.value.introShownForVersionCodeValue != page.ooooo.geoshare.data.local.preferences.introShowForVersionCode.default,
    )

    fun start(runContext: ConversionRunContext) {
        stateContext.currentState = ReceivedUriString(stateContext, runContext, inputUriString)
        transition()
    }

    fun start(runContext: ConversionRunContext, intent: Intent) {
        stateContext.currentState = ReceivedIntent(stateContext, runContext, intent)
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

    fun saveGpx(context: Context, result: ActivityResult) {
        result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.writer()
                if (stateContext.currentState is HasResult) {
                    (stateContext.currentState as HasResult).position.toGpx(writer)
                }
                writer.close()
            }
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
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
            transition()
        }
    }

    fun setChangelogShownForVersionCode(value: Int) {
        setUserPreferenceValue(
            page.ooooo.geoshare.data.local.preferences.changelogShownForVersionCode,
            value,
        )
    }

    fun setIntroShown() {
        setUserPreferenceValue(
            page.ooooo.geoshare.data.local.preferences.introShowForVersionCode,
            BuildConfig.VERSION_CODE,
        )
    }

    fun <T> setUserPreferenceValue(userPreference: UserPreference<T>, value: T) {
        viewModelScope.launch {
            userPreferencesRepository.setValue(userPreference, value)
        }
    }
}
