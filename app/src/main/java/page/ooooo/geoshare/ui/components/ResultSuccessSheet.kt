package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultSuccessSheet(
    points: Points,
    selectedPointIndex: Int,
    appDetails: AppDetails,
    outputsForPoint: List<PointOutput>,
    outputsForPoints: List<PointsOutput>,
    onExecute: (action: Action<*>) -> Unit,
    onHide: () -> Unit,
) {
    val selectedPoint = points.getOrNull(selectedPointIndex) ?: return

    LazyColumn(
        Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("geoShareResultSuccessSheet"),
    ) {
        item {
            ResultSuccessSheetItemGroup(
                title = if (points.size > 1) {
                    stringResource(R.string.conversion_succeeded_point_number, selectedPointIndex + 1)
                } else {
                    null
                },
                appDetails = appDetails,
                actions = outputsForPoint.map { it.toAction(selectedPoint) },
                value = selectedPoint,
                onClick = { action ->
                    onHide()
                    onExecute(action)
                },
            )
        }
        item {
            Spacer(Modifier.height(LocalSpacing.current.mediumAdaptive))
        }
        item {
            ResultSuccessSheetItemGroup(
                title = if (points.size > 1) {
                    stringResource(R.string.conversion_succeeded_point_all, points.size)
                } else {
                    null
                },
                appDetails = appDetails,
                actions = outputsForPoints.map { it.toAction(points) },
                value = points,
                onClick = { action ->
                    onHide()
                    onExecute(action)
                },
            )
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
    Column {
        if (title != null) {
            LabelLarge(
                title,
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = LocalSpacing.current.smallAdaptive),
            )
        }
        var prevIcon: IconDescriptor? = null
        actions.forEach { action ->
            ResultSuccessSheetItem(
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

@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessSheet(
                points = persistentListOf(WGS84Point.example, WGS84Point.genRandomPoint()),
                selectedPointIndex = 1,
                appDetails = emptyMap(),
                outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks),
                outputsForPoints = outputRepository.getOutputsForPoints(),
                onHide = {},
                onExecute = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessSheet(
                points = persistentListOf(WGS84Point.example, WGS84Point.genRandomPoint()),
                selectedPointIndex = 1,
                appDetails = emptyMap(),
                outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks),
                outputsForPoints = outputRepository.getOutputsForPoints(),
                onHide = {},
                onExecute = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun LastPointPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessSheet(
                points = persistentListOf(WGS84Point.example),
                selectedPointIndex = 0,
                appDetails = emptyMap(),
                outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks),
                outputsForPoints = outputRepository.getOutputsForPoints(),
                onHide = {},
                onExecute = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkLastPointPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessSheet(
                points = persistentListOf(WGS84Point.example),
                selectedPointIndex = 0,
                appDetails = emptyMap(),
                outputsForPoint = outputRepository.getOutputsForPoint(defaultFakeLinks),
                outputsForPoints = outputRepository.getOutputsForPoints(),
                onHide = {},
                onExecute = {},
            )
        }
    }
}
