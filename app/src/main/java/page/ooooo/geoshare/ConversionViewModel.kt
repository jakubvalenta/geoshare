package page.ooooo.geoshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.*
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.SavableDelegate
import page.ooooo.geoshare.lib.conversion.*
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.allOutputGroups
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val intentTools = IntentTools()
    val stateContext = ConversionStateContext(
        inputs = allInputs,
        intentTools = intentTools,
        userPreferencesRepository = userPreferencesRepository,
    ) { newState ->
        _currentState.value = newState
        when (newState) {
            is ConversionState.HasLoadingIndicator -> {
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

    private val _intent = MutableStateFlow<Intent?>(null)
    val intent: StateFlow<Intent?> = _intent

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

    fun start(runContext: ConversionRunContext) {
        stateContext.currentState = ReceivedUriString(stateContext, runContext, inputUriString)
        transition()
    }

    fun start(runContext: ConversionRunContext, intent: Intent) {
        stateContext.currentState = ReceivedIntent(stateContext, runContext, intent)
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

    fun saveGpx(context: Context, result: ActivityResult) {
        (stateContext.currentState as? ConversionState.HasResult)?.let { currentState ->
            result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = outputStream.writer()
                    val saveGpxAction = allOutputGroups.firstNotNullOfOrNull { outputGroup ->
                        outputGroup.getActionOutputs()
                            .firstNotNullOfOrNull { it.getAction(currentState.position) as? Action.SaveGpx }
                    }
                    saveGpxAction?.write(writer)
                    writer.close()
                }
            }
        }
    }

    fun cancel() {
        transitionJob?.cancel()
    }

    fun setIntent(value: Intent) {
        _intent.value = value
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

    fun runAction(runContext: ConversionRunContext, action: Action) {
        viewModelScope.launch {
            try {
                val success = action.run(intentTools, runContext)
                if (success) {
                    if (action is Action.Copy) {
                        val systemHasClipboardEditor = VERSION.SDK_INT >= VERSION_CODES.TIRAMISU
                        if (!systemHasClipboardEditor) {
                            Toast.makeText(
                                runContext.context,
                                R.string.copying_finished,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                } else {
                    if (action is Action.OpenApp) {
                        Toast.makeText(
                            runContext.context, runContext.context.resources.getString(
                                R.string.conversion_automation_open_app_failed,
                                intentTools.queryApp(runContext.context.packageManager, action.packageName)?.label
                                    ?: action.packageName,
                            ), Toast.LENGTH_SHORT
                        ).show()
                    } else if (action is Action.OpenChooser) {
                        Toast.makeText(
                            runContext.context,
                            R.string.conversion_succeeded_apps_not_found,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            } catch (tr: Exception) {
                stateContext.log.e(null, "Exception while running action action", tr)
            }
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
