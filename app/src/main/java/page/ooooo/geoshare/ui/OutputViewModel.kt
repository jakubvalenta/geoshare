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
import page.ooooo.geoshare.lib.android.isMessagingApp
import page.ooooo.geoshare.lib.android.queryActivitiesForUri
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.components.IconDescriptor
import java.util.UUID
import javax.inject.Inject

/**
 * An [output] with resolved [icon].
 *
 * Example: "Copy coordinates" output
 */
data class OutputState<T : Output>(
    val icon: IconDescriptor?,
    val menuIcon: IconDescriptor?,
    val output: T,
)

/**
 * All [outputStates] that can be executed for an app specified by its [packageName].
 *
 * - [defaultOutputState] is the output that will be executed when clicking the icon in the UI.
 * - [outputStates] are the outputs that will be shown in a context menu in the UI.
 *
 * Example: OsmAnd with the "Open point" and "Launch navigation" outputs
 */
data class OutputStatesForApp(
    val packageName: String,
    val label: String,
    val defaultOutputState: OutputState<Output>,
    val outputStates: List<OutputState<Output>>,
)

data class OutputStatesForAppsByCategory(
    val mapApps: List<OutputStatesForApp>,
    val messagingApps: List<OutputStatesForApp>,
)

/**
 * All [outputStates] that can be executed for a link specified by its [group].
 *
 * - [defaultOutputState] is the output that will be executed when clicking the icon in the UI.
 * - [outputStates] are the outputs that will be shown in a context menu in the UI.
 *
 * Example: OsmAnd with the "Open point" and "Launch navigation" outputs
 */
data class OutputStatesForLink(
    val group: String,
    val defaultOutputState: OutputState<Output>,
    val outputStates: List<OutputState<Output>>,
)

/**
 * All [outputStates] that can be executed for the share icon.
 *
 * - [defaultOutputState] is the output that will be executed when clicking the icon in the UI.
 * - [outputStates] are the outputs that will be shown in a context menu in the UI.
 *
 * Example: "Share", "Navigate", and "Share GPX route" outputs
 */
data class OutputStatesForSharing(
    val defaultOutputState: OutputState<Output>,
    val outputStates: List<OutputState<Output>>,
)

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

    val outputsForPoint: StateFlow<List<OutputState<PointOutput>>> =
        linkRepository.all.map { outputRepository.getOutputsForPoint(it) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputState(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPoints: StateFlow<List<OutputState<PointsOutput>>> =
        flow { emit(outputRepository.getOutputsForPoints()) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputState(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointChips: StateFlow<List<OutputState<PointOutput>>> =
        linkRepository.all.map { outputRepository.getOutputsForPointChips(it) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputState(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointsChips: StateFlow<List<OutputState<PointsOutput>>> =
        flow { emit(outputRepository.getOutputsForPointsChips()) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputState(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForAppsByCategory: StateFlow<OutputStatesForAppsByCategory> =
        appsRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps ->
                activities.partition { !it.isMessagingApp() }.let { (mapActivities, messagingActivities) ->
                    Pair(
                        outputRepository.getOutputsForApps(mapActivities, hiddenApps),
                        outputRepository.getOutputsForApps(messagingActivities, hiddenApps),
                    )
                }
            }
            .combine(appDetails) { partitionedOutputs, appDetails ->
                partitionedOutputs.toOutputStatesForAppsByCategory(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                OutputStatesForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList()),
            )
    val outputsForLinks: StateFlow<List<OutputStatesForLink>> =
        linkRepository.all.map { links -> outputRepository.getOutputsForLinks(links) }
            .combine(appDetails) { outputsForLinks, appDetails ->
                outputsForLinks.toOutputStatesForLinks(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForSharing: StateFlow<OutputStatesForSharing?> =
        flow { emit(outputRepository.getOutputsForSharing()) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.toOutputStatesForSharing(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )

    suspend fun getAutomationOutput(automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?): Output? =
        outputRepository.getAutomationOutput(automation, getLinkByUUID)

    fun setSelectedUriString(newSelectedUriString: String?) {
        _selectedUriString.value = newSelectedUriString
    }
}

fun Output.toOutputState(appDetails: AppDetails): OutputState<Output> =
    OutputState(
        icon = getIcon(appDetails),
        menuIcon = getMenuIcon(appDetails),
        output = this,
    )

fun PointOutput.toOutputState(appDetails: AppDetails): OutputState<PointOutput> =
    OutputState(
        icon = getIcon(appDetails),
        menuIcon = getMenuIcon(appDetails),
        output = this,
    )

fun PointsOutput.toOutputState(appDetails: AppDetails): OutputState<PointsOutput> =
    OutputState(
        icon = getIcon(appDetails),
        menuIcon = getMenuIcon(appDetails),
        output = this,
    )

fun Map<String, List<Output>>.toOutputStatesForApps(appDetails: AppDetails): List<OutputStatesForApp> =
    mapNotNull { (packageName, outputs) ->
        outputs.map { it.toOutputState(appDetails) }.let { outputStates ->
            outputStates.firstOrNull()?.let { defaultOutputState ->
                OutputStatesForApp(
                    packageName = packageName,
                    label = appDetails[packageName]?.label.orEmpty(),
                    defaultOutputState = defaultOutputState,
                    outputStates = outputStates,
                )
            }
        }
    }

fun Pair<Map<String, List<Output>>, Map<String, List<Output>>>.toOutputStatesForAppsByCategory(appDetails: AppDetails): OutputStatesForAppsByCategory =
    let { (outputsForMapApps, outputsForMessagingApps) ->
        OutputStatesForAppsByCategory(
            mapApps = outputsForMapApps.toOutputStatesForApps(appDetails),
            messagingApps = outputsForMessagingApps.toOutputStatesForApps(appDetails),
        )
    }

fun Map<String, List<Output>>.toOutputStatesForLinks(appDetails: AppDetails): List<OutputStatesForLink> =
    mapNotNull { (group, outputs) ->
        outputs.map { it.toOutputState(appDetails) }.let { outputStates ->
            outputStates.firstOrNull()?.let { defaultOutputState ->
                OutputStatesForLink(
                    group = group,
                    defaultOutputState = defaultOutputState,
                    outputStates = outputStates,
                )
            }
        }
    }

fun List<Output>.toOutputStatesForSharing(appDetails: AppDetails): OutputStatesForSharing? =
    map { it.toOutputState(appDetails) }.let { outputStates ->
        outputStates.firstOrNull()?.let { firstOutputState ->
            OutputStatesForSharing(
                defaultOutputState = firstOutputState,
                outputStates = outputStates,
            )
        }
    }
