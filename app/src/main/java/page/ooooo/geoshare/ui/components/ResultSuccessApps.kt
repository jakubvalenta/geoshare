package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.COMAPS_FDROID_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.GMAPS_WV_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.HERE_WEGO_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAGIC_EARTH_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAPY_COM_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.ORGANIC_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.OSMAND_PLUS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.getOutputsForApps
import page.ooooo.geoshare.lib.outputs.getOutputsForLinks
import page.ooooo.geoshare.lib.outputs.getOutputsForSharing
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Points
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSuccessApps(
    appDetails: AppDetails,
    outputsForApps: Map<String, List<Output>>,
    outputsForLinks: Map<String?, List<Output>>,
    outputsForSharing: List<Output>,
    points: Points,
    iconSize: Dp = 46.dp,
    onExecute: (Action<*>) -> Unit,
    onNavigateToLinksScreen: () -> Unit,
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
    val spacing = LocalSpacing.current

    Grid(Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.smallAdaptive)) {
        // Apps
        outputsForApps
            .map { (packageName, outputs) -> appDetails[packageName]?.label to outputs }
            .sortedWith(compareBy(nullsLast()) { (label) -> label })
            .forEach { (label, outputs) ->
                item {
                    AppIcon(Modifier.weight(1f), label, appDetails, outputs, onClick) {
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
        // Share item
        item {
            AppIcon(Modifier.weight(1f), null, appDetails, outputsForSharing, onClick) {
                Surface(
                    Modifier.requiredSize(iconSize),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    outputsForSharing.firstOrNull()?.let { firstOutput ->
                        firstOutput.getIcon(appDetails)?.let { icon ->
                            IconFromDescriptor(
                                icon,
                                contentDescription = firstOutput.label(appDetails),
                                size = 24.dp
                            )
                        }
                    }
                }
            }
        }
    }

    if (outputsForLinks.isNotEmpty()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.windowPadding,
                    top = spacing.tinyAdaptive,
                    end = spacing.windowPadding - 8.dp, // Align with last point menu
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.links_title),
                Modifier.semantics { heading() },
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(
                { onNavigateToLinksScreen() },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.conversion_succeeded_apps_links_configure),
                    Modifier.requiredSize(24.dp),
                )
            }
        }
        Grid(
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.smallAdaptive)
        ) {
            // Links
            outputsForLinks
                .forEach { (group, outputs) ->
                    item {
                        AppIcon(Modifier.weight(1f), group, appDetails, outputs, onClick) {
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
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    appDetails = mapOf(
                        COMAPS_FDROID_PACKAGE_NAME to AppDetail(
                            "CoMaps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        ORGANIC_MAPS_PACKAGE_NAME to AppDetail(
                            "Organic Maps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        HERE_WEGO_PACKAGE_NAME to AppDetail(
                            "HERE WeGo",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        MAPY_COM_PACKAGE_NAME to AppDetail(
                            "Mapy.com",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        MAGIC_EARTH_PACKAGE_NAME to AppDetail(
                            "Magic Earth",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        GOOGLE_MAPS_PACKAGE_NAME to AppDetail(
                            "Google Maps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        GMAPS_WV_PACKAGE_NAME to AppDetail(
                            "GMaps WV",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        TOMTOM_PACKAGE_NAME to AppDetail(
                            "TomTom",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    outputsForApps = getOutputsForApps(
                        mapOf(
                            COMAPS_FDROID_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            GMAPS_WV_PACKAGE_NAME to setOf(DataType.GEO_URI),
                            GOOGLE_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            HERE_WEGO_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                            MAPY_COM_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            ORGANIC_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GPX_DATA),
                            TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                        ),
                        emptySet(),
                    ),
                    outputsForLinks = getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    appDetails = mapOf(
                        COMAPS_FDROID_PACKAGE_NAME to AppDetail(
                            "CoMaps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        ORGANIC_MAPS_PACKAGE_NAME to AppDetail(
                            "Organic Maps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        HERE_WEGO_PACKAGE_NAME to AppDetail(
                            "HERE WeGo",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        MAPY_COM_PACKAGE_NAME to AppDetail(
                            "Mapy.com",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        MAGIC_EARTH_PACKAGE_NAME to AppDetail(
                            "Magic Earth",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        GOOGLE_MAPS_PACKAGE_NAME to AppDetail(
                            "Google Maps",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        GMAPS_WV_PACKAGE_NAME to AppDetail(
                            "GMaps WV",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                        TOMTOM_PACKAGE_NAME to AppDetail(
                            "TomTom",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    outputsForApps = getOutputsForApps(
                        mapOf(
                            COMAPS_FDROID_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            GMAPS_WV_PACKAGE_NAME to setOf(DataType.GEO_URI),
                            GOOGLE_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            HERE_WEGO_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                            MAPY_COM_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            ORGANIC_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            OSMAND_PLUS_PACKAGE_NAME to setOf(DataType.GPX_DATA),
                            TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                        ),
                        emptySet(),
                    ),
                    outputsForLinks = getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    appDetails = emptyMap(),
                    outputsForApps = getOutputsForApps(
                        mapOf(
                            COMAPS_FDROID_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            ORGANIC_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                        ),
                        emptySet(),
                    ),
                    outputsForLinks = getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
                ) {}
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    appDetails = emptyMap(),
                    outputsForApps = getOutputsForApps(
                        mapOf(
                            COMAPS_FDROID_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                            ORGANIC_MAPS_PACKAGE_NAME to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                        ),
                        emptySet(),
                    ),
                    outputsForLinks = getOutputsForLinks(defaultFakeLinks),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
                ) {}
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
                ResultSuccessApps(
                    appDetails = emptyMap(),
                    outputsForApps = emptyMap(),
                    outputsForLinks = emptyMap(),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
                ) {}
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
                ResultSuccessApps(
                    appDetails = emptyMap(),
                    outputsForApps = getOutputsForApps(emptyMap(), emptySet()),
                    outputsForLinks = emptyMap(),
                    outputsForSharing = getOutputsForSharing(),
                    points = persistentListOf(Point.example),
                    onExecute = {},
                ) {}
            }
        }
    }
}
