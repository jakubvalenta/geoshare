package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessCoordinates(
    points: Points,
    appDetails: AppDetails,
    coordinateFormat: CoordinateFormat,
    coordinateConverter: CoordinateConverter,
    outputsForPointChips: List<PointOutput>,
    outputsForPointsChips: List<PointsOutput>,
    onExecute: (action: Action<*>) -> Unit,
    onSelect: (index: Int?) -> Unit,
    initialExpanded: Boolean = false,
) {
    val lastPoint = points.lastOrNull() ?: return
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(initialExpanded) }

    Column {
        Headline(
            lastPoint.cleanName
                ?: if (points.size > 1) {
                    stringResource(R.string.conversion_succeeded_point_last)
                } else {
                    stringResource(R.string.conversion_succeeded_title)
                },
            Modifier
                .testTag("geoShareResultSuccessLastPointName")
                .padding(horizontal = spacing.windowPadding)
                .padding(top = 4.dp), // Align with the "Open with..." headline on wide screen
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = spacing.windowPadding, end = spacing.windowPadding - 10.dp)
                .testTag("geoShareResultSuccessLastPointSource_${lastPoint.source}"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (lastPoint.hasCoordinates()) {
                SelectionContainer {
                    Text(
                        when (coordinateFormat) {
                            CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                                coordinateConverter.toWGS84(lastPoint)
                            )

                            CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                                coordinateConverter.toWGS84(lastPoint)
                            )
                        },
                        Modifier
                            .weight(1f)
                            .testTag("geoShareResultSuccessLastPointCoordinates"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                ParagraphText(
                    stringResource(R.string.conversion_succeeded_description_q_only),
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("geoShareResultSuccessLastPointDescription"),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(
                { onSelect(points.size - 1) },
                Modifier.testTag("geoShareResultSuccessLastPointMenu")
            ) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
        if (!lastPoint.isAccurate()) {
            ResultSuccessCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_srs),
                Modifier.testTag("geoShareResultSuccessLastPointCheckSRS"),
            )
        } else if (lastPoint.source == Source.JAVASCRIPT) {
            ResultSuccessCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_experimental),
                Modifier.testTag("geoShareResultSuccessLastPointCheckJavaScript"),
            )
        } else if (lastPoint.source == Source.MAP_CENTER) {
            ResultSuccessCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_map_center),
                Modifier.testTag("geoShareResultSuccessLastPointCheckMapCenter"),
            )
        }
        if (outputsForPointChips.isNotEmpty()) {
            ScrollableChips {
                outputsForPointChips.forEach { output ->
                    item {
                        StyledChip(
                            label = output.label(appDetails),
                            icon = output.getIcon(appDetails)?.let {
                                { IconFromDescriptor(it, contentDescription = null) }
                            },
                        ) {
                            onExecute(output.toAction(lastPoint))
                        }
                    }
                }
            }
        }
        points.takeIf { points.size > 1 }?.let { points ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    Modifier.padding(top = spacing.mediumAdaptive),
                    verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive),
                ) {
                    ExpandablePane(
                        expanded = expanded,
                        title = {
                            Text(
                                stringResource(R.string.conversion_succeeded_point_all, points.size),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("geoShareResultSuccessAllPointsHeadline"),
                            )
                        },
                        onSetExpanded = { expanded = it },
                        modifier = Modifier.padding(horizontal = spacing.windowPadding),
                    ) {
                        Column(
                            Modifier.padding(
                                top = spacing.mediumAdaptive,
                                end = 10.dp, // Align with expand/collapse icon
                            ),
                            verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)
                        ) {
                            points.forEachIndexed { index, point ->
                                ResultSuccessPoint(
                                    point = point,
                                    index = index,
                                    coordinateFormat = coordinateFormat,
                                    coordinateConverter = coordinateConverter,
                                    onSelect = { onSelect(index) },
                                )
                            }
                        }
                    }
                    if (outputsForPointsChips.isNotEmpty()) {
                        ScrollableChips {
                            outputsForPointsChips.forEach { output ->
                                item {
                                    StyledChip(
                                        label = output.label(appDetails),
                                        icon = output.getIcon(appDetails)?.let {
                                            { IconFromDescriptor(it, contentDescription = null) }
                                        },
                                    ) {
                                        onExecute(output.toAction(points))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSuccessCoordinatesCheck(
    text: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    ParagraphText(
        text,
        modifier.padding(horizontal = spacing.windowPadding),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodySmall,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DescriptionPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0, source = Source.URI)),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDescriptionPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0, source = Source.URI)),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.example),
                    GCJ02Point(31.22850685422705, 121.47552456472106, z = 11.0, source = Source.MAP_CENTER),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.example),
                    GCJ02Point(31.22850685422705, 121.47552456472106, z = 11.0, source = Source.MAP_CENTER),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsWithNamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                onExecute = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsWithNamePreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            ResultSuccessCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                appDetails = emptyMap(),
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                onExecute = {},
                onSelect = {},
            )
        }
    }
}
