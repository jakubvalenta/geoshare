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
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
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
import page.ooooo.geoshare.data.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.OutputDetail
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.defaultLinkStyles
import page.ooooo.geoshare.ui.toOutputDetail

@Composable
fun ResultPoints(
    points: Points,
    coordinateConverter: CoordinateConverter,
    outputsForPointsChips: StateFlow<List<OutputDetail<Points>>>,
    userPreferencesValues: StateFlow<UserPreferencesValues>,
    initialExpanded: Boolean = false,
    onExecute: (action: Action<*>) -> Unit,
    onOpenPoint: (point: Point) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onSelectPointToCopy: (point: Point, pointIndex: Int) -> Unit,
    onSelectPointToSave: (point: Point, pointIndex: Int) -> Unit,
    onSelectPointsToSave: () -> Unit,
    onSelectPointsToShare: () -> Unit,
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
                userPreferencesValues = userPreferencesValues,
                initialExpanded = initialExpanded,
                onOpenPoint = onOpenPoint,
                onSelectPointToCopy = onSelectPointToCopy,
                onSelectPointToSave = onSelectPointToSave,
                onSelectPointsToSave = onSelectPointsToSave,
                onSelectPointsToShare = onSelectPointsToShare,
            )
            ResultPointsChips(
                outputsForPointsChips = outputsForPointsChips,
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
    userPreferencesValues: StateFlow<UserPreferencesValues>,
    initialExpanded: Boolean = false,
    onOpenPoint: (point: Point) -> Unit,
    onSelectPointToCopy: (point: Point, pointIndex: Int) -> Unit,
    onSelectPointToSave: (point: Point, pointIndex: Int) -> Unit,
    onSelectPointsToSave: () -> Unit,
    onSelectPointsToShare: () -> Unit,
) {
    val spacing = LocalSpacing.current

    // TODO Why does ResultPointsBox expand so slow?
    var expanded by retain { mutableStateOf(initialExpanded) }
    val userPreferencesValues by userPreferencesValues.collectAsStateWithLifecycle()

    Column {
        Row(
            Modifier.padding(start = spacing.windowPadding, end = spacing.windowPadding - 7.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny),
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .toggleable(value = expanded, onValueChange = { expanded = !expanded }),
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
            ResultPointShareButton(onClick = onSelectPointsToShare)
            ResultPointSaveButton(onClick = onSelectPointsToSave)
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
                        coordinateFormat = userPreferencesValues.coordinateFormat,
                        coordinateConverter = coordinateConverter,
                        onOpenPoint = { onOpenPoint(point) },
                        onSelectPointToCopy = { onSelectPointToCopy(point, pointIndex) },
                        onSelectPointToSave = { onSelectPointToSave(point, pointIndex) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultPointsChips(
    outputsForPointsChips: StateFlow<List<OutputDetail<Points>>>,
    onExecute: (output: Output<Points>) -> Unit,
) {
    val outputsForPointsChips by outputsForPointsChips.collectAsStateWithLifecycle()

    if (outputsForPointsChips.isNotEmpty()) {
        ScrollableChips(Modifier.testTag("geoShareResultPointsChips")) {
            outputsForPointsChips.forEach { outputDetail ->
                key(outputDetail.output.id) {
                    StyledChip(
                        onClick = { onExecute(outputDetail.output) },
                        label = { Text(outputDetail.label()) },
                        icon = outputDetail.icon?.let {
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
    coordinateFormat: CoordinateFormat,
    coordinateConverter: CoordinateConverter,
    onOpenPoint: () -> Unit,
    onSelectPointToCopy: () -> Unit,
    onSelectPointToSave: () -> Unit,
) {
    val spacing = LocalSpacing.current

    ListItem(
        supportingContent = if (point.hasCoordinates()) {
            {
                SelectionContainer {
                    Text(
                        when (coordinateFormat) {
                            CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                                coordinateConverter.toWGS84(point)
                            )

                            CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                                coordinateConverter.toWGS84(point)
                            )
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
                    onClick = onOpenPoint,
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
                ResultPointCopyButton(onClick = onSelectPointToCopy)
                ResultPointSaveButton(onClick = onSelectPointToSave)
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
            val appDetails = getFakeAppDetails(context)
            ResultPoints(
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
                outputsForPointsChips = MutableStateFlow(
                    outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                initialExpanded = true,
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = {},
                onSelectPointToCopy = { _, _ -> },
                onSelectPointToSave = { _, _ -> },
                onSelectPointsToSave = {},
                onSelectPointsToShare = {},
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
            val appDetails = getFakeAppDetails(context)
            ResultPoints(
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
                outputsForPointsChips = MutableStateFlow(
                    outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                initialExpanded = true,
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = {},
                onSelectPointToCopy = { _, _ -> },
                onSelectPointToSave = { _, _ -> },
                onSelectPointsToSave = {},
                onSelectPointsToShare = {},
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
            val appDetails = getFakeAppDetails(context)
            ResultPoints(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                coordinateConverter = coordinateConverter,
                outputsForPointsChips = MutableStateFlow(
                    outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
                ),
                initialExpanded = true,
                userPreferencesValues = MutableStateFlow(
                    defaultFakeUserPreferences.copy(coordinateFormat = CoordinateFormat.DEG_MIN_SEC)
                ),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = {},
                onSelectPointToCopy = { _, _ -> },
                onSelectPointToSave = { _, _ -> },
                onSelectPointsToSave = {},
                onSelectPointsToShare = {},
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
            val appDetails = getFakeAppDetails(context)
            ResultPoints(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                coordinateConverter = coordinateConverter,
                outputsForPointsChips = MutableStateFlow(
                    outputRepository.getOutputsForPointsChips().map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onOpenPoint = {},
                onSelectPointToCopy = { _, _ -> },
                onSelectPointToSave = { _, _ -> },
                onSelectPointsToSave = {},
                onSelectPointsToShare = {},
            )
        }
    }
}
