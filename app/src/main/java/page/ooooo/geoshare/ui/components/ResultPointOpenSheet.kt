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
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.PlaceholderIconDescriptor
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.OutputDetailForApp
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputDetailForApp
import page.ooooo.geoshare.ui.toOutputDetailsForApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ResultPointOpenSheet(
    outputs: List<OutputDetailForApp<T>>,
    title: String?,
    value: T,
    initialSheetValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    // TODO Add behavior test
    val sheetState = rememberBottomSheetState(initialSheetValue)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("geoShareResultOpenPointSheet"),
        sheetState = sheetState,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SheetSection(title = title) {
                outputs.forEach { outputDetail ->
                    key(outputDetail.output.id) {
                        SheetListItem(
                            headlineText = outputDetail.label.orEmpty(),
                            modifier = Modifier.testTag("geoSharePointOpenSheetItem_${outputDetail.output.id}"),
                            onClick = {
                                onDismissRequest()
                                onExecute(outputDetail.output.toAction(value))
                            },
                            icon = outputDetail.icon ?: PlaceholderIconDescriptor,
                        )
                    }
                }
            }
        }
    }
}

// Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
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
            val activities = fakeActivities.filter {
                it.packageName in setOf(
                    PackageNames.COMAPS_FDROID,
                    PackageNames.CONVERSATIONS,
                    PackageNames.OSMAND_PLUS,
                )
            }
            val appDetails = getFakeAppDetails(context)
            val point = WGS84Point(NaivePoint.example)
            ResultPointOpenSheet(
                outputs = outputRepository.getOutputsForPointOpen(activities, emptySet())
                    .toOutputDetailsForApp(appDetails),
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
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
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
            val activities = fakeActivities.filter {
                it.packageName in setOf(
                    PackageNames.COMAPS_FDROID,
                    PackageNames.CONVERSATIONS,
                    PackageNames.OSMAND_PLUS,
                )
            }
            val appDetails = getFakeAppDetails(context)
            val point = WGS84Point(NaivePoint.example)
            ResultPointOpenSheet(
                outputs = outputRepository.getOutputsForPointOpen(activities, emptySet())
                    .toOutputDetailsForApp(appDetails),
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
@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
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
            val activities = fakeActivities.filter {
                it.packageName in setOf(
                    PackageNames.COMAPS_FDROID,
                    PackageNames.CONVERSATIONS,
                    PackageNames.OSMAND_PLUS,
                )
            }
            val point = WGS84Point(NaivePoint.example)
            ResultPointOpenSheet(
                outputs = outputRepository.getOutputsForPointOpen(activities, emptySet())
                    .map { it.toOutputDetailForApp(emptyMap()) },
                title = pointTitle(point, pointIndex = 2, totalPoints = 3),
                value = point,
                initialSheetValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}
