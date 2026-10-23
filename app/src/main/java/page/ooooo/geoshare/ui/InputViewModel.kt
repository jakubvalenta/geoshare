package page.ooooo.geoshare.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.inputs.InputChangelogItem
import page.ooooo.geoshare.data.inputs.InputGroup
import page.ooooo.geoshare.data.preferences.ChangelogShownForVersionCodePreference
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inputRepository: InputRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val allChangelogsByGroupFlow: Flow<Map<InputGroup, ImmutableList<InputChangelogItem>>> =
        flow {
            emit(
                inputRepository.all
                    .groupBy { input -> input.group }
                    .mapValues { (_, inputs) -> inputs.flatMap { input -> input.changelog }.toImmutableList() }
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
    val placeholderUris: StateFlow<List<String>> =
        flow {
            emit(
                inputRepository.genPlaceholderUris(
                    WGS84Point(
                        NaivePoint.genRandomPoint(
                            name = context.resources.getString(R.string.intro_how_to_share_google_maps_screenshot_place)
                        )
                    )
                )
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
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
