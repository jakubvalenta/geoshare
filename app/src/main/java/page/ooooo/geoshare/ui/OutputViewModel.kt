package page.ooooo.geoshare.ui

import android.content.Context
import androidx.compose.runtime.Composable
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
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.android.AppActivity
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.getPackageNames
import page.ooooo.geoshare.lib.android.isMessagingApp
import page.ooooo.geoshare.lib.android.queryActivitiesForUri
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.CopyStringOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.StringOutput
import page.ooooo.geoshare.ui.components.IconDescriptor
import javax.inject.Inject

data class OutputDetail<T : Output>(
    private val appDetail: AppDetail?,
    val icon: IconDescriptor?,
    val menuIcon: IconDescriptor?,
    val output: T,
) {
    @Composable
    fun label(): String = output.label(appDetail)
}

data class OutputDetailsForApp(
    val packageName: String,
    val label: String,
    val default: OutputDetail<Output>,
    val all: List<OutputDetail<Output>>,
)

data class OutputDetailsForAppsByCategory(
    val mapApps: List<OutputDetailsForApp>,
    val messagingApps: List<OutputDetailsForApp>,
)

data class OutputDetailsForLink(
    val group: String,
    val default: OutputDetail<Output>,
    val all: List<OutputDetail<Output>>,
)

data class OutputDetailsForUriByCategory(
    val copy: List<OutputDetail<StringOutput>>,
    val open: List<OutputDetail<StringOutput>>,
)

/**
 * All [outputDetails] that can be executed for the share icon.
 *
 * - [defaultOutputDetail] is the output that will be executed when clicking the icon in the UI.
 * - [outputDetails] are the outputs that will be shown in a context menu in the UI.
 *
 * Example: "Share", "Navigate", and "Share GPX route" outputs
 */
data class OutputDetailsForSharing(
    val defaultOutputDetail: OutputDetail<Output>,
    val outputDetails: List<OutputDetail<Output>>,
)

