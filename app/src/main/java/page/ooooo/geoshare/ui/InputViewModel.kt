package page.ooooo.geoshare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.lib.inputs.InputChangelogItem
import page.ooooo.geoshare.lib.inputs.InputGroup
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    val inputRepository: InputRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val allChangelogsByGroupFlow: Flow<Map<InputGroup, ImmutableList<InputChangelogItem>>> = flow {
        emit(
            inputRepository.all
                .mapNotNull { input -> input.group?.let { group -> group to input } }
                .groupBy { (group) -> group }
                .mapValues { (_, inputs) -> inputs.flatMap { (_, input) -> input.changelog }.toImmutableList() }
        )
    }
    val allChangelogsByGroup: StateFlow<Map<InputGroup, ImmutableList<InputChangelogItem>>> =
        allChangelogsByGroupFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val recentChangelogsByGroup: StateFlow<Map<InputGroup, ImmutableList<InputChangelogItem>>> =
        userPreferencesRepository.values
            .mapNotNull { values -> values.changelogShownForVersionCode }
            .combine(allChangelogsByGroupFlow) { changelogShownForVersionCode, allGroups ->
                allGroups.filterValues { changelog ->
                    changelog.any { it.addedInVersionCode > changelogShownForVersionCode }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val changelogShown: StateFlow<Boolean> =
        recentChangelogsByGroup
            .map { it.isEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                true,
            )

    fun setChangelogShown() {
        val newestInputAddedInVersionCode = inputRepository.all
            .maxOfOrNull { input -> input.changelog.maxOfOrNull { it.addedInVersionCode } ?: BuildConfig.VERSION_CODE }
            ?: BuildConfig.VERSION_CODE
        viewModelScope.launch {
            userPreferencesRepository.setValue(
                ChangelogShownForVersionCodePreference,
                newestInputAddedInVersionCode,
            )
        }
    }
}
