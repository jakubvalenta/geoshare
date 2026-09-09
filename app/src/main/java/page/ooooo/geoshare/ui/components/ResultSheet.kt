package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheet(
    points: Points,
    selectedPointIndex: Int,
    appDetails: StateFlow<AppDetails>,
    initialValue: SheetValue = SheetValue.Hidden,
    outputsForPoint: StateFlow<List<PointOutput>>,
    outputsForPoints: StateFlow<List<PointsOutput>>,
    onExecute: (action: Action<*>) -> Unit,
    onSelectPointIndex: (index: Int?) -> Unit,
) {
    val selectedPoint = points.getOrNull(selectedPointIndex) ?: return

    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    val sheetState = rememberBottomSheetState(initialValue)

    val appDetails by appDetails.collectAsStateWithLifecycle()
    val outputsForPoint by outputsForPoint.collectAsStateWithLifecycle()
    val outputsForPoints by outputsForPoints.collectAsStateWithLifecycle()

    fun hide() {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
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
                ResultSuccessSheetItemGroup(
                    title = if (points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_number, selectedPointIndex + 1)
                    } else {
                        null
                    },
                    appDetails = appDetails,
                    actions = outputsForPoint
                        .filter { it.isAvailable(selectedPoint) }
                        .map { it.toAction(selectedPoint) },
                    value = selectedPoint,
                    onClick = { action ->
                        hide()
                        onExecute(action)
                    },
                )
            }
            item {
                Spacer(Modifier.height(spacing.small))
            }
            item {
                ResultSuccessSheetItemGroup(
                    title = if (points.size > 1) {
                        stringResource(R.string.conversion_succeeded_point_all, points.size)
                    } else {
                        null
                    },
                    appDetails = appDetails,
                    actions = outputsForPoints
                        .filter { it.isAvailable(points) }
                        .map { it.toAction(points) },
                    value = points,
                    onClick = { action ->
                        hide()
                        onExecute(action)
                    },
                )
            }
        }
    }
}

@Composable
private fun <T> ResultSuccessSheetItemGroup(
    title: String?,
    appDetails: AppDetails,
    actions: List<Action<T>>,
    value: T,
    onClick: (action: Action<*>) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column {
        if (title != null) {
            LabelLarge(
                title,
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = spacing.tiny),
            )
        }
        var prevIcon: IconDescriptor? = null
        actions.forEach { action ->
            ResultSheetItem(
                headlineText = action.output.label(appDetails),
                onClick = { onClick(action) },
                supportingText = action.getDescription(value),
                icon = action.output.getIcon(appDetails)
                    ?.takeIf { it != prevIcon }
                    ?.also { prevIcon = it },
            )
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example), WGS84Point(NaivePoint.genRandomPoint())),
                selectedPointIndex = 1,
                appDetails = MutableStateFlow(emptyMap()),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(outputRepository.getOutputsForPoint(defaultFakeLinks)),
                outputsForPoints = MutableStateFlow(outputRepository.getOutputsForPoints()),
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example), WGS84Point(NaivePoint.genRandomPoint())),
                selectedPointIndex = 1,
                appDetails = MutableStateFlow(emptyMap()),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(outputRepository.getOutputsForPoint(defaultFakeLinks)),
                outputsForPoints = MutableStateFlow(outputRepository.getOutputsForPoints()),
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                selectedPointIndex = 0,
                appDetails = MutableStateFlow(emptyMap()),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(outputRepository.getOutputsForPoint(defaultFakeLinks)),
                outputsForPoints = MutableStateFlow(outputRepository.getOutputsForPoints()),
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSheet(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                selectedPointIndex = 0,
                appDetails = MutableStateFlow(emptyMap()),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(outputRepository.getOutputsForPoint(defaultFakeLinks)),
                outputsForPoints = MutableStateFlow(outputRepository.getOutputsForPoints()),
                onExecute = {},
                onSelectPointIndex = {},
            )
        }
    }
}
