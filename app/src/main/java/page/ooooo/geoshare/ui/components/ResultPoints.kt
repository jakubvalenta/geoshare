package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.formatters.CoordinateFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.preferences.CoordinateFormat
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.ResultPointsState
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.defaultLinkStyles

@Composable
fun ResultPoints(
    state: StateFlow<ResultPointsState>,
    points: Points,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: StateFlow<CoordinateFormat>,
    initialExpanded: Boolean = false,
    onExecute: (action: Action<*>) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onCopyPoint: (point: Point, pointIndex: Int) -> Unit,
    onOpenPoint: (point: Point, pointIndex: Int) -> Unit,
    onSavePoint: (point: Point, pointIndex: Int) -> Unit,
    onSavePoints: (points: Points) -> Unit,
    onSharePoints: (points: Points) -> Unit,
) {
    val spacing = LocalSpacing.current

    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(
            Modifier.padding(vertical = spacing.windowPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            ResultPointsChecks(
                points = points,
                onNavigateToFaqScreen = onNavigateToFaqScreen,
            )
            ResultPointsBox(
                points = points,
                coordinateConverter = coordinateConverter,
                coordinateFormat = coordinateFormat,
                initialExpanded = initialExpanded,
                onCopyPoint = onCopyPoint,
                onOpenPoint = onOpenPoint,
                onSavePoint = onSavePoint,
                onSavePoints = onSavePoints,
                onSharePoints = onSharePoints,
            )
            ResultPointsChips(
                state = state,
                onExecute = { output -> onExecute(output.toAction(points)) },
            )
        }
    }
}

@Composable
private fun ResultPointsChecks(
    points: Points,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val spacing = LocalSpacing.current

    if (points.any { !it.hasCoordinates() }) {
        ResultCheck(
            buildAnnotatedString {
                append(stringResource(R.string.conversion_succeeded_check_name_only_points))
                append(" ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "link",
                        styles = defaultLinkStyles()
                    ) {
                        onNavigateToFaqScreen(FaqItemId.NAME_ONLY)
                    }
                ) {
                    append(stringResource(R.string.faq_title))
                }
            },
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .testTag("geoShareResultCheckNameOnlyPoints"),
        )
    }
}

