package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.extensions.zipWithNextFirstNull
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.OutputDetail
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputDetailsForPoint
import page.ooooo.geoshare.ui.toOutputDetailsForPoints

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ResultSheet(
    outputs: List<OutputDetail<T>>,
    title: String?,
    value: T,
    initialSheetValue: SheetValue = SheetValue.Hidden,
    onExecute: (action: Action<*>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialSheetValue)

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

    if (outputs.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SheetSection(title = title) {
                    outputs.zipWithNextFirstNull { prevOutputDetail, outputDetail ->
                        val enabled = outputDetail.output.isEnabled(value)
                        SheetListItem(
                            headlineText = outputDetail.label(),
                            enabled = enabled,
                            onClick = {
                                hide()
                                onExecute(outputDetail.output.toAction(value))
                            },
                            supportingText = if (enabled) {
                                outputDetail.output.getDescription(value)
                            } else {
                                stringResource(R.string.not_available)
                            },
                            icon = outputDetail.icon,
                            prevIcon = prevOutputDetail?.icon,
                        )
                    }
                }
            }
        }
    }
}

// Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun CopyPointPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails: AppDetails = emptyMap()
            val outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks)
                .toOutputDetailsForPoint(appDetails)
            val point = WGS84Point(NaivePoint.example)
            ResultSheet(
                outputs = outputsForPoint.copy,
                title = pointTitle(point, pointIndex = 0, totalPoints = 2),
                value = point,
                initialSheetValue = SheetValue.Expanded,
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
private fun DarkCopyPointPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails: AppDetails = emptyMap()
            val outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks)
                .toOutputDetailsForPoint(appDetails)
            val point = WGS84Point(NaivePoint.example)
            ResultSheet(
                outputs = outputsForPoint.copy,
                title = pointTitle(point, pointIndex = 0, totalPoints = 2),
                value = point,
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun SavePointPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails: AppDetails = emptyMap()
            val outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks)
                .toOutputDetailsForPoint(appDetails)
            val point = WGS84Point(NaivePoint.example)
            ResultSheet(
                outputs = outputsForPoint.save,
                title = pointTitle(point, pointIndex = 2, totalPoints = 3),
                value = point,
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun SavePointsPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails: AppDetails = emptyMap()
            val outputsForPoints = outputRepository.getOutputsForPoints()
                .toOutputDetailsForPoints(appDetails)
            val points = persistentListOf(
                WGS84Point(NaivePoint.genRandomPoint()),
                WGS84Point(NaivePoint.genRandomPoint()),
                WGS84Point(NaivePoint.example),
            )
            ResultSheet(
                outputs = outputsForPoints.save,
                title = stringResource(R.string.conversion_succeeded_point_all, points.size),
                value = points,
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}
