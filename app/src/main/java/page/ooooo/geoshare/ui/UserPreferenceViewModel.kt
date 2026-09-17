package page.ooooo.geoshare.ui

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
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
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.ActivityAutomation
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BasicAutomation
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.LinkAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.toOutput
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.components.IconDescriptor
import javax.inject.Inject

data class AutomationDetail(
    private val appDetail: AppDetail?,
    val automation: Automation,
    val icon: IconDescriptor?,
    val output: Output,
) {
    @Composable
    fun label(): String = output.automationLabel(appDetail)

    @Composable
    fun description(): String? = output.automationDescription()
}

data class HiddenAppDetail(
    val label: String?,
    val icon: Drawable?,
    val packageName: String,
)

data class HiddenAppsSize(
    val total: Int,
    val visible: Int,
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

    val automationDetail: StateFlow<AutomationDetail> =
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
                automation to output
            }
            .combine(appRepository.appDetails) { (automation, output), appDetails ->
                output.toAutomationDetail(automation, appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                NoopOutput.toAutomationDetail(NoopAutomation, emptyMap()),
            )
    val automationDetails: StateFlow<List<List<AutomationDetail>>> =
        combine(
            appRepository.activities,
            appRepository.appDetails,
            userPreferencesRepository.values
                .map { it.hiddenApps }
                .distinctUntilChanged(),
            linkRepository.all,
        ) { activities, appDetails, hiddenApps, links ->
            AutomationPreference.getOptionGroups(activities, appDetails, hiddenApps, links)
                .map { group ->
                    group.map { automation ->
                        when (automation) {
                            is BasicAutomation -> automation.toOutput(coordinateConverter)
                            is ActivityAutomation -> automation.toOutput(coordinateConverter, log)
                            is LinkAutomation -> links.findByUUID(automation.linkUUID)?.let { link ->
                                automation.toOutput(coordinateConverter, link)
                            }
                        }.toAutomationDetail(automation, appDetails)
                    }
                }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val hiddenAppsDetails: StateFlow<List<HiddenAppDetail>> =
        appRepository.activities
            .map { activities -> HiddenAppsPreference.getOptions(activities) }
            .combine(appRepository.appDetails) { hiddenApps, appDetails ->
                hiddenApps.toHiddenAppsDetails(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val hiddenAppsSize: StateFlow<HiddenAppsSize> =
        appRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps ->
                HiddenAppsSize(
                    total = activities.size,
                    visible = (activities.map { it.packageName }.toSet() - (hiddenApps ?: emptySet())).size,
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

fun Output?.toAutomationDetail(automation: Automation, appDetails: AppDetails): AutomationDetail =
    (this ?: NoopOutput).run {
        (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
            AutomationDetail(
                appDetail = appDetail,
                automation = automation,
                icon = getIcon(appDetail),
                output = this,
            )
        }
    }

fun Set<String>.toHiddenAppsDetails(appDetails: AppDetails): List<HiddenAppDetail> =
    map { packageName ->
        appDetails[packageName].let { appDetail ->
            HiddenAppDetail(
                label = appDetail?.label,
                icon = appDetail?.icon,
                packageName = packageName,
            )
        }
    }
