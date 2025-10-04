package page.ooooo.geoshare

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.compose.ui.platform.ClipEntry
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
import page.ooooo.geoshare.data.local.preferences.changelogShownForVersionCode
import page.ooooo.geoshare.data.local.preferences.lastRunVersionCode
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

    data class App(val packageName: String, val label: String, val icon: Drawable)

    sealed class MapServiceFilter(val titleResId: Int) {
        class All : MapServiceFilter(R.string.supported_uris_filter_all)
        class Recent : MapServiceFilter(R.string.supported_uris_filter_recent)
        class Enabled : MapServiceFilter(R.string.supported_uris_default_handler_enabled)
        class Disabled : MapServiceFilter(R.string.supported_uris_default_handler_disabled)
    }

    data class MapServiceInput(
        // TODO Create MapServiceInput.Text and MapServiceInput.Url
        val supportedInput: SupportedInput,
        val defaultHandlerEnabled: Boolean?,
    )

    data class MapService(
        val urlConverter: UrlConverter,
        val inputs: List<MapServiceInput>,
    )

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

    val mapServiceFilters = listOf(
        MapServiceFilter.All(),
        MapServiceFilter.Recent(),
        MapServiceFilter.Enabled(),
        MapServiceFilter.Disabled(),
    )

    val stateContext = ConversionStateContext(
        urlConverters = urlConverters,
        intentTools = IntentTools(),
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
    val changelogShown: StateFlow<Boolean> = userPreferencesValues.mapLatest {
        it.changelogShownForVersionCodeValue != changelogShownForVersionCode.default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        userPreferencesValues.value.changelogShownForVersionCodeValue != changelogShownForVersionCode.default,
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

    fun getMapServices(packageManager: PackageManager, filter: MapServiceFilter): List<MapService> =
        urlConverters.mapNotNull { urlConverter ->
            urlConverter.supportedInputs.mapNotNull { supportedInput ->
                if (filter is MapServiceFilter.Recent && supportedInput.addedInVersionCode <= 10) {
                    return@mapNotNull null
                }
                val defaultHandlerEnabled = when (supportedInput) {
                    is SupportedInput.Url -> isDefaultHandlerEnabled(packageManager, supportedInput.urlString)
                    else -> null
                }
                when (filter) {
                    is MapServiceFilter.Enabled -> {
                        if (defaultHandlerEnabled == false) {
                            return@mapNotNull null
                        }
                    }

                    is MapServiceFilter.Disabled -> {
                        if (defaultHandlerEnabled != false) {
                            return@mapNotNull null
                        }
                    }

                    else -> Unit
                }
                MapServiceInput(supportedInput, defaultHandlerEnabled)
            }.takeIf { it.isNotEmpty() }?.let { mapServiceInputs ->
                MapService(urlConverter, mapServiceInputs)
            }
        }

    fun isDefaultHandlerEnabled(packageManager: PackageManager, uriString: String): Boolean {
        val resolveInfo = try {
            packageManager.resolveActivity(
                Intent(Intent.ACTION_VIEW, uriString.toUri()),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        } catch (e: Exception) {
            Log.e(null, "Error when querying which app is the default handler for a URI", e)
            return false
        }
        val packageName = try {
            resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.e(null, "Error when loading info about an app that is the default handler for URI", e)
            null
        }
        return packageName == BuildConfig.APPLICATION_ID
    }

    fun queryGeoUriApps(packageManager: PackageManager): List<App> {
        val resolveInfos = try {
            packageManager.queryIntentActivities(
                Intent(Intent.ACTION_VIEW, "geo:".toUri()),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        } catch (e: Exception) {
            Log.e(null, "Error when querying apps that support geo: URIs", e)
            return emptyList()
        }
        return resolveInfos.mapNotNull {
            try {
                App(
                    it.activityInfo.packageName,
                    it.activityInfo.loadLabel(packageManager).toString(),
                    it.activityInfo.loadIcon(packageManager),
                )
            } catch (e: Exception) {
                Log.e(null, "Error when loading info about an app that supports geo: URIs", e)
                null
            }
        }.filterNot { it.packageName == BuildConfig.APPLICATION_ID }.sortedBy { it.label }
    }

    fun openApp(context: Context, packageName: String, uriString: String) {
        try {
            context.startActivity(
                stateContext.intentTools.createViewIntent(packageName, uriString.toUri())
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.conversion_succeeded_apps_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun openChooser(context: Context, uriString: String) {
        try {
            context.startActivity(
                stateContext.intentTools.createChooserIntent(uriString.toUri())
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.conversion_succeeded_apps_not_found, Toast.LENGTH_SHORT).show()
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

    fun showOpenByDefaultSettings(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
    ) {
        showOpenByDefaultSettingsForPackage(context, launcher, BuildConfig.APPLICATION_ID)
    }

    fun showOpenByDefaultSettingsForPackage(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
        packageName: String,
    ) {
        try {
            val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                // Samsung supposedly doesn't allow going to the "Open by default" settings page.
                Build.MANUFACTURER.lowercase(Locale.ROOT) != "samsung"
            ) {
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            } else {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            }
            val intent = Intent(action, "package:$packageName".toUri())
            launcher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.intro_settings_activity_not_found,
                Toast.LENGTH_LONG,
            ).show()
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

    fun setChangelogShown() {
        setUserPreferenceValue(changelogShownForVersionCode, BuildConfig.VERSION_CODE)
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
