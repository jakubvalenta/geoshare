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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.OutputsForApps
import page.ooooo.geoshare.data.OutputsForPoints
import page.ooooo.geoshare.data.OutputsForSharing
import page.ooooo.geoshare.data.OutputsForUri
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.android.AppActivity
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.getPackageNames
import page.ooooo.geoshare.data.android.queryActivitiesForUri
import page.ooooo.geoshare.data.android.queryAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.outputs.IconDescriptor
import page.ooooo.geoshare.data.outputs.Output
import javax.inject.Inject

data class OutputDetail<T>(
    private val appDetail: AppDetail?,
    val icon: IconDescriptor?,
    val menuIcon: IconDescriptor?,
    val output: Output<T>,
) {
    @Composable
    fun label(): String = output.label(appDetail)
}

data class OutputDetailsForPoints(
    val save: List<OutputDetail<Points>> = emptyList(),
    val share: List<OutputDetail<Points>> = emptyList(),
)

data class OutputDetailsForApp(
    val packageName: String,
    val label: String,
    val default: OutputDetail<*>,
    val all: List<OutputDetail<*>>,
)

data class OutputDetailsForApps(
    val maps: List<OutputDetailsForApp> = emptyList(),
    val messaging: List<OutputDetailsForApp> = emptyList(),
)

data class OutputDetailsForLink(
    val group: String,
    val default: OutputDetail<*>,
    val all: List<OutputDetail<*>>,
)

data class OutputDetailForApp<T>(
    val label: String?,
    val icon: IconDescriptor?,
    val output: Output<T>,
)

data class OutputDetailsForUri(
    val copy: OutputDetail<String>,
    val open: List<OutputDetailForApp<String>>,
)

