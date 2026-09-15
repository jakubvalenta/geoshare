package page.ooooo.geoshare.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.lib.android.AppActivity
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.getPackageNames
import page.ooooo.geoshare.lib.android.queryActivitiesForUri
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OutputViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    appsRepository: AppsRepository,
    linkRepository: LinkRepository,
    private val outputRepository: OutputRepository,
    userPreferencesRepository: UserPreferencesRepository,
    val coordinateConverter: CoordinateConverter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedUriString: MutableStateFlow<String?> =
        savedStateHandle.getMutableStateFlow("selectedUri", null)
    val selectedUriString: StateFlow<String?> = _selectedUriString.asStateFlow()

    val activities: StateFlow<List<AppActivity>> = appsRepository.activities
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    val activitiesForSelectedUriString: StateFlow<List<AppActivity>> = selectedUriString
        .filterNotNull()
        .map { context.packageManager.queryActivitiesForUri(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val appDetails: StateFlow<AppDetails> = appsRepository.appDetails
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )
    val appDetailsForSelectedUriString: StateFlow<AppDetails> = activitiesForSelectedUriString
        .filter { it.isNotEmpty() }
        .map { context.packageManager.queryAppDetails(it.getPackageNames()) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )

    val outputsForPoint: StateFlow<List<PointOutput>> =
        linkRepository.all.map { outputRepository.getOutputsForPoint(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPoints: StateFlow<List<PointsOutput>> =
        flow { emit(outputRepository.getOutputsForPoints()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointChips: StateFlow<List<PointOutput>> =
        linkRepository.all.map { outputRepository.getOutputsForPointChips(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointsChips: StateFlow<List<PointsOutput>> =
        flow { emit(outputRepository.getOutputsForPointsChips()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForApps: StateFlow<Map<String, List<Output>>> =
        appsRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { apps, hiddenApps ->
                outputRepository.getOutputsForApps(apps, hiddenApps)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val outputsForLinks: StateFlow<Map<String?, List<Output>>> =
        linkRepository.all.map { links -> outputRepository.getOutputsForLinks(links) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val outputsForSharing: StateFlow<List<Output>> =
        flow { emit(outputRepository.getOutputsForSharing()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )

    suspend fun getAutomationOutput(automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?): Output? =
        outputRepository.getAutomationOutput(automation, getLinkByUUID)

    fun setSelectedUriString(newSelectedUriString: String?) {
        _selectedUriString.value = newSelectedUriString
    }
}
