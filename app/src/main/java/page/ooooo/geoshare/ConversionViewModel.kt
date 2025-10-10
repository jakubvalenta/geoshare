package page.ooooo.geoshare

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import page.ooooo.geoshare.data.local.preferences.Automation
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

    fun runAutomation(context: Context, clipboard: Clipboard, saveLauncher: ActivityResultLauncher<Intent>) {
        assert(stateContext.currentState is AutomationReady)
        viewModelScope.launch {
            stateContext.currentState = (stateContext.currentState as AutomationReady).run(
                onCopy = { text -> copy(context, clipboard, text) },
                onOpenApp = { packageName, uriString -> openApp(context, packageName, uriString) },
                onOpenChooser = { uriString -> openChooser(context, uriString) },
                onSave = { launchSave(context, saveLauncher) },
            )
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

    fun openApp(context: Context, packageName: String, uriString: String): Boolean =
        stateContext.intentTools.openApp(context, packageName, uriString)

    fun openChooser(context: Context, uriString: String): Boolean =
        stateContext.intentTools.openChooser(context, uriString)

    fun queryGeoUriApps(packageManager: PackageManager): List<IntentTools.App> =
        stateContext.intentTools.queryGeoUriApps(packageManager)

    fun showOpenByDefaultSettings(context: Context, settingsLauncher: ActivityResultLauncher<Intent>) {
        stateContext.intentTools.showOpenByDefaultSettings(context, settingsLauncher)
    }

    fun isDefaultHandlerEnabled(packageManager: PackageManager, uriString: String): Boolean =
        stateContext.intentTools.isDefaultHandlerEnabled(packageManager, uriString)

    fun showOpenByDefaultSettingsForPackage(
        context: Context,
        settingsLauncher: ActivityResultLauncher<Intent>,
        packageName: String,
    ) {
        stateContext.intentTools.showOpenByDefaultSettingsForPackage(context, settingsLauncher, packageName)
    }

    fun launchSave(context: Context, saveLauncher: ActivityResultLauncher<Intent>) {
        @Suppress("SpellCheckingInspection") val timestamp =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(System.currentTimeMillis())
        val filename = context.resources.getString(
            R.string.conversion_succeeded_save_gpx_filename,
            context.resources.getString(R.string.app_name),
            timestamp,
        )
        saveLauncher.launch(
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, filename)
            }
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
