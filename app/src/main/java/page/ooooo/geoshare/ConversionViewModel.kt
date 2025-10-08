package page.ooooo.geoshare

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
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
import page.ooooo.geoshare.data.local.preferences.AutomaticAction
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val intentTools = IntentTools()
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

    val stateContext = ConversionStateContext(
        urlConverters = urlConverters,
        intentTools = intentTools,
        networkTools = NetworkTools(),
        userPreferencesRepository = userPreferencesRepository,
        onStateChange = { newState ->
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
        })

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
    val automaticAction: StateFlow<AutomaticAction> = userPreferencesValues.mapLatest {
        it.automaticActionValue
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        page.ooooo.geoshare.data.local.preferences.automaticAction.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val lastInputShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.lastInputVersionCodeValue?.let { lastInputVersionCode ->
            urlConverters.all { urlConverter ->
                urlConverter.documentation.inputs.all { input ->
                    input.addedInVersionCode <= lastInputVersionCode
                }
            }
        } ?: true
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val lastInputVersionCode: StateFlow<Int?> = userPreferencesValues.mapLatest {
        it.lastInputVersionCodeValue
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        page.ooooo.geoshare.data.local.preferences.lastInputVersionCode.default,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val introShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.introShownForVersionCodeValue != page.ooooo.geoshare.data.local.preferences.lastRunVersionCode.default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        userPreferencesValues.value.introShownForVersionCodeValue != page.ooooo.geoshare.data.local.preferences.lastRunVersionCode.default,
    )

    fun start() {
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

    fun copy(context: Context, clipboard: Clipboard, text: String) {
        viewModelScope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Geographic coordinates", text)))
            val systemHasClipboardEditor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            if (!systemHasClipboardEditor) {
                Toast.makeText(context, R.string.copying_finished, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchSave(context: Context, launcher: ActivityResultLauncher<Intent>) {
        @Suppress("SpellCheckingInspection") val timestamp =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(System.currentTimeMillis())
        val filename = context.resources.getString(
            R.string.conversion_succeeded_save_gpx_filename,
            context.resources.getString(R.string.app_name),
            timestamp,
        )
        launcher.launch(
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, filename)
            },
        )
    }

    fun save(context: Context, result: ActivityResult) {
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

    fun updateInput(newUriString: String) {
        withMutableSnapshot {
            inputUriString = newUriString
        }
        if (stateContext.currentState !is Initial) {
            stateContext.currentState = Initial()
            transition()
        }
    }

    fun setLastInputVersionCode(newLastInputVersionCode: Int) {
        setUserPreferenceValue(
            page.ooooo.geoshare.data.local.preferences.lastInputVersionCode,
            newLastInputVersionCode,
        )
    }

    fun setIntroShown() {
        setUserPreferenceValue(page.ooooo.geoshare.data.local.preferences.lastRunVersionCode, BuildConfig.VERSION_CODE)
    }

    fun <T> setUserPreferenceValue(userPreference: UserPreference<T>, value: T) {
        viewModelScope.launch {
            userPreferencesRepository.setValue(userPreference, value)
        }
    }
}
