package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.extensions.zipWithNextFirstNull
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.OutputState
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheet(
    points: Points,
    selectedPointIndex: Int,
    initialValue: SheetValue = SheetValue.Hidden,
    outputsForPoint: StateFlow<List<OutputState<PointOutput>>>,
    outputsForPoints: StateFlow<List<OutputState<PointsOutput>>>,
    onExecute: (action: Action<*>) -> Unit,
    onSelectPointIndex: (index: Int?) -> Unit,
) {
    val selectedPoint = points.getOrNull(selectedPointIndex) ?: return

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue)

    val outputsForPoint by outputsForPoint.collectAsStateWithLifecycle()
    val outputsForPoints by outputsForPoints.collectAsStateWithLifecycle()

    fun hide() {
        coroutineScope
            .launch {
                sheetState.hide()
            }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onSelectPointIndex(null)
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = { onSelectPointIndex(null) },
        sheetState = sheetState,
    ) {
        LazyColumn(
            Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareResultSheet"),
        ) {
            item {
                SheetSection(
                    title = if (points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_number, selectedPointIndex + 1)
                    } else {
                        null
                    },
                ) {
                    outputsForPoint
                        .filter { it.output.isAvailable(selectedPoint) }
                        .zipWithNextFirstNull { prevOutputState, outputState ->
                            SheetListItem(
                                headlineText = outputState.output.label(outputState.appLabel),
                                onClick = {
                                    hide()
                                    onExecute(outputState.output.toAction(selectedPoint))
                                },
                                supportingText = outputState.output.getDescription(selectedPoint),
                                icon = outputState.icon,
                                prevIcon = prevOutputState?.icon,
                            )
                        }
                }
            }
            item {
                SheetSection(
                    first = false,
                    title = if (points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_all, points.size)
                    } else {
                        null
                    },
                ) {
                    outputsForPoints
                        .filter { it.output.isAvailable(points) }
                        .zipWithNextFirstNull { prevOutputState, outputState ->
                            SheetListItem(
                                headlineText = outputState.output.label(outputState.appLabel),
                                onClick = {
                                    hide()
                                    onExecute(outputState.output.toAction(points))
                                },
                                supportingText = outputState.output.getDescription(points),
                                icon = outputState.icon,
                                prevIcon = prevOutputState?.icon,
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun DefaultPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example), WGS84Point(NaivePoint.genRandomPoint())),
                selectedPointIndex = 1,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).map { it.toOutputState(appDetails) }
                ),
                outputsForPoints = MutableStateFlow(
                    outputRepository.getOutputsForPoints().map { it.toOutputState(appDetails) }
                ),
                onExecute = {},
                onSelectPointIndex = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example), WGS84Point(NaivePoint.genRandomPoint())),
                selectedPointIndex = 1,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).map { it.toOutputState(appDetails) }
                ),
                outputsForPoints = MutableStateFlow(
                    outputRepository.getOutputsForPoints().map { it.toOutputState(appDetails) }
                ),
                onExecute = {},
                onSelectPointIndex = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun LastPointPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                selectedPointIndex = 0,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).map { it.toOutputState(appDetails) }
                ),
                outputsForPoints = MutableStateFlow(
                    outputRepository.getOutputsForPoints().map { it.toOutputState(appDetails) }
                ),
                onExecute = {},
                onSelectPointIndex = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkLastPointPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                selectedPointIndex = 0,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).map { it.toOutputState(appDetails) }
                ),
                outputsForPoints = MutableStateFlow(
                    outputRepository.getOutputsForPoints().map { it.toOutputState(appDetails) }
                ),
                onExecute = {},
                onSelectPointIndex = {},
            )
        }
    }
}
