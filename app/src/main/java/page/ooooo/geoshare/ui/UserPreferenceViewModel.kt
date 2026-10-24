package page.ooooo.geoshare.ui

import android.content.res.Resources
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.database.findByUUID
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.message.Message
import page.ooooo.geoshare.data.outputs.NoopOutput
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.preferences.ActivityAutomation
import page.ooooo.geoshare.data.preferences.Automation
import page.ooooo.geoshare.data.preferences.AutomationPreference
import page.ooooo.geoshare.data.preferences.BasicAutomation
import page.ooooo.geoshare.data.preferences.CoordinateFormat
import page.ooooo.geoshare.data.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.preferences.LinkAutomation
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.toOutput
import javax.inject.Inject

data class AutomationOption(
    val automation: Automation = NoopAutomation,
    val output: Output<*> = NoopOutput,
)

data class SelectedAutomationOptionState(
    val appDetails: AppDetails = emptyMap(),
    val option: AutomationOption = AutomationOption(),
)

data class AutomationOptionsState(
    val appDetails: AppDetails = emptyMap(),
    val optionGroups: List<List<AutomationOption>> = emptyList(),
)

data class SelectedHiddenAppsState(
    val totalCount: Int = 0,
    val visibleCount: Int = 0,
)

data class HiddenAppsOptionsState(
    val appDetails: AppDetails = emptyMap(),
    val hiddenApps: Set<String> = emptySet(),
)

@HiltViewModel
class UserPreferenceViewModel @Inject constructor(
    appRepository: AppRepository,
    linkRepository: LinkRepository,
    private val coordinateConverter: CoordinateConverter,
    private val log: Log,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message.asStateFlow()

    val values: StateFlow<UserPreferencesValues> =
        userPreferencesRepository.values
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UserPreferencesValues(),
            )

    // Automation

    val selectedAutomationOption: StateFlow<SelectedAutomationOptionState> =
        userPreferencesRepository.values
            .map { it.automation }
            .distinctUntilChanged()
            .combine(linkRepository.all) { automation, links ->
                val output = when (automation) {
                    is BasicAutomation -> automation.toOutput(coordinateConverter)
                    is ActivityAutomation -> automation.toOutput(coordinateConverter, log)
                    is LinkAutomation -> links.findByUUID(automation.linkUUID)?.let { link ->
                        automation.toOutput(coordinateConverter, link)
                    }
                } ?: NoopOutput
                AutomationOption(automation, output)
            }
            .combine(appRepository.appDetails) { option, appDetails ->
                SelectedAutomationOptionState(appDetails, option)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SelectedAutomationOptionState(),
            )
    val automationOptions: StateFlow<AutomationOptionsState> =
        combine(
            appRepository.activities,
            userPreferencesRepository.values
                .map { it.hiddenApps }
                .distinctUntilChanged(),
            linkRepository.all,
        ) { activities, hiddenApps, links ->
            AutomationPreference.getOptionGroups(activities, hiddenApps, links)
                .map { group ->
                    group.map { automation ->
                        AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                    }
                }
        }
            .combine(appRepository.appDetails) { optionGroups, appDetails ->
                AutomationOptionsState(appDetails, optionGroups)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AutomationOptionsState(),
            )

    // Coordinate format

    val coordinateFormat: StateFlow<CoordinateFormat> =
        userPreferencesRepository.values
            .map { it.coordinateFormat }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CoordinateFormat.DEC,
            )

    // Hidden apps

    val selectedHiddenApps: StateFlow<SelectedHiddenAppsState> =
        appRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps ->
                SelectedHiddenAppsState(
                    totalCount = activities.size,
                    visibleCount = (activities.map { it.packageName }.toSet() - (hiddenApps ?: emptySet())).size,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SelectedHiddenAppsState(),
            )
    val hiddenAppsOptions: StateFlow<HiddenAppsOptionsState> =
        appRepository.activities
            .map { activities -> HiddenAppsPreference.getOptions(activities) }
            .combine(appRepository.appDetails) { hiddenApps, appDetails ->
                HiddenAppsOptionsState(appDetails, hiddenApps)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                HiddenAppsOptionsState(),
            )

    // Methods

    fun editUserPreferences(transform: (preferences: MutablePreferences) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.edit(transform)
        }
    }

    fun hideApp(resources: Resources, packageName: String) {
        editUserPreferences { preferences ->
            HiddenAppsPreference.setValue(
                preferences,
                HiddenAppsPreference.getValue(preferences).orEmpty() + packageName,
            )
        }
        _message.value = Message(resources.getString(R.string.user_preferences_apps_message_hidden))
    }

    fun dismissMessage() {
        _message.value = null
    }
}

fun Automation.toOutput(coordinateConverter: CoordinateConverter, links: List<Link>, log: Log): Output<*> =
    when (this) {
        is BasicAutomation -> toOutput(coordinateConverter)
        is ActivityAutomation -> toOutput(coordinateConverter, log)
        is LinkAutomation -> links.findByUUID(linkUUID)?.let { link ->
            toOutput(coordinateConverter, link)
        }
    } ?: NoopOutput