@HiltViewModel
class OutputViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    appRepository: AppRepository,
    linkRepository: LinkRepository,
    private val outputRepository: OutputRepository,
    userPreferencesRepository: UserPreferencesRepository,
    val coordinateConverter: CoordinateConverter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedUri: MutableStateFlow<String?> =
        savedStateHandle.getMutableStateFlow("selectedUri", null)
    val selectedUri: StateFlow<String?> = _selectedUri.asStateFlow()

    private val activitiesForUri: StateFlow<List<AppActivity>> = selectedUri
        .filterNotNull()
        .map { context.packageManager.queryActivitiesForUri(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val appDetails: StateFlow<AppDetails> = appRepository.appDetails
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )
    val appDetailsForUri: StateFlow<AppDetails> = activitiesForUri
        .filter { it.isNotEmpty() }
        .map { context.packageManager.queryAppDetails(it.getPackageNames()) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap(),
        )

    val outputsForPoint: StateFlow<List<OutputDetail<PointOutput>>> =
        linkRepository.all.map { outputRepository.getOutputsForPoint(it) }
            .combine(appRepository.appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputDetail(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPoints: StateFlow<List<OutputDetail<PointsOutput>>> =
        flow { emit(outputRepository.getOutputsForPoints()) }
            .combine(appRepository.appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputDetail(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointChips: StateFlow<List<OutputDetail<PointOutput>>> =
        linkRepository.all.map { outputRepository.getOutputsForPointChips(it) }
            .combine(appRepository.appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputDetail(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointsChips: StateFlow<List<OutputDetail<PointsOutput>>> =
        flow { emit(outputRepository.getOutputsForPointsChips()) }
            .combine(appRepository.appDetails) { outputs, appDetails ->
                outputs.map { it.toOutputDetail(appDetails) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForAppsByCategory: StateFlow<OutputDetailsForAppsByCategory> =
        appRepository.activities
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
                partitionedOutputs.toOutputDetailsForAppsByCategory(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList()),
            )
    val outputsForLinks: StateFlow<List<OutputDetailsForLink>> =
        linkRepository.all.map { links -> outputRepository.getOutputsForLinks(links) }
            .combine(appDetails) { outputsForLinks, appDetails ->
                outputsForLinks.toOutputDetailsForLinks(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForSharing: StateFlow<OutputDetailsForSharing?> =
        flow { emit(outputRepository.getOutputsForSharing()) }
            .combine(appDetails) { outputs, appDetails ->
                outputs.toOutputDetailsForSharing(appDetails)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )
    val outputsForUriByCategory: StateFlow<OutputDetailsForUriByCategory> = activitiesForUri
        .combine(appDetailsForUri) { activities, appDetails ->
            outputRepository
                .getOutputsForUri(activities)
                .partition { it is CopyStringOutput }
                .toOutputDetailsForUriByCategory(appDetails)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OutputDetailsForUriByCategory(copy = emptyList(), open = emptyList()),
        )

    fun setSelectedUri(newSelectedUri: String?) {
        _selectedUri.value = newSelectedUri
    }
}

fun Output.toOutputDetail(appDetails: AppDetails): OutputDetail<Output> =
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetail(
            appDetail = appDetail,
            icon = getIcon(appDetail),
            menuIcon = getMenuIcon(appDetail),
            output = this,
        )
    }

fun PointOutput.toOutputDetail(appDetails: AppDetails): OutputDetail<PointOutput> =
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetail(
            appDetail = appDetail,
            icon = getIcon(appDetail),
            menuIcon = getMenuIcon(appDetail),
            output = this,
        )
    }

fun PointsOutput.toOutputDetail(appDetails: AppDetails): OutputDetail<PointsOutput> =
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetail(
            appDetail = appDetail,
            icon = getIcon(appDetail),
            menuIcon = getMenuIcon(appDetail),
            output = this,
        )
    }

fun StringOutput.toOutputDetail(appDetails: AppDetails): OutputDetail<StringOutput> =
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetail(
            appDetail = appDetail,
            icon = getIcon(appDetail),
            menuIcon = getMenuIcon(appDetail),
            output = this,
        )
    }

fun Map<String, List<Output>>.toOutputDetailsForApps(appDetails: AppDetails): List<OutputDetailsForApp> =
    mapNotNull { (packageName, outputs) ->
        outputs.map { it.toOutputDetail(appDetails) }.let { outputDetails ->
            outputDetails.firstOrNull()?.let { defaultOutputDetail ->
                OutputDetailsForApp(
                    packageName = packageName,
                    label = appDetails[packageName]?.label.orEmpty(),
                    default = defaultOutputDetail,
                    all = outputDetails,
                )
            }
        }
    }

fun Pair<Map<String, List<Output>>, Map<String, List<Output>>>.toOutputDetailsForAppsByCategory(appDetails: AppDetails): OutputDetailsForAppsByCategory =
    let { (outputsForMapApps, outputsForMessagingApps) ->
        OutputDetailsForAppsByCategory(
            mapApps = outputsForMapApps.toOutputDetailsForApps(appDetails),
            messagingApps = outputsForMessagingApps.toOutputDetailsForApps(appDetails),
        )
    }

fun Map<String, List<Output>>.toOutputDetailsForLinks(appDetails: AppDetails): List<OutputDetailsForLink> =
    mapNotNull { (group, outputs) ->
        outputs.map { it.toOutputDetail(appDetails) }.let { outputDetails ->
            outputDetails.firstOrNull()?.let { defaultOutputDetail ->
                OutputDetailsForLink(
                    group = group,
                    default = defaultOutputDetail,
                    all = outputDetails,
                )
            }
        }
    }

fun List<Output>.toOutputDetailsForSharing(appDetails: AppDetails): OutputDetailsForSharing? =
    map { it.toOutputDetail(appDetails) }.let { outputDetails ->
        outputDetails.firstOrNull()?.let { firstOutputDetail ->
            OutputDetailsForSharing(
                defaultOutputDetail = firstOutputDetail,
                outputDetails = outputDetails,
            )
        }
    }

fun Pair<List<StringOutput>, List<StringOutput>>.toOutputDetailsForUriByCategory(appDetails: AppDetails): OutputDetailsForUriByCategory =
    let { (outputsForCopying, outputsForOpening) ->
        OutputDetailsForUriByCategory(
            copy = outputsForCopying.map { it.toOutputDetail(appDetails) },
            open = outputsForOpening.map { it.toOutputDetail(appDetails) },
        )
    }
