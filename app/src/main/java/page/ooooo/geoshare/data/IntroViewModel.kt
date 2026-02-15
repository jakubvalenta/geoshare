package page.ooooo.geoshare.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val shown: StateFlow<Boolean> =
        userPreferencesRepository.values
            .map { values -> values.introShownForVersionCode != IntroShowForVersionCodePreference.default }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                true,
            )

    fun setShown() {
        viewModelScope.launch {
            userPreferencesRepository.setValue(
                IntroShowForVersionCodePreference,
                BuildConfig.VERSION_CODE,
            )
        }
    }
}
