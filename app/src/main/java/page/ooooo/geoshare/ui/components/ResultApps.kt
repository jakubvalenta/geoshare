package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.SendPointOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultApps(
    appDetails: AppDetails,
    outputsForApps: Map<String, List<Output>>,
    outputsForLinks: Map<String?, List<Output>>,
    outputsForSharing: List<Output>,
    points: Points,
    iconSize: Dp = 46.dp,
    onDisableLinkGroup: (group: String?) -> Unit,
    onExecute: (Action<*>) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
) {
    val lastPoint = points.lastOrNull() ?: return
    val onClick = { output: Output ->
        onExecute(
            when (output) {
                is PointOutput -> output.toAction(lastPoint)
                is PointsOutput -> output.toAction(points)
            }
        )
    }
    val (outputsForMapApps, outputsForMessagingApps) = outputsForApps.entries.partition { (_, outputs) ->
        outputs.size != 1 || outputs[0] !is SendPointOutput
    }

    // Map apps
    ResultAppsGrid(
        outputsForApps = outputsForMapApps,
        appDetails = appDetails,
        iconSize = iconSize,
        onClick = onClick,
        onHideApp = onHideApp,
    ) {
        // Share item
        item {
            AppIcon(
                modifier = Modifier
                    .weight(1f)
                    .testTag("geoShareApp_share"),
                label = null,
                appDetails = appDetails,
                outputs = outputsForSharing,
                onClick = onClick,
            ) {
                Surface(
                    Modifier.requiredSize(iconSize),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    outputsForSharing.firstOrNull()?.let { firstOutput ->
                        firstOutput.getIcon(appDetails)?.let { icon ->
                            IconFromDescriptor(icon, contentDescription = firstOutput.label(appDetails), size = 24.dp)
                        }
                    }
                }
            }
        }
    }

    // Messaging apps
    if (outputsForMessagingApps.isNotEmpty()) {
        ResultAppsHeadline(stringResource(R.string.output_send))
        ResultAppsGrid(
            outputsForApps = outputsForMessagingApps,
            appDetails = appDetails,
            iconSize = iconSize,
            onClick = onClick,
            onHideApp = onHideApp,
        )
    }

    // Links
    if (outputsForLinks.isNotEmpty()) {
        ResultAppsHeadline(stringResource(R.string.links_title)) {
            IconButton({ onNavigateToLinkScreen() }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.conversion_succeeded_apps_links_configure),
                    Modifier.requiredSize(24.dp),
                )
            }
        }
        ResultAppsLinksGrid(
            outputsForLinks = outputsForLinks,
            appDetails = appDetails,
            iconSize = iconSize,
            onClick = onClick,
            onDisableLinkGroup = onDisableLinkGroup,
        )
    }
}

@Composable
private fun ResultAppsHeadline(text: String, extra: (@Composable RowScope.() -> Unit)? = null) {
    val spacing = LocalSpacing.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(
                start = spacing.windowPadding,
                top = spacing.tinyAdaptive,
                end = spacing.windowPadding - 8.dp, // Align with last point menu
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        extra?.invoke(this)
    }
}

@Composable
private fun ResultAppsGrid(
    outputsForApps: List<Map.Entry<String, List<Output>>>,
    appDetails: AppDetails,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    extra: (GridScope.() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current

    Grid(Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.smallAdaptive)) {
        outputsForApps
            .map { (packageName, outputs) -> Triple(packageName, appDetails[packageName]?.label, outputs) }
            .sortedWith(compareBy(nullsLast()) { (_, label) -> label })
            .forEach { (packageName, label, outputs) ->
                item {
                    AppIcon(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("geoShareApp_$packageName"),
                        label = label,
                        appDetails = appDetails,
                        outputs = outputs,
                        onClick = onClick,
                        onHide = { onHideApp(packageName) },
                    ) {
                        outputs.firstOrNull()?.getIcon(appDetails)
                            ?.let { IconFromDescriptor(it, contentDescription = null, size = iconSize) }
                            ?: Box(
                                Modifier
                                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                                    .requiredSize(iconSize)
                            )
                    }
                }
            }
        extra?.invoke(this)
    }
}

