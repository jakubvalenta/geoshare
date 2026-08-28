package page.ooooo.geoshare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.DismissedHelpMessagesPreference
import page.ooooo.geoshare.lib.Message
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message.asStateFlow()

    val dismissedHelpMessages: StateFlow<Set<HelpMessage>?> = userPreferencesRepository.values
        .map { it.dismissedHelpMessages }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    fun dismissHelpMessage(helpMessage: HelpMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.edit { preferences ->
                DismissedHelpMessagesPreference.setValue(
                    preferences,
                    DismissedHelpMessagesPreference.getValue(preferences).orEmpty() + helpMessage,
                )
            }
        }
    }
}
