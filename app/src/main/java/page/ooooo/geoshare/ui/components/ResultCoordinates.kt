package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.ActionContext
import page.ooooo.geoshare.lib.outputs.OpenPointOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun ResultCoordinates(
    points: Points,
    appDetails: AppDetails,
    coordinateFormat: CoordinateFormat,
    coordinateConverter: CoordinateConverter,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    outputsForApps: Map<String, List<Output>>,
    outputsForPointChips: List<PointOutput>,
    outputsForPointsChips: List<PointsOutput>,
    sourceComesFromIntent: StateFlow<Boolean>,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onExecute: (action: Action<*>) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
    onSelect: (index: Int?) -> Unit,
    initialExpanded: Boolean = false,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val lastPoint = points.lastOrNull() ?: return
    val spacing = LocalSpacing.current

    var expanded by remember { mutableStateOf(initialExpanded) }
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = spacing.windowPadding, end = spacing.windowPadding - 10.dp)
                .testTag("geoShareResultLastPointSource_${lastPoint.source}"),
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
                            .testTag("geoShareResultLastPointCoordinates"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                ResultCoordinatesCheck(
                    buildAnnotatedString {
                        append(stringResource(R.string.conversion_succeeded_check_name_only))
                        append(" ")
                        ClickableLink(stringResource(R.string.faq_title)) {
                            onNavigateToFaqScreen(FaqItemId.NAME_ONLY)
                        }
                    },
                    Modifier
                        .weight(1f)
                        .testTag("geoShareResultCheckNameOnly"),
                )
            }
            IconButton(
                { onSelect(points.size - 1) },
                Modifier.testTag("geoShareResultLastPointMenu")
            ) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.nav_menu_content_description),
                )
            }
        }
        if (!lastPoint.isAccurate()) {
            ResultCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_srs),
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .testTag("geoShareResultCheckSRS"),
            )
        } else if (lastPoint.source == Source.JAVASCRIPT) {
            ResultCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_experimental),
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .testTag("geoShareResultCheckExperimental"),
            )
        } else if (lastPoint.source == Source.MAP_CENTER) {
            ResultCoordinatesCheck(
                stringResource(R.string.conversion_succeeded_check_map_center),
                Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .testTag("geoShareResultCheckMapCenter"),
            )
        }
        if (outputsForPointChips.isNotEmpty()) {
            ScrollableChips(
                paddingValues = PaddingValues(
                    start = spacing.windowPadding,
                    end = spacing.windowPadding,
                    bottom = spacing.extraTiny,
                ),
            ) {
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
        if (!sourceComesFromIntent) {
            val examplePoint = WGS84Point.Kilimanjaro

            /**
             * An output that opens a point in a map app.
             *
             * The map app is the first installed app from a list of common map apps.
             */
            val exampleAppOutput = setOf(
                PackageNames.GOOGLE_MAPS,
                PackageNames.OSMAND_PLUS,
                PackageNames.COMAPS_FDROID,
                PackageNames.ORGANIC_MAPS,
                PackageNames.MAPY_COM,
                PackageNames.HERE_WEGO,
                PackageNames.MAGIC_EARTH,
                PackageNames.MAPS_ME,
            ).firstNotNullOfOrNull { packageName ->
                outputsForApps[packageName]?.firstNotNullOfOrNull { it as? OpenPointOutput }
            }
            HelpMessageCard(
                helpMessage = HelpMessage.SHARE_SOURCE,
                dismissedHelpMessages = dismissedHelpMessages,
                title = { Text(stringResource(R.string.help_share_source_title)) },
                actionText = exampleAppOutput?.let { exampleAppOutput ->
                    appDetails[exampleAppOutput.packageName]?.label?.let { exampleAppLabel ->
                        {
                            stringResource(R.string.help_share_source_action, exampleAppLabel)
                        }
                    }
                },
                onAction = {
                    exampleAppOutput?.let { exampleAppOutput ->
                        val actionContext = ActionContext(
                            context = context, clipboard = clipboard, resources = resources
                        )
                        coroutineScope.launch {
                            exampleAppOutput.toAction(examplePoint).execute(actionContext)
                        }
                    }
                },
                onDismiss = onDismissHelpMessage,
                modifier = Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(bottom = spacing.tiny + spacing.extraTiny),
            ) {
                val shareIconId = "shareIcon"
                val shareIconSize = 14.sp
                ParagraphText(
                    annotatedStringResource(
                        R.string.help_share_source_text,
                        FormatArg.InlineContent(shareIconId),
                        FormatArg.Text(stringResource(R.string.app_name)),
                    ),
                    inlineContent = mapOf(
                        shareIconId to InlineTextContent(
                            Placeholder(
                                width = 14.sp,
                                height = 14.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                            )
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                Modifier.requiredSize(with(LocalDensity.current) { shareIconSize.toDp() }),
                            )
                        }
                    )
                )
            }
        }
        points.takeIf { points.size > 1 }?.let { points ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(Modifier.padding(top = spacing.small)) {
                    ExpandablePane(
                        expanded = expanded,
                        onSetExpanded = { expanded = it },
                        title = {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.conversion_succeeded_point_all, points.size),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.testTag("geoShareResultPointsHeadline"),
                                )
                                if (points.any { !it.hasCoordinates() }) {
                                    ResultCoordinatesCheck(
                                        buildAnnotatedString {
                                            append(stringResource(R.string.conversion_succeeded_check_name_only_points))
                                            append(" ")
                                            ClickableLink(stringResource(R.string.faq_title)) {
                                                onNavigateToFaqScreen(FaqItemId.NAME_ONLY)
                                            }
                                        },
                                        Modifier
                                            .padding(top = spacing.tiny)
                                            .testTag("geoShareResultCheckNameOnlyPoints"),
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = spacing.windowPadding)
                            .testTag("geoShareResultPoints"),
                    ) {
                        Column(Modifier.padding(top = spacing.tiny)) {
                            points.forEachIndexed { index, point ->
                                ResultPoint(
                                    point = point,
                                    index = index,
                                    coordinateFormat = coordinateFormat,
                                    coordinateConverter = coordinateConverter,
                                    onSelect = { onSelect(index) },
                                )
                                if (index < points.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    if (outputsForPointsChips.isNotEmpty()) {
                        ScrollableChips(Modifier.testTag("geoShareResultPointsChips")) {
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
private fun ResultCoordinatesCheck(modifier: Modifier = Modifier, block: @Composable () -> Unit) {
    Row(modifier) {
        Icon(
            painterResource(R.drawable.warning_24px),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 6.dp)
                .requiredSize(16.dp),
        )
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium) {
            block()
        }
    }
}

@Composable
private fun ResultCoordinatesCheck(text: String, modifier: Modifier = Modifier) {
    ResultCoordinatesCheck(modifier) {
        ParagraphText(text)
    }
}

@Composable
private fun ResultCoordinatesCheck(text: AnnotatedString, modifier: Modifier = Modifier) {
    ResultCoordinatesCheck(modifier) {
        ParagraphText(text)
    }
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
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
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
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DescriptionPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0, source = Source.URI)),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDescriptionPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(WGS84Point(name = "Berlin, Germany", z = 13.0, source = Source.URI)),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NamePreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.example),
                    GCJ02Point(31.22850685422705, 121.47552456472106, z = 11.0, source = Source.MAP_CENTER),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNamePreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.example),
                    GCJ02Point(31.22850685422705, 121.47552456472106, z = 11.0, source = Source.MAP_CENTER),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(name = "Central Park", source = Source.GENERATED),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(name = "Central Park", source = Source.GENERATED),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsWithNamePreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsWithNamePreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultCoordinates(
                points = persistentListOf(
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint()),
                    WGS84Point(NaivePoint.genRandomPoint(name = "Berlin, Germany", z = 13.0)),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        packageName = PackageNames.OSMAND_PLUS,
                        label = "OsmAnd",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                coordinateFormat = CoordinateFormat.DEG_MIN_SEC,
                coordinateConverter = coordinateConverter,
                dismissedHelpMessages = MutableStateFlow(null),
                outputsForApps = outputRepository.getOutputsForApps(
                    mapOf(
                        PackageNames.OSMAND_PLUS to App(
                            packageName = PackageNames.OSMAND_PLUS,
                            dataTypes = setOf(DataType.GEO_URI)
                        ),
                    ),
                    hiddenApps = emptySet(),
                ),
                outputsForPointChips = outputRepository.getOutputsForPointChips(defaultFakeLinks),
                outputsForPointsChips = outputRepository.getOutputsForPointsChips(),
                initialExpanded = true,
                sourceComesFromIntent = MutableStateFlow(false),
                onDismissHelpMessage = {},
                onExecute = {},
                onNavigateToFaqScreen = {},
                onSelect = {},
            )
        }
    }
}
