package page.ooooo.geoshare.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppOutputs
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.data.AppsOutputs
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.LinkOutputs
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.ShareOutputs
import page.ooooo.geoshare.data.UserPreferenceRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.getUriString
import page.ooooo.geoshare.data.extensions.zipWithNextLastNull
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.LocationAction
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.preferences.FinishPreference
import page.ooooo.geoshare.data.preferences.shouldAppFinish
import page.ooooo.geoshare.data.state.ActionCompleted
import page.ooooo.geoshare.data.state.ActionReady
import page.ooooo.geoshare.data.state.ActionState
import page.ooooo.geoshare.data.state.ActionStateContext
import page.ooooo.geoshare.data.state.ActivityShouldFinish
import page.ooooo.geoshare.data.state.AutomationRequested
import page.ooooo.geoshare.data.state.ConversionFailed
import page.ooooo.geoshare.data.state.ConversionState
import page.ooooo.geoshare.data.state.ConversionStateContext
import page.ooooo.geoshare.data.state.ConversionSucceeded
import page.ooooo.geoshare.data.state.ExtendedStateLog
import page.ooooo.geoshare.data.state.ExtendedStateLogItem
import page.ooooo.geoshare.data.state.FileUriReceived
import page.ooooo.geoshare.data.state.FileUriRequested
import page.ooooo.geoshare.data.state.LocationPermissionReceived
import page.ooooo.geoshare.data.state.LocationRationaleConfirmed
import page.ooooo.geoshare.data.state.LocationRationaleShown
import page.ooooo.geoshare.data.state.LocationReceived
import page.ooooo.geoshare.data.state.SourceReceived
import page.ooooo.geoshare.data.state.StateLog
import page.ooooo.geoshare.data.state.append
import page.ooooo.geoshare.data.state.transitionRecursively
import javax.inject.Inject
import kotlin.time.ComparableTimeMark

data class ResultAppsState(
    val appsOutputs: AppsOutputs = AppsOutputs(),
    val shareOutputs: ShareOutputs? = null,
)

data class ResultCoordinatesState(
    val chipsOutputs: List<Output<Point>> = emptyList(),
)

data class ResultLinksState(
    val linksOutputs: List<LinkOutputs> = emptyList(),
)

data class ResultPointsState(
    val chipsOutputs: List<Output<Points>> = emptyList()
)

sealed interface ResultSheetState {
    class PointCopy(
        val point: Point,
        val pointIndex: Int,
        val pointCount: Int,
        val outputs: List<Output<Point>>,
    ) : ResultSheetState

    class PointOpen(
        val point: Point,
        val pointIndex: Int,
        val pointCount: Int,
        val appsOutputs: List<AppOutputs<Output<Point>>>,
    ) : ResultSheetState

    class PointSave(
        val point: Point,
        val pointIndex: Int,
        val pointCount: Int,
        val outputs: List<Output<Point>>,
    ) : ResultSheetState

    class PointsSave(
        val points: Points,
        val outputs: List<Output<Points>>,
    ) : ResultSheetState

    class PointsShare(
        val points: Points,
        val outputs: List<Output<Points>>,
    ) : ResultSheetState

