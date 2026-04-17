package page.ooooo.geoshare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.InputRepository
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    private val inputRepository: InputRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val allInputs = inputRepository.all

    private val allDocumentationsFlow = flow {
        emit(inputRepository.all.map { input -> input.documentation })
    }

    val allDocumentations = allDocumentationsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    val recentDocumentations = userPreferencesRepository.values
        .mapNotNull { values -> values.changelogShownForVersionCode }
        .combine(allDocumentationsFlow) { changelogShownForVersionCode, documentations ->
            documentations.filter { documentation ->
                documentation.items.any { it.addedInVersionCode > changelogShownForVersionCode }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    val changelogShown = recentDocumentations
        .map { it.isEmpty() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true,
        )

    fun setChangelogShown() {
        val newestInputAddedInVersionCode = inputRepository.all.maxOfOrNull { input ->
            input.documentation.items.maxOfOrNull { it.addedInVersionCode } ?: BuildConfig.VERSION_CODE
        } ?: BuildConfig.VERSION_CODE
        viewModelScope.launch {
            userPreferencesRepository.setValue(
                ChangelogShownForVersionCodePreference,
                newestInputAddedInVersionCode,
            )
        }
    }
}
