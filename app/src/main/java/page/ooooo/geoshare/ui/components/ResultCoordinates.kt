package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.formatters.CoordinateFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.inputs.GeoUriInputGroup
import page.ooooo.geoshare.data.inputs.GoogleMapsInputGroup
import page.ooooo.geoshare.data.inputs.InputGroup
import page.ooooo.geoshare.data.inputs.OpenStreetMapInputGroup
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
fun ResultCoordinates(
    point: Point,
    totalPoints: Int,
    coordinateConverter: CoordinateConverter,
    inputGroup: InputGroup,
    outputsForPointChips: StateFlow<List<OutputDetail<Point>>>,
    userPreferencesValues: StateFlow<UserPreferencesValues>,
    onExecute: (action: Action<*>) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onSelectPointToCopy: () -> Unit,
    onSelectPointToSave: () -> Unit,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column {
        ResultCoordinatesChecks(
            point = point,
            onNavigateToFaqScreen = onNavigateToFaqScreen,
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(
                Modifier.padding(vertical = spacing.windowPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                ResultCoordinatesBox(
                    point = point,
                    totalPoints = totalPoints,
                    coordinateConverter = coordinateConverter,
                    userPreferencesValues = userPreferencesValues,
                    onSelectPointToCopy = onSelectPointToCopy,
                    onSelectPointToSave = onSelectPointToSave,
                )
                ResultCoordinatesChips(
                    outputsForPointChips = outputsForPointChips,
                    onExecute = { output -> onExecute(output.toAction(point)) },
                )
            }
            content()
        }
        ResultCoordinatesAttribution(inputGroup)
    }
}

@Composable
private fun ResultCoordinatesChecks(
    point: Point,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val spacing = LocalSpacing.current

    if (!point.hasCoordinates()) {
        ResultCheck(
            buildAnnotatedString {
                append(stringResource(R.string.conversion_succeeded_check_name_only))
                append(" ")
                withLink(
                    LinkAnnotation.Clickable(tag = "link", styles = defaultLinkStyles()) {
                        onNavigateToFaqScreen(FaqItemId.NAME_ONLY)
                    }
                ) {
                    append(stringResource(R.string.faq_title))
                }
            },
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(bottom = spacing.tiny)
                .testTag("geoShareResultCheckNameOnly"),
        )
    } else if (!point.isAccurate()) {
        ResultCheck(
            stringResource(R.string.conversion_succeeded_check_srs),
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(bottom = spacing.tiny)
                .testTag("geoShareResultCheckSRS"),
        )
    } else {
        when (point.source) {
            Source.HTML_NOT_SEMANTIC, Source.JAVASCRIPT_STATE ->
                ResultCheck(
                    stringResource(R.string.conversion_succeeded_check_experimental),
                    Modifier
                        .padding(horizontal = spacing.windowPadding)
                        .padding(bottom = spacing.tiny)
                        .testTag("geoShareResultCheckExperimental"),
                )

            Source.MAP_CENTER ->
                ResultCheck(
                    stringResource(R.string.conversion_succeeded_check_map_center),
                    Modifier
                        .padding(horizontal = spacing.windowPadding)
                        .padding(bottom = spacing.tiny)
                        .testTag("geoShareResultCheckMapCenter"),
                )

            Source.API,
            Source.GENERATED,
            Source.GPS_SENSOR,
            Source.HASH,
            Source.HTML_SEMANTIC,
            Source.TEXT,
            Source.URI,
                -> {
            }
        }
    }
}

@Composable
private fun ResultCoordinatesBox(
    point: Point,
    totalPoints: Int,
    coordinateConverter: CoordinateConverter,
    userPreferencesValues: StateFlow<UserPreferencesValues>,
    onSelectPointToCopy: () -> Unit,
    onSelectPointToSave: () -> Unit,
) {
    val spacing = LocalSpacing.current

    val userPreferencesValues by userPreferencesValues.collectAsStateWithLifecycle()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = spacing.windowPadding, end = spacing.windowPadding - 7.dp)
            .testTag("geoShareResultSource_${point.source}"),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
            Text(
                pointTitle(
                    point = point,
                    pointIndex = totalPoints - 1,
                    totalPoints = totalPoints,
                ) ?: stringResource(R.string.conversion_succeeded_title),
                Modifier.testTag("geoShareResultName"),
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
                style = MaterialTheme.typography.headlineSmall,
            )
            if (point.hasCoordinates()) {
                SelectionContainer {
                    Text(
                        when (userPreferencesValues.coordinateFormat) {
                            CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                                coordinateConverter.toWGS84(point)
                            )

                            CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                                coordinateConverter.toWGS84(point)
                            )
                        },
                        Modifier.testTag("geoShareResultCoordinates"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        ResultPointCopyButton(onClick = onSelectPointToCopy)
        ResultPointSaveButton(onClick = onSelectPointToSave)
    }
}

@Composable
private fun ResultCoordinatesChips(
    outputsForPointChips: StateFlow<List<OutputDetail<Point>>>,
    onExecute: (output: Output<Point>) -> Unit,
) {
    val outputsForPointChips by outputsForPointChips.collectAsStateWithLifecycle()

    if (outputsForPointChips.isNotEmpty()) {
        ScrollableChips {
            outputsForPointChips.forEach { outputDetail ->
                key(outputDetail.output.id) {
                    StyledChip(
                        onClick = { onExecute(outputDetail.output) },
                        label = { Text(outputDetail.label()) },
                        modifier = Modifier.testTag("geoShareResultChip_${outputDetail.output.id}"),
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
private fun ColumnScope.ResultCoordinatesAttribution(inputGroup: InputGroup) {
    val spacing = LocalSpacing.current

    if (inputGroup is InputGroup.HasAttribution) {
        Column(
            Modifier
                .align(Alignment.End)
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.extraTiny)
        ) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                inputGroup.Attribution()
            }
        }
    }
}

// Previews

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
            ResultCoordinates(
                point = coordinateConverter.toGCJ02(WGS84Point.ForbiddenCity),
                totalPoints = 1,
                coordinateConverter = coordinateConverter,
                inputGroup = OpenStreetMapInputGroup,
                outputsForPointChips = MutableStateFlow(
                    outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelectPointToCopy = {},
                onSelectPointToSave = {},
            ) {}
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
            ResultCoordinates(
                point = coordinateConverter.toGCJ02(WGS84Point.ForbiddenCity),
                totalPoints = 1,
                coordinateConverter = coordinateConverter,
                inputGroup = OpenStreetMapInputGroup,
                outputsForPointChips = MutableStateFlow(
                    outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelectPointToCopy = {},
                onSelectPointToSave = {},
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeveralPointsPreview() {
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
            ResultCoordinates(
                point = WGS84Point(NaivePoint.example.copy(source = Source.HTML_NOT_SEMANTIC)),
                totalPoints = 2,
                coordinateConverter = coordinateConverter,
                inputGroup = GoogleMapsInputGroup,
                outputsForPointChips = MutableStateFlow(
                    outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelectPointToCopy = {},
                onSelectPointToSave = {},
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NameOnlyPreview() {
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
            ResultCoordinates(
                point = WGS84Point(name = "Berlin, Germany", z = 13.0, source = Source.URI),
                totalPoints = 1,
                coordinateConverter = coordinateConverter,
                inputGroup = GeoUriInputGroup,
                outputsForPointChips = MutableStateFlow(
                    outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(defaultFakeUserPreferences),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelectPointToCopy = {},
                onSelectPointToSave = {},
            ) {}
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
            ResultCoordinates(
                point = WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                totalPoints = 1,
                coordinateConverter = coordinateConverter,
                inputGroup = GeoUriInputGroup,
                outputsForPointChips = MutableStateFlow(
                    outputRepository.getOutputsForPointChips(defaultFakeLinks).map { it.toOutputDetail(appDetails) }
                ),
                userPreferencesValues = MutableStateFlow(
                    defaultFakeUserPreferences.copy(coordinateFormat = CoordinateFormat.DEG_MIN_SEC)
                ),
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelectPointToCopy = {},
                onSelectPointToSave = {},
            ) {}
        }
    }
}