@Composable
private fun ResultAppsLinksGrid(
    outputsForLinks: Map<String?, List<Output>>,
    appDetails: AppDetails,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
    onDisableLinkGroup: (group: String?) -> Unit,
) {
    val spacing = LocalSpacing.current

    Grid(Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.smallAdaptive)) {
        outputsForLinks
            .forEach { (group, outputs) ->
                item {
                    val uuid = (outputs.firstOrNull() as? ShareLinkUriOutput)?.link?.uuid
                    AppIcon(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("geoShareApp_$uuid"),
                        label = group,
                        appDetails = appDetails,
                        outputs = outputs,
                        onClick = onClick,
                        onHide = { onDisableLinkGroup(group) },
                    ) {
                        outputs.firstOrNull()?.getIcon(appDetails)?.let {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                                IconFromDescriptor(
                                    it,
                                    contentDescription = null,
                                    size = iconSize,
                                    inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
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
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultApps(
                    appDetails = mapOf(
                        PackageNames.COMAPS_FDROID to AppDetail(
                            packageName = PackageNames.COMAPS_FDROID,
                            label = "CoMaps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.CONVERSATIONS to AppDetail(
                            packageName = PackageNames.CONVERSATIONS,
                            label = "Conversations",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.ORGANIC_MAPS to AppDetail(
                            packageName = PackageNames.ORGANIC_MAPS,
                            label = "Organic Maps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.HERE_WEGO to AppDetail(
                            packageName = PackageNames.HERE_WEGO,
                            label = "HERE WeGo",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.MAPY_COM to AppDetail(
                            packageName = PackageNames.MAPY_COM,
                            label = "Mapy.com",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.MAGIC_EARTH to AppDetail(
                            packageName = PackageNames.MAGIC_EARTH,
                            label = "Magic Earth",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.GOOGLE_MAPS to AppDetail(
                            packageName = PackageNames.GOOGLE_MAPS,
                            label = "Google Maps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.GMAPS_WV to AppDetail(
                            packageName = PackageNames.GMAPS_WV,
                            label = @Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "GMaps WV",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.TOMTOM to AppDetail(
                            packageName = PackageNames.TOMTOM,
                            label = "TomTom",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.COMAPS_FDROID to App(
                                packageName = PackageNames.COMAPS_FDROID,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.CONVERSATIONS to App(
                                packageName = PackageNames.CONVERSATIONS,
                                dataTypes = setOf(DataType.SEND_PLAIN_TEXT)
                            ),
                            PackageNames.GMAPS_WV to App(
                                packageName = PackageNames.GMAPS_WV,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                            PackageNames.GOOGLE_MAPS to App(
                                packageName = PackageNames.GOOGLE_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.HERE_WEGO to App(
                                packageName = PackageNames.HERE_WEGO,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.MAGIC_EARTH to App(
                                packageName = PackageNames.MAGIC_EARTH,
                                dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                            ),
                            PackageNames.MAPY_COM to App(
                                packageName = PackageNames.MAPY_COM,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.ORGANIC_MAPS to App(
                                packageName = PackageNames.ORGANIC_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GPX_DATA)
                            ),
                            PackageNames.TOMTOM to App(
                                packageName = PackageNames.TOMTOM,
                                dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                ) {}
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultApps(
                    appDetails = mapOf(
                        PackageNames.COMAPS_FDROID to AppDetail(
                            packageName = PackageNames.COMAPS_FDROID,
                            label = "CoMaps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.CONVERSATIONS to AppDetail(
                            packageName = PackageNames.CONVERSATIONS,
                            label = "Conversations",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.ORGANIC_MAPS to AppDetail(
                            packageName = PackageNames.ORGANIC_MAPS,
                            label = "Organic Maps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.HERE_WEGO to AppDetail(
                            packageName = PackageNames.HERE_WEGO,
                            label = "HERE WeGo",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.MAPY_COM to AppDetail(
                            packageName = PackageNames.MAPY_COM,
                            label = "Mapy.com",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.MAGIC_EARTH to AppDetail(
                            packageName = PackageNames.MAGIC_EARTH,
                            label = "Magic Earth",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.GOOGLE_MAPS to AppDetail(
                            packageName = PackageNames.GOOGLE_MAPS,
                            label = "Google Maps",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.GMAPS_WV to AppDetail(
                            packageName = PackageNames.GMAPS_WV,
                            label = @Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "GMaps WV",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        PackageNames.TOMTOM to AppDetail(
                            packageName = PackageNames.TOMTOM,
                            label = "TomTom",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.COMAPS_FDROID to App(
                                packageName = PackageNames.COMAPS_FDROID,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.CONVERSATIONS to App(
                                packageName = PackageNames.CONVERSATIONS,
                                dataTypes = setOf(DataType.SEND_PLAIN_TEXT)
                            ),
                            PackageNames.GMAPS_WV to App(
                                packageName = PackageNames.GMAPS_WV,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                            PackageNames.GOOGLE_MAPS to App(
                                packageName = PackageNames.GOOGLE_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.HERE_WEGO to App(
                                packageName = PackageNames.HERE_WEGO,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.MAGIC_EARTH to App(
                                packageName = PackageNames.MAGIC_EARTH,
                                dataTypes = setOf(DataType.MAGIC_EARTH_URI)
                            ),
                            PackageNames.MAPY_COM to App(
                                packageName = PackageNames.MAPY_COM,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.ORGANIC_MAPS to App(
                                packageName = PackageNames.ORGANIC_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GPX_DATA)
                            ),
                            PackageNames.TOMTOM to App(
                                packageName = PackageNames.TOMTOM,
                                dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                ) {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                ResultApps(
                    appDetails = emptyMap(),
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.COMAPS_FDROID to App(
                                packageName = PackageNames.COMAPS_FDROID,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.ORGANIC_MAPS to App(
                                packageName = PackageNames.ORGANIC_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                    onNavigateToLinkScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                ResultApps(
                    appDetails = emptyMap(),
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.COMAPS_FDROID to App(
                                packageName = PackageNames.COMAPS_FDROID,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                            PackageNames.ORGANIC_MAPS to App(
                                packageName = PackageNames.ORGANIC_MAPS,
                                dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    outputsForLinks = outputRepository.getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                    onNavigateToLinkScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                ResultApps(
                    appDetails = emptyMap(),
                    outputsForApps = emptyMap(),
                    outputsForLinks = emptyMap(),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                    onNavigateToLinkScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                ResultApps(
                    appDetails = emptyMap(),
                    outputsForApps = outputRepository.getOutputsForApps(emptyMap(), emptySet()),
                    outputsForLinks = emptyMap(),
                    outputsForSharing = outputRepository.getOutputsForSharing(),
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    onDisableLinkGroup = {},
                    onExecute = {},
                    onHideApp = {},
                    onNavigateToLinkScreen = {},
                )
            }
        }
    }
}
