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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.extensions.zipWithNextFirstNull
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.ui.OutputDetailsForPoint
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputDetailsForPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultCopySheet(
    point: Point,
    pointIndex: Int? = null,
    initialValue: SheetValue = SheetValue.Hidden,
    outputsForPoint: StateFlow<OutputDetailsForPoint>,
    onDismissRequest: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue)

    val outputsForPoint by outputsForPoint.collectAsStateWithLifecycle()

    fun hide() {
        coroutineScope
            .launch {
                sheetState.hide()
            }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismissRequest()
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        LazyColumn(
            Modifier
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareResultSheet"),
        ) {
            item(key = "point_section", contentType = "sheet_section") {
                SheetSection(
                    title = pointIndex?.let { pointIndex ->
                        stringResource(R.string.conversion_succeeded_point_number, pointIndex + 1)
                    },
                ) {
                    outputsForPoint.copy.zipWithNextFirstNull { prevOutputDetail, outputDetail ->
                        SheetListItem(
                            headlineText = outputDetail.label(),
                            enabled = outputDetail.output.isEnabled(point),
                            onClick = {
                                hide()
                                onExecute(outputDetail.output.toAction(point))
                            },
                            supportingText = outputDetail.output.getDescription(point),
                            icon = outputDetail.icon,
                            prevIcon = prevOutputDetail?.icon,
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
            ResultCopySheet(
                point = WGS84Point(NaivePoint.example),
                pointIndex = 1,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).toOutputDetailsForPoint(appDetails)
                ),
                onDismissRequest = {},
                onExecute = {},
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
            ResultCopySheet(
                point = WGS84Point(NaivePoint.example),
                pointIndex = 1,
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).toOutputDetailsForPoint(appDetails)
                ),
                onDismissRequest = {},
                onExecute = {},
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
            ResultCopySheet(
                point = WGS84Point(NaivePoint.example),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).toOutputDetailsForPoint(appDetails)
                ),
                onDismissRequest = {},
                onExecute = {},
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
            ResultCopySheet(
                point = WGS84Point(NaivePoint.example),
                initialValue = SheetValue.Expanded,
                outputsForPoint = MutableStateFlow(
                    outputRepository.getOutputsForPoint(defaultFakeLinks).toOutputDetailsForPoint(appDetails)
                ),
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}