    class SourceShare(
        val source: String,
        val appDetailsForSource: AppDetails,
        val appsOutputs: List<AppOutputs<Output<String>>>,
        val copyOutput: Output<String>,
    ) : ResultSheetState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val actionStateContext: ActionStateContext,
    private val appRepository: AppRepository,
    private val conversionStateContext: ConversionStateContext,
    private val inputRepository: InputRepository,
    private val linkRepository: LinkRepository,
    private val log: Log,
    private val outputRepository: OutputRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    savedStateHandle: SavedStateHandle,
    val coordinateConverter: CoordinateConverter,
) : ViewModel() {

    // Source

    private val _error = savedStateHandle.getMutableStateFlow<String?>("error", null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _source = savedStateHandle.getMutableStateFlow("source", "")
    val source: StateFlow<String> = _source.asStateFlow()

    private val _sourceComesFromIntent = savedStateHandle.getMutableStateFlow("sourceComesFromIntent", false)
    val sourceComesFromIntent: StateFlow<Boolean> = _sourceComesFromIntent.asStateFlow()

    fun setSource(newSource: String) {
        _error.value = null
        _source.value = newSource
    }

    fun setRandomSource() {
        inputRepository.genRandomSource(
            WGS84Point(
                NaivePoint.genRandomPoint(
                    name = context.resources.getString(R.string.intro_how_to_share_google_maps_screenshot_place)
                )
            )
        )?.let { newSource -> setSource(newSource) }
    }

    // Conversion

    private var _conversionState: MutableStateFlow<ConversionState> = MutableStateFlow(ConversionState.Initial)
    val conversionState: StateFlow<ConversionState> = _conversionState.asStateFlow()
    private var conversionJob: Job? = null
    private val conversionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        log.e(TAG, "Exception when transitioning state", tr)
        val newState = ConversionFailed(
            _source.value,
            conversionStateContext.resources.getString(R.string.conversion_failed_reason_exception),
            stackTrace = tr.stackTraceToString(),
        )
        _conversionState.value = newState
        _conversionStateLog.append(newState)
    }

    private fun transitionConversion(clearLog: Boolean = false, newState: (suspend () -> ConversionState)) {
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch(conversionExceptionHandler) {
            val newState = newState()
            _conversionState.value = newState
            log.d(TAG, "Set conversion state to $newState")
            _conversionStateLog.append(newState, clear = clearLog)
            newState.transitionRecursively(conversionStateContext) { newState ->
                _conversionState.value = newState as ConversionState
                log.d(TAG, "Transitioned conversion state to $newState")
                _conversionStateLog.append(newState)
            }
        }
    }

    fun start(sourceComesFromIntent: Boolean) {
        if (_source.value.isEmpty()) {
            _error.value = context.resources.getString(R.string.conversion_failed_missing_url)
            return
        }
        _sourceComesFromIntent.value = sourceComesFromIntent
        transitionConversion(clearLog = true) { SourceReceived(_source.value) }
    }

    fun grant(doNotAsk: Boolean) {
        (_conversionState.value as? ConversionState.HasPermission)?.apply {
            transitionConversion { grant(conversionStateContext, doNotAsk) }
        }
    }

    fun deny(doNotAsk: Boolean) {
        (_conversionState.value as? ConversionState.HasPermission)?.apply {
            transitionConversion { deny(conversionStateContext, doNotAsk) }
        }
    }

    fun cancelConversion() {
        conversionJob?.cancel()
    }

    fun reset() {
        transitionConversion(clearLog = true) { ConversionState.Initial }
    }

    fun retry() {
        (_conversionState.value as? ConversionState.HasSource)?.apply {
            transitionConversion { SourceReceived(source) }
        }
    }

    // Conversion state log

    private var _conversionStateLog: MutableStateFlow<StateLog<ConversionState>> = MutableStateFlow(emptyList())
    private val conversionStateLog: StateFlow<StateLog<ConversionState>> = _conversionStateLog.asStateFlow()
    val extendedConversionStateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>> = _conversionStateLog
        .map { it.toExtendedLog() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val conversionStart: StateFlow<ComparableTimeMark?> = conversionStateLog
        .map { it.firstOrNull()?.start }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Action

    private var _actionState: MutableStateFlow<ActionState> = MutableStateFlow(ActionState.Initial)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()
    private var actionJob: Job? = null
    private val actionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        log.e(TAG, "Exception when transitioning state", tr)
        val newState = ActionCompleted(ActionResult.FAILED)
        _actionState.value = newState
    }

    private fun transitionAction(newState: (suspend () -> ActionState)) {
        actionJob?.cancel()
        actionJob = viewModelScope.launch(actionExceptionHandler) {
            val newState = newState()
            _actionState.value = newState
            log.d(TAG, "Set action state to $newState")
            newState.transitionRecursively(actionStateContext) { newState ->
                log.d(TAG, "Transitioned action state to $newState")
                _actionState.value = newState as ActionState
            }
        }
    }

    init {
        // Start automation when conversion succeeds
        _conversionState
            .filterIsInstance<ConversionSucceeded>()
            .onEach { conversionState ->
                transitionAction { AutomationRequested(conversionState.points) }
            }
            .launchIn(viewModelScope)
    }

    init {
        // Finish activity when action completes and conversion is not in progress
        _actionState
            .filterIsInstance<ActionCompleted>()
            .onEach { actionState ->
                if (_conversionState.value is ConversionState.HasResult) {
                    val finish = userPreferenceRepository.values.map { it.finish }.firstOrNull()
                        ?: FinishPreference.default
                    if (finish.shouldAppFinish(actionState.actionResult)) {
                        transitionAction { ActivityShouldFinish }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Any action

    fun startAction(action: Action<*>) {
        transitionAction { ActionReady(action, isAutomation = false) }
    }

    fun grantAction(doNotAsk: Boolean) {
        (_actionState.value as? ActionState.HasPermission)?.apply {
            transitionAction { grant(actionStateContext, doNotAsk) }
        }
    }

    fun denyAction(doNotAsk: Boolean) {
        (_actionState.value as? ActionState.HasPermission)?.apply {
            transitionAction { deny(actionStateContext, doNotAsk) }
        }
    }

    fun cancelAction() {
        actionJob?.cancel()
    }

    // File action

    fun receiveFileUri(uri: Uri) {
        (_actionState.value as? FileUriRequested)?.apply {
            transitionAction { FileUriReceived(action, isAutomation, uri) }
        }
    }

    fun cancelFileUriRequest() {
        (_actionState.value as? FileUriRequested)?.apply {
            transitionAction { ActionCompleted(ActionResult.FAILED) }
        }
    }

    // Location action

    fun showLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        transitionAction { LocationRationaleShown(action, isAutomation) }
    }

    fun skipLocationRationale(action: LocationAction<*>, isAutomation: Boolean) {
        transitionAction { LocationPermissionReceived(action, isAutomation) }
    }

    fun receiveLocationPermission() {
        (_actionState.value as? LocationRationaleConfirmed)?.apply {
            transitionAction { LocationPermissionReceived(action, isAutomation) }
        }
    }

    fun receiveLocation(action: LocationAction<*>, isAutomation: Boolean, location: Point?) {
        transitionAction { LocationReceived(action, isAutomation, location) }
    }

    fun cancelLocationFinding() {
        (_actionState.value as? LocationPermissionReceived)?.apply {
            transitionAction { ActionCompleted(ActionResult.FAILED) }
        }
    }

    // Result

    val appDetails: StateFlow<AppDetails> =
        appRepository.appDetails
            .filterNotNull()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap(),
            )

    val resultAppsState: StateFlow<ResultAppsState> =
        appRepository.activities
            .filterNotNull()
            .combine(
                userPreferenceRepository.values
                    .map { it.hiddenApps }
                    .distinctUntilChanged()
            ) { activities, hiddenApps ->
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(activities, hiddenApps),
                    shareOutputs = outputRepository.shareOutputs,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ResultAppsState(),
            )

    val resultCoordinatesState: StateFlow<ResultCoordinatesState> =
        linkRepository.all
            .map { links ->
                ResultCoordinatesState(chipsOutputs = outputRepository.getPointChipsOutputs(links))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ResultCoordinatesState(),
            )

    val resultPointsState: StateFlow<ResultPointsState> =
        flow { emit(ResultPointsState(chipsOutputs = outputRepository.pointsChipsOutputs)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ResultPointsState(),
            )

    val resultLinksState: StateFlow<ResultLinksState> =
        linkRepository.all
            .map { links -> ResultLinksState(outputRepository.getLinksOutputs(links)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ResultLinksState(),
            )

    private var resultSheetJob: Job? = null

    private val _resultSheetState: MutableStateFlow<ResultSheetState?> = MutableStateFlow(null)
    val resultSheetState: StateFlow<ResultSheetState?> = _resultSheetState.asStateFlow()

    fun copyPoint(point: Point, pointCount: Int, pointIndex: Int) {
        cancelAction()
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            val links = linkRepository.all.firstOrNull() ?: emptyList()
            _resultSheetState.value = ResultSheetState.PointCopy(
                point = point,
                pointIndex = pointIndex,
                pointCount = pointCount,
                outputs = outputRepository.getPointCopyOutputs(links),
            )
        }
    }

    fun openPoint(point: Point, pointCount: Int, pointIndex: Int) {
        cancelAction()
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            val activities = appRepository.activities.filterNotNull().firstOrNull() ?: emptyList()
            val hiddenApps = userPreferenceRepository.values.map { it.hiddenApps }.firstOrNull() ?: emptySet()
            _resultSheetState.value = ResultSheetState.PointOpen(
                point = point,
                pointIndex = pointIndex,
                pointCount = pointCount,
                appsOutputs = outputRepository.getPointOpenOutputs(activities, hiddenApps),
            )
        }
    }

    fun savePoint(point: Point, pointCount: Int, pointIndex: Int) {
        cancelAction()
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            _resultSheetState.value = ResultSheetState.PointSave(
                point = point,
                pointIndex = pointIndex,
                pointCount = pointCount,
                outputs = outputRepository.pointSaveOutputs,
            )
        }
    }

    fun savePoints(points: Points) {
        cancelAction()
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            _resultSheetState.value = ResultSheetState.PointsSave(
                points = points,
                outputs = outputRepository.pointsSaveOutputs,
            )
        }
    }

    fun sharePoints(points: Points) {
        cancelAction()
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            _resultSheetState.value = ResultSheetState.PointsShare(
                points = points,
                outputs = outputRepository.pointsShareOutputs,
            )
        }
    }

    fun shareSource(source: String) {
        // Notice that sharing source does not cancel action
        resultSheetJob?.cancel()
        resultSheetJob = viewModelScope.launch {
            // First load only activities and outputs
            val activitiesForUri = appRepository.queryActivitiesForUri(source)
            val appsOutputs = outputRepository.getSourceAppsOutputs(activitiesForUri)
            _resultSheetState.value = ResultSheetState.SourceShare(
                source = source,
                appDetailsForSource = emptyMap(),
                appsOutputs = appsOutputs,
                copyOutput = outputRepository.sourceCopyOutput,
            )
            // Then load app details
            val appDetailsForSource = appRepository.queryAppDetails(activitiesForUri)
            _resultSheetState.value = ResultSheetState.SourceShare(
                source = source,
                appDetailsForSource = appDetailsForSource,
                appsOutputs = appsOutputs,
                copyOutput = outputRepository.sourceCopyOutput,
            )
        }
    }

    fun hideSheet() {
        resultSheetJob?.cancel()
        _resultSheetState.value = null
    }

    // Lifecycle

    fun onCreateOrNewIntent(intent: Intent) {
        setSource(intent.getUriString().orEmpty())
        start(true)
    }

    private companion object {
        private const val TAG = "MainViewModel"
    }
}

fun StateLog<ConversionState>.toExtendedLog(): ExtendedStateLog<ConversionState.HasDescription> =
    zipWithNextLastNull { logItem, nextLogItem ->
        if (logItem.state is ConversionState.HasDescription) {
            if (nextLogItem != null) {
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = logItem.id,
                    state = logItem.state,
                    start = logItem.start,
                    end = nextLogItem.start,
                    succeeded = when (nextLogItem.state) {
                        is ConversionState.HasError -> false
                        is ConversionState.HasAttempt if nextLogItem.state.lastAttempt != null -> false
                        else -> true
                    },
                )
            } else {
                ExtendedStateLogItem.Pending<ConversionState.HasDescription>(
                    id = logItem.id,
                    state = logItem.state,
                    start = logItem.start,
                )
            }
        } else {
            null
        }
    }
        .filterNotNull()