@Composable
private fun ResultPointsBox(
    points: Points,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: StateFlow<CoordinateFormat>,
    initialExpanded: Boolean = false,
    onCopyPoint: (point: Point, pointIndex: Int) -> Unit,
    onOpenPoint: (point: Point, pointIndex: Int) -> Unit,
    onSavePoint: (point: Point, pointIndex: Int) -> Unit,
    onSavePoints: (points: Points) -> Unit,
    onSharePoints: (points: Points) -> Unit,
) {
    val spacing = LocalSpacing.current

    // TODO Why does ResultPointsBox expand so slow?
    val coordinateFormat by coordinateFormat.collectAsStateWithLifecycle()
    var expanded by retain { mutableStateOf(initialExpanded) }

    Column {
        Row(
            Modifier.padding(start = spacing.windowPadding, end = spacing.windowPadding - 7.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny),
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .toggleable(value = expanded, onValueChange = { expanded = !expanded })
                    .testTag("geoShareResultPoints"),
                horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.conversion_succeeded_point_all, points.size),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("geoShareResultPointsHeadline"),
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
            ResultPointShareButton(
                onClick = { onSharePoints(points) },
                modifier = Modifier.testTag("geoShareResultSharePoints"),
            )
            ResultPointSaveButton(
                onClick = { onSavePoints(points) },
                modifier = Modifier.testTag("geoShareResultSavePoints"),
            )
        }
        AnimatedVisibility(
            expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(Modifier.padding(top = spacing.tiny)) {
                points.forEachIndexed { pointIndex, point ->
                    ResultPoint(
                        point = point,
                        pointIndex = pointIndex,
                        coordinateConverter = coordinateConverter,
                        coordinateFormat = coordinateFormat,
                        onCopyPoint = onCopyPoint,
                        onOpenPoint = onOpenPoint,
                        onSavePoint = onSavePoint,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultPointsChips(
    state: StateFlow<ResultPointsState>,
    onExecute: (output: Output<Points>) -> Unit,
) {
    val state by state.collectAsStateWithLifecycle()

    if (state.chipsOutputs.isNotEmpty()) {
        ScrollableChips(Modifier.testTag("geoShareResultPointsChips")) {
            state.chipsOutputs.forEach { output ->
                key(output.id) {
                    StyledChip(
                        onClick = { onExecute(output) },
                        label = { Text(output.label(state.appDetails)) },
                        icon = output.getIcon(state.appDetails)?.let {
                            { IconFromDescriptor(it, contentDescription = null) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultPoint(
    point: Point,
    pointIndex: Int,
    coordinateConverter: CoordinateConverter,
    coordinateFormat: CoordinateFormat,
    onCopyPoint: (point: Point, pointIndex: Int) -> Unit,
    onOpenPoint: (point: Point, pointIndex: Int) -> Unit,
    onSavePoint: (point: Point, pointIndex: Int) -> Unit,
) {
    val spacing = LocalSpacing.current

    ListItem(
        supportingContent = if (point.hasCoordinates()) {
            {
                SelectionContainer {
                    Text(
                        when (coordinateFormat) {
                            CoordinateFormat.DEC ->
                                CoordinateFormatter.formatDecCoords(coordinateConverter.toWGS84(point))

                            CoordinateFormat.DEG_MIN_SEC ->
                                CoordinateFormatter.formatDegMinSecCoords(coordinateConverter.toWGS84(point))
                        }
                    )
                }
            }
        } else {
            null
        },
        trailingContent = {
            Row {
                Button(
                    onClick = { onOpenPoint(point, pointIndex) },
                    modifier = Modifier
                        .height(28.dp)
                        .padding(end = spacing.tiny),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                    contentPadding = PaddingValues(horizontal = spacing.small),
                ) {
                    Text(stringResource(R.string.open_with))
                }
                ResultPointCopyButton(
                    onClick = { onCopyPoint(point, pointIndex) },
                )
                ResultPointSaveButton(
                    onClick = { onSavePoint(point, pointIndex) },
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(
            start = spacing.windowPadding,
            top = spacing.small,
            end = spacing.windowPadding - 7.dp,
            bottom = spacing.small,
        ),
    ) {
        SelectionContainer {
            Text(
                point.cleanName ?: stringResource(R.string.conversion_succeeded_point_number, pointIndex + 1)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
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
            ResultPoints(
                state = MutableStateFlow(
                    ResultPointsState(
                        appDetails = getFakeAppDetails(context),
                        chipsOutputs = outputRepository.pointsChipsOutputs,
                    )
                ),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(name = "Central Park", source = Source.GENERATED),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
                coordinateConverter = coordinateConverter,
                coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
                initialExpanded = true,
                onCopyPoint = { _, _ -> },
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = { _, _ -> },
                onSavePoint = { _, _ -> },
                onSavePoints = {},
                onSharePoints = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
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
            ResultPoints(
                state = MutableStateFlow(
                    ResultPointsState(
                        appDetails = getFakeAppDetails(context),
                        chipsOutputs = outputRepository.pointsChipsOutputs,
                    )
                ),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(name = "Central Park", source = Source.GENERATED),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(
                        NaivePoint.example,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098",
                    ),
                ),
                coordinateConverter = coordinateConverter,
                coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
                initialExpanded = true,
                onCopyPoint = { _, _ -> },
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = { _, _ -> },
                onSavePoint = { _, _ -> },
                onSavePoints = {},
                onSharePoints = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinateFormatPreview() {
    AppTheme {
        Surface {
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
            ResultPoints(
                state = MutableStateFlow(
                    ResultPointsState(
                        appDetails = getFakeAppDetails(context),
                        chipsOutputs = outputRepository.pointsChipsOutputs,
                    )
                ),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                coordinateConverter = coordinateConverter,
                coordinateFormat = MutableStateFlow(CoordinateFormat.DEG_MIN_SEC),
                initialExpanded = true,
                onCopyPoint = { _, _ -> },
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = { _, _ -> },
                onSavePoint = { _, _ -> },
                onSavePoints = {},
                onSharePoints = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollapsedPreview() {
    AppTheme {
        Surface {
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
            ResultPoints(
                state = MutableStateFlow(
                    ResultPointsState(
                        appDetails = getFakeAppDetails(context),
                        chipsOutputs = outputRepository.pointsChipsOutputs,
                    )
                ),
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                coordinateConverter = coordinateConverter,
                coordinateFormat = MutableStateFlow(CoordinateFormat.DEC),
                onCopyPoint = { _, _ -> },
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = { _, _ -> },
                onSavePoint = { _, _ -> },
                onSavePoints = {},
                onSharePoints = {},
            )
        }
    }
}
