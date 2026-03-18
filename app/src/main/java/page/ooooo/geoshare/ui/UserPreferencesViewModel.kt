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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.Message
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    appsRepository: AppsRepository,
    linkRepository: LinkRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

    val apps = appsRepository.apps
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )
    val appDetails = appsRepository.appDetails
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )
    val links = linkRepository.all
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    val values: StateFlow<UserPreferencesValues> = userPreferencesRepository.values
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesValues(),
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
                (HiddenAppsPreference.getValue(preferences) ?: emptySet()) + packageName,
            )
        }
        _message.value = Message(resources.getString(R.string.user_preferences_apps_message_hidden))
    }

    fun dismissMessage() {
        _message.value = null
    }
}
