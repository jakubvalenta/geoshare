package page.ooooo.geoshare.ui

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.toOutput
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.components.IconDescriptor
import java.util.UUID
import javax.inject.Inject

data class AutomationOutput(
    val automation: Automation,
    val output: Output,
) {
    companion object {
        val Noop = AutomationOutput(NoopAutomation, NoopOutput())
    }
}

data class AutomationDetail(
    private val appLabel: String?,
    val icon: IconDescriptor?,
    val automation: Automation,
    val output: Output,
) {
    @Composable
    fun automationLabel(): String = output.automationLabel(appLabel)
}

data class HiddenAppDetail(
    val appLabel: String?,
    val icon: Drawable?,
    val packageName: String,
)

data class HiddenAppsSize(
    val total: Int,
    val visible: Int,
)

@HiltViewModel
class UserPreferenceViewModel @Inject constructor(
    appsRepository: AppsRepository,
    linkRepository: LinkRepository,
    private val outputRepository: OutputRepository,
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

    private val automationOutput: Flow<AutomationOutput> =
        userPreferencesRepository.values
            .map { it.automation }
            .distinctUntilChanged()
            .combine(linkRepository.all) { automation, links ->
                outputRepository.getAutomationOutput(automation) { links.findByUUID(it) }
                    ?.let { AutomationOutput(automation, it) }
                    ?: AutomationOutput.Noop
            }
    val automationDetail: StateFlow<AutomationDetail> =
        automationOutput
            .combine(appsRepository.appDetails) { automation, appDetails ->
                automation.toAutomationDetail(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AutomationOutput.Noop.toAutomationDetail(emptyMap()),
            )

    private val automationOutputs: Flow<List<List<AutomationOutput>>> =
        combine(
            appsRepository.activities,
            appsRepository.appDetails,
            userPreferencesRepository.values
                .map { it.hiddenApps }
                .distinctUntilChanged(),
            linkRepository.all,
        ) { activities, appDetails, hiddenApps, links ->
            AutomationPreference.getOptionGroups(activities, appDetails, hiddenApps, links)
                .map { group ->
                    group.map { automation ->
                        outputRepository.getAutomationOutput(automation) { links.findByUUID(it) }
                            .toAutomationOutput(automation)
                    }
                }
        }
    val automationDetails: StateFlow<List<List<AutomationDetail>>> =
        automationOutputs
            .combine(appsRepository.appDetails) { automationOutputs, appDetails ->
                automationOutputs.toAutomationDetails(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val hiddenAppsDetails: StateFlow<List<HiddenAppDetail>> =
        appsRepository.activities
            .map { activities -> HiddenAppsPreference.getOptions(activities) }
            .combine(appsRepository.appDetails) { hiddenApps, appDetails ->
                hiddenApps.toHiddenAppsDetails(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val hiddenAppsSize: StateFlow<HiddenAppsSize> =
        appsRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps ->
                HiddenAppsSize(
                    total = activities.size,
                    visible = activities.size.minus(hiddenApps?.size ?: 0),
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                HiddenAppsSize(total = 0, visible = 0),
            )

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

fun Automation.toAutomationOutput(
    coordinateConverter: CoordinateConverter,
    log: Log = DefaultLog,
    getLinkByUUID: (linkUUID: UUID) -> Link?,
): AutomationOutput =
    toOutput(coordinateConverter, log, getLinkByUUID)
        .toAutomationOutput(this)

fun List<List<Automation>>.toAutomationOutputs(
    coordinateConverter: CoordinateConverter,
    log: Log = DefaultLog,
    getLinkByUUID: (linkUUID: UUID) -> Link?,
): List<List<AutomationOutput>> =
    map { group ->
        group.map { automation ->
            automation.toAutomationOutput(coordinateConverter, log, getLinkByUUID)
        }
    }

fun Output?.toAutomationOutput(automation: Automation): AutomationOutput =
    if (this != null) {
        AutomationOutput(automation, this)
    } else {
        AutomationOutput.Noop
    }

fun AutomationOutput.toAutomationDetail(appDetails: AppDetails): AutomationDetail =
    AutomationDetail(
        appLabel = output.getAppLabel(appDetails),
        icon = output.getIcon(appDetails),
        automation = automation,
        output = output,
    )

fun List<List<AutomationOutput>>.toAutomationDetails(appDetails: AppDetails): List<List<AutomationDetail>> =
    map { groups ->
        groups.map { automation ->
            automation.toAutomationDetail(appDetails)
        }
    }

fun Set<String>.toHiddenAppsDetails(appDetails: AppDetails): List<HiddenAppDetail> =
    map { packageName ->
        appDetails[packageName].let { appDetail ->
            HiddenAppDetail(
                appLabel = appDetail?.label,
                icon = appDetail?.icon,
                packageName = packageName,
            )
        }
    }