data class OutputDetailsForSharing(
    val default: OutputDetail<Point>,
    val all: List<OutputDetail<*>>,
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

    // Selected URI

    private val _selectedUri: MutableStateFlow<String?> =
        savedStateHandle.getMutableStateFlow("selectedUri", null)
    val selectedUri: StateFlow<String?> = _selectedUri.asStateFlow()

    fun setSelectedUri(newSelectedUri: String?) {
        _selectedUri.value = newSelectedUri
    }

    // Activities

    private val activitiesForUri: StateFlow<List<AppActivity>> =
        selectedUri
            .filterNotNull()
            .map { context.packageManager.queryActivitiesForUri(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )

    // App details

    val appDetails: StateFlow<AppDetails> =
        appRepository.appDetails
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )
    val appDetailsForUri: StateFlow<AppDetails> =
        activitiesForUri
            .filter { it.isNotEmpty() }
            .map { context.packageManager.queryAppDetails(it.getPackageNames()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )

    // Outputs

    val outputsForPointCopy: StateFlow<List<OutputDetail<Point>>> =
        linkRepository.all
            .map { links -> outputRepository.getOutputsForPointCopy(links) }
            .combine(appRepository.appDetails) { outputs, appDetails -> outputs.toOutputDetails(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointOpen: StateFlow<List<OutputDetailForApp<Point>>> =
        appRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps -> outputRepository.getOutputsForPointOpen(activities, hiddenApps) }
            .combine(appRepository.appDetails) { outputs, appDetails -> outputs.toOutputDetailsForApp(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointSave: StateFlow<List<OutputDetail<Point>>> =
        appRepository.appDetails
            .map { appDetails -> outputRepository.outputsForPointSave.toOutputDetails(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPoints: StateFlow<OutputDetailsForPoints> =
        appRepository.appDetails
            .map { appDetails -> outputRepository.outputsForPoints.toOutputDetailsForPoints(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                OutputDetailsForPoints(),
            )
    val outputsForPointChips: StateFlow<List<OutputDetail<Point>>> =
        linkRepository.all
            .map { links -> outputRepository.getOutputsForPointChips(links) }
            .combine(appRepository.appDetails) { outputs, appDetails -> outputs.toOutputDetails(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForPointsChips: StateFlow<List<OutputDetail<Points>>> =
        appRepository.appDetails
            .map { appDetails -> outputRepository.outputsForPointsChips.toOutputDetails(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForApps: StateFlow<OutputDetailsForApps> =
        appRepository.activities
            .combine(
                userPreferencesRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps -> outputRepository.getOutputsForApps(activities, hiddenApps) }
            .combine(appDetails) { outputsForApps, appDetails -> outputsForApps.toOutputDetailsForApps(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                OutputDetailsForApps(),
            )
    val outputsForLinks: StateFlow<List<OutputDetailsForLink>> =
        linkRepository.all
            .map { links -> outputRepository.getOutputsForLinks(links) }
            .combine(appDetails) { outputsForLinks, appDetails -> outputsForLinks.toOutputDetailsForLinks(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    val outputsForSharing: StateFlow<OutputDetailsForSharing?> =
        appRepository.appDetails
            .map { appDetails -> outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )
    val outputsForUri: StateFlow<OutputDetailsForUri?> =
        activitiesForUri
            .map { activities -> outputRepository.getOutputsForUri(activities) }
            .combine(appDetailsForUri) { outputsForUri, appDetails -> outputsForUri.toOutputDetailsForUri(appDetails) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )
    val outputsForExampleApp: StateFlow<OutputDetailsForApp?> =
        outputsForApps
            .map { outputDetailsForApps ->
                PackageNames.examples.firstNotNullOfOrNull { packageName ->
                    outputDetailsForApps.maps.firstOrNull { outputDetailsForApp ->
                        outputDetailsForApp.packageName == packageName
                    }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )
}

fun <T> Output<T>.toOutputDetail(appDetails: AppDetails): OutputDetail<T> =
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetail(
            appDetail = appDetail,
            icon = getIcon(appDetail),
            menuIcon = getMenuIcon(appDetail),
            output = this,
        )
    }

fun <T> List<Output<T>>.toOutputDetails(appDetails: AppDetails): List<OutputDetail<T>> =
    map { it.toOutputDetail(appDetails) }

fun <T> Output<T>.toOutputDetailForApp(appDetails: AppDetails): OutputDetailForApp<T> = // TODO Rename
    (this as? Output.HasActivity<*>)?.getAppDetail(appDetails).let { appDetail ->
        OutputDetailForApp(
            label = appDetail?.label,
            icon = getIcon(appDetail),
            output = this,
        )
    }

fun <T> List<Output<T>>.toOutputDetailsForApp(appDetails: AppDetails): List<OutputDetailForApp<T>> =
    map { it.toOutputDetailForApp(appDetails) }
        .sortedBy { it.label }

fun OutputsForPoints.toOutputDetailsForPoints(appDetails: AppDetails): OutputDetailsForPoints =
    OutputDetailsForPoints(
        save = save.toOutputDetails(appDetails),
        share = share.toOutputDetails(appDetails),
    )

fun OutputsForApps.toOutputDetailsForApps(appDetails: AppDetails): OutputDetailsForApps =
    OutputDetailsForApps(
        maps = maps.toOutputDetailsForApps(appDetails),
        messaging = messaging.toOutputDetailsForApps(appDetails),
    )

fun Map<String, List<Output<*>>>.toOutputDetailsForApps(appDetails: AppDetails): List<OutputDetailsForApp> =
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
        .sortedBy { it.label }

fun Map<String, List<Output<*>>>.toOutputDetailsForLinks(appDetails: AppDetails): List<OutputDetailsForLink> =
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

fun OutputsForSharing.toOutputDetailsForSharing(appDetails: AppDetails): OutputDetailsForSharing =
    OutputDetailsForSharing(
        default = default.toOutputDetail(appDetails),
        all = all.map { it.toOutputDetail(appDetails) },
    )

fun OutputsForUri.toOutputDetailsForUri(appDetails: AppDetails): OutputDetailsForUri =
    OutputDetailsForUri(
        copy = copy.toOutputDetail(appDetails),
        open = open.toOutputDetailsForApp(appDetails),
    )
