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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import page.ooooo.geoshare.data.AppOutputs
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.extensions.zipWithNextFirstNull
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.DrawableIconDescriptor
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.outputs.PlaceholderIconDescriptor
import page.ooooo.geoshare.data.outputs.ResourceIconDescriptor
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.ResultSheetState
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheet(
    state: StateFlow<ResultSheetState>,
    initialSheetValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    val state by state.collectAsStateWithLifecycle()

    when (state) {
        is ResultSheetState.PointCopy -> (state as? ResultSheetState.PointCopy)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetDefaultContent(
                    appDetails = appDetails,
                    value = point,
                    outputs = outputs,
                    title = pointTitle(point, pointIndex, totalPoints),
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        is ResultSheetState.PointOpen -> (state as? ResultSheetState.PointOpen)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetOpenPointContent(
                    appDetails = appDetails,
                    appsOutputs = appsOutputs,
                    value = point,
                    title = pointTitle(point, pointIndex, totalPoints),
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        is ResultSheetState.PointSave -> (state as? ResultSheetState.PointSave)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetDefaultContent(
                    appDetails = appDetails,
                    value = point,
                    outputs = outputs,
                    title = pointTitle(point, pointIndex, totalPoints),
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        is ResultSheetState.PointsSave -> (state as? ResultSheetState.PointsSave)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetDefaultContent(
                    appDetails = appDetails,
                    value = points,
                    outputs = outputs,
                    title = stringResource(R.string.conversion_succeeded_point_all, points.size),
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        is ResultSheetState.PointsShare -> (state as? ResultSheetState.PointsShare)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetDefaultContent(
                    appDetails = appDetails,
                    value = points,
                    outputs = outputs,
                    title = stringResource(R.string.conversion_succeeded_point_all, points.size),
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        is ResultSheetState.SourceShare -> (state as? ResultSheetState.SourceShare)?.run {
            ResultSheet(
                initialSheetValue = initialSheetValue,
                onDismissRequest = onDismissRequest,
            ) { onHide ->
                ResultSheetSourceContent(
                    appDetails = appDetails,
                    appsOutputs = appsOutputs,
                    copyOutput = copyOutput,
                    value = source,
                    onHide = onHide,
                    onExecute = onExecute,
                )
            }
        }

        ResultSheetState.Hidden -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultSheet(
    initialSheetValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
    content: @Composable (onHide: () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialSheetValue)

    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .semantics { testTagsAsResourceId = true }
                .testTag("geoShareResultSheet")
        ) {
            content {
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
        }
    }
}

@Composable
private fun <T> ResultSheetDefaultContent(
    appDetails: AppDetails,
    outputs: List<Output<T>>,
    title: String?,
    value: T,
    onExecute: (action: Action<*>) -> Unit,
    onHide: () -> Unit,
) {
    SheetSection(title = title) {
        outputs.zipWithNextFirstNull { prevOutput, output ->
            val enabled = output.isEnabled(value)
            SheetListItem(
                headlineText = output.label(appDetails),
                enabled = enabled,
                onClick = {
                    onHide()
                    onExecute(output.toAction(value))
                },
                supportingText = if (enabled) {
                    output.getDescription(value)
                } else {
                    stringResource(R.string.not_available)
                },
                icon = output.getIcon(appDetails),
                prevIcon = prevOutput?.getIcon(appDetails),
            )
        }
    }
}

@Composable
private fun <T> ResultSheetOpenPointContent(
    appDetails: AppDetails,
    appsOutputs: List<AppOutputs<Output<T>>>,
    title: String?,
    value: T,
    onExecute: (action: Action<*>) -> Unit,
    onHide: () -> Unit,
) {
    SheetSection(title = title) {
        appsOutputs.forEach { appOutputs ->
            SheetListItem(
                headlineText = appDetails[appOutputs.packageName]?.label.orEmpty(),
                modifier = Modifier.testTag("geoSharePointOpenSheetItem_${appOutputs.defaultOutput.id}"),
                enabled = appOutputs.defaultOutput.isEnabled(value),
                onClick = {
                    onHide()
                    onExecute(appOutputs.defaultOutput.toAction(value))
                },
                icon = appDetails[appOutputs.packageName]?.icon?.let { DrawableIconDescriptor(it) }
                    ?: PlaceholderIconDescriptor,
            )
        }
    }
}

@Composable
private fun ResultSheetSourceContent(
    appDetails: AppDetails,
    appsOutputs: List<AppOutputs<Output<String>>>,
    copyOutput: Output<String>,
    value: String,
    onHide: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    SheetSection(title = stringResource(R.string.conversion_succeeded_skip)) {
        OneLineSheetListItem(
            headlineText = copyOutput.getDescription(value).orEmpty(),
            onClick = {
                onHide()
                onExecute(copyOutput.toAction(value))
            },
            icon = ResourceIconDescriptor(R.drawable.content_copy_24px),
        )
    }
    if (appsOutputs.isNotEmpty()) {
        SheetSection(title = stringResource(R.string.main_source_open), first = false) {
            appsOutputs.forEach { appOutputs ->
                key(appOutputs.defaultOutput.id) {
                    SheetListItem(
                        headlineText = appDetails[appOutputs.packageName]?.label.orEmpty(),
                        modifier = Modifier.testTag("geoShareConversionUriSheetItem_${appOutputs.defaultOutput.id}"),
                        onClick = {
                            onHide()
                            onExecute(appOutputs.defaultOutput.toAction(value))
                        },
                        icon = appOutputs.defaultOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
                    )
                }
            }
        }
    }
}

// Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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
            ResultSheet(
                state = MutableStateFlow(
                    ResultSheetState.PointCopy(
                        appDetails = emptyMap(),
                        point = WGS84Point(NaivePoint.example),
                        pointIndex = 0,
                        totalPoints = 2,
                        outputs = outputRepository.getPointCopyOutputs(defaultFakeLinks),
                    )
                ),
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
            ResultSheet(
                state = MutableStateFlow(
                    ResultSheetState.PointCopy(
                        appDetails = emptyMap(),
                        point = WGS84Point(NaivePoint.example),
                        pointIndex = 0,
                        totalPoints = 2,
                        outputs = outputRepository.getPointCopyOutputs(defaultFakeLinks),
                    )
                ),
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun OpenPointPreview() {
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
            ResultSheet(
                state = MutableStateFlow(
                    ResultSheetState.PointOpen(
                        appDetails = emptyMap(),
                        point = WGS84Point(NaivePoint.example),
                        pointIndex = 2,
                        totalPoints = 3,
                        appsOutputs = outputRepository.getPointOpenOutputs(fakeActivities, emptySet()),
                    )
                ),
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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
            ResultSheet(
                state = MutableStateFlow(
                    ResultSheetState.PointSave(
                        appDetails = emptyMap(),
                        point = WGS84Point(NaivePoint.example),
                        pointIndex = 2,
                        totalPoints = 3,
                        outputs = outputRepository.pointSaveOutputs,
                    )
                ),
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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
            ResultSheet(
                state = MutableStateFlow(
                    ResultSheetState.PointsSave(
                        appDetails = emptyMap(),
                        points = persistentListOf(
                            WGS84Point(NaivePoint.genRandomPoint()),
                            WGS84Point(NaivePoint.genRandomPoint()),
                            WGS84Point(NaivePoint.example),
                        ),
                        outputs = outputRepository.pointsSaveOutputs,
                    )
                ),
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}
