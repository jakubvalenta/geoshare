package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
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
    appDetails: StateFlow<AppDetails>,
    outputsForApps: StateFlow<Map<String, List<Output>>>,
    outputsForLinks: StateFlow<Map<String, List<Output>>>,
    outputsForSharing: StateFlow<List<Output>>,
    points: Points,
    modifier: Modifier = Modifier,
    iconSize: Dp = 46.dp,
    onDisableLinkGroup: (group: String?) -> Unit,
    onExecute: (Action<*>) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    message: (@Composable (paddingValues: PaddingValues) -> Unit)? = null,
) {
    val lastPoint = points.lastOrNull() ?: return
    val spacing = LocalSpacing.current

    val appDetails by appDetails.collectAsStateWithLifecycle()
    val outputsForApps by outputsForApps.collectAsStateWithLifecycle()
    val outputsForLinks by outputsForLinks.collectAsStateWithLifecycle()
    val outputsForSharing by outputsForSharing.collectAsStateWithLifecycle()
    val (outputsForMapApps, outputsForMessagingApps) = remember(outputsForApps, outputsForLinks) {
        outputsForApps.entries.partition { (_, outputs) ->
            outputs.size != 1 || outputs[0] !is SendPointOutput
        }
    }
    val onClick = fun(output: Output) {
        onExecute(
            when (output) {
                is PointOutput -> output.toAction(lastPoint)
                is PointsOutput -> output.toAction(points)
            }
        )
    }

    Column(modifier) {
        // Map apps
        ResultAppsGrid(
            outputsForApps = outputsForMapApps,
            appDetails = appDetails,
            iconSize = iconSize,
            onClick = onClick,
            onHideApp = onHideApp,
        ) {
            outputsForSharing.firstOrNull()?.let { firstOutput ->
                // Share item
                item(firstOutput.id) {
                    AppIcon(
                        label = null,
                        menu = { expanded, onDismissRequest ->
                            AppMenu(
                                appDetails = appDetails,
                                expanded = expanded,
                                outputs = outputsForSharing,
                                onClick = onClick,
                                onDismissRequest = onDismissRequest,
                                onHide = null,
                            )
                        },
                        onClick = { onClick(firstOutput) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("geoShareApp_share"),
                    ) {
                        Surface(
                            Modifier.requiredSize(iconSize),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = CircleShape,
                        ) {
                            IconFromDescriptor(
                                firstOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
                                contentDescription = firstOutput.label(appDetails),
                                size = 24.dp,
                            )
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
                FilledIconButton(
                    { onNavigateToLinkScreen() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                ) {
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
                onClick = { onClick(it) },
                onDisableLinkGroup = onDisableLinkGroup,
            )
        }

        // Message
        message?.invoke(PaddingValues(top = spacing.small))
    }
}

@Composable
private fun ResultAppsHeadline(text: String, extra: (@Composable RowScope.() -> Unit)? = null) {
    val spacing = LocalSpacing.current

    Row(
        Modifier
            .padding(top = spacing.tiny)
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
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
    extra: (LazyGridScope.() -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false,
    ) {
        outputsForApps
            .map { (packageName, outputs) -> Triple(packageName, appDetails[packageName]?.label, outputs) }
            .sortedWith(compareBy(nullsLast()) { (_, label) -> label })
            .forEach { (packageName, label, outputs) ->
                outputs.firstOrNull()?.let { firstOutput ->
                    item(firstOutput.id) {
                        AppIcon(
                            label = label.orEmpty(),
                            menu = { expanded, onDismissRequest ->
                                AppMenu(
                                    appDetails = appDetails,
                                    expanded = expanded,
                                    outputs = outputs,
                                    onClick = onClick,
                                    onDismissRequest = onDismissRequest,
                                    onHide = { onHideApp(packageName) },
                                )
                            },
                            onClick = { onClick(firstOutput) },
                            modifier = Modifier.testTag("geoShareApp_$packageName"),
                        ) {
                            IconFromDescriptor(
                                firstOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
                                contentDescription = null,
                                size = iconSize,
                                placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            )
                        }
                    }
                }
            }
        extra?.invoke(this)
    }
}

@Composable
private fun ResultAppsLinksGrid(
    outputsForLinks: Map<String, List<Output>>,
    appDetails: AppDetails,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
    onDisableLinkGroup: (group: String?) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false,
    ) {
        outputsForLinks
            .forEach { (group, outputs) ->
                outputs.firstOrNull()?.let { firstOutput ->
                    item(firstOutput.id) {
                        AppIcon(
                            label = group,
                            menu = { expanded, onDismissRequest ->
                                AppMenu(
                                    appDetails = appDetails,
                                    expanded = expanded,
                                    outputs = outputs,
                                    onClick = onClick,
                                    onDismissRequest = onDismissRequest,
                                    onHide = { onDisableLinkGroup(group) },
                                )
                            },
                            onClick = { onClick(firstOutput) },
                            modifier = Modifier.testTag("geoShareApp_${(firstOutput as? ShareLinkUriOutput)?.link?.uuid}"),
                        ) {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                                IconFromDescriptor(
                                    firstOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
                                    contentDescription = null,
                                    size = iconSize,
                                    inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                )
                            }
                        }
                    }
                }
            }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun fakeAppDetails(
    context: Context = LocalContext.current,
) = mapOf(
    PackageNames.COMAPS_FDROID to AppDetail(
        packageName = PackageNames.COMAPS_FDROID,
        label = "CoMaps",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.CONVERSATIONS to AppDetail(
        packageName = PackageNames.CONVERSATIONS,
        label = "Conversations",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.ORGANIC_MAPS to AppDetail(
        packageName = PackageNames.ORGANIC_MAPS,
        label = "Organic Maps",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.HERE_WEGO to AppDetail(
        packageName = PackageNames.HERE_WEGO,
        label = "HERE WeGo",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.MAPY_COM to AppDetail(
        packageName = PackageNames.MAPY_COM,
        label = "Mapy.com",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.OSMAND_PLUS to AppDetail(
        packageName = PackageNames.OSMAND_PLUS,
        label = "OsmAnd",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.MAGIC_EARTH to AppDetail(
        packageName = PackageNames.MAGIC_EARTH,
        label = "Magic Earth",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.GOOGLE_MAPS to AppDetail(
        packageName = PackageNames.GOOGLE_MAPS,
        label = "Google Maps",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.GMAPS_WV to AppDetail(
        packageName = PackageNames.GMAPS_WV,
        label = @Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "GMaps WV",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
    PackageNames.TOMTOM to AppDetail(
        packageName = PackageNames.TOMTOM,
        label = "TomTom",
        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
    ),
)

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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(fakeAppDetails()),
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(
                        activities = fakeActivities,
                        hiddenApps = emptySet(),
                    )
                ),
                outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(fakeAppDetails()),
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(
                        activities = fakeActivities,
                        hiddenApps = emptySet(),
                    )
                ),
                outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(emptyMap()),
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(
                        activities = fakeActivities.filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.ORGANIC_MAPS,
                            )
                        },
                        hiddenApps = emptySet(),
                    )
                ),
                outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            ) { paddingValues ->
                HelpMessageCard(
                    helpMessage = HelpMessage.WELCOME,
                    dismissedHelpMessages = MutableStateFlow(emptySet()),
                    title = { Text(stringResource(R.string.help_welcome_title)) },
                    modifier = Modifier.padding(paddingValues),
                    onDismiss = {},
                ) {
                    ParagraphText(
                        stringResource(
                            R.string.help_welcome_text,
                            stringResource(R.string.main_create_geo_uri),
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(emptyMap()),
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(
                        activities = fakeActivities.filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.ORGANIC_MAPS,
                            )
                        },
                        hiddenApps = emptySet(),
                    )
                ),
                outputsForLinks = MutableStateFlow(outputRepository.getOutputsForLinks(defaultFakeLinks)),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            ) { paddingValues ->
                HelpMessageCard(
                    helpMessage = HelpMessage.WELCOME,
                    dismissedHelpMessages = MutableStateFlow(emptySet()),
                    title = { Text(stringResource(R.string.help_welcome_title)) },
                    modifier = Modifier.padding(paddingValues),
                    onDismiss = {},
                ) {
                    ParagraphText(
                        stringResource(
                            R.string.help_welcome_text,
                            stringResource(R.string.main_create_geo_uri),
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(emptyMap()),
                outputsForApps = MutableStateFlow(emptyMap()),
                outputsForLinks = MutableStateFlow(emptyMap()),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            ResultApps(
                appDetails = MutableStateFlow(emptyMap()),
                outputsForApps = MutableStateFlow(outputRepository.getOutputsForApps(emptyList(), emptySet())),
                outputsForLinks = MutableStateFlow(emptyMap()),
                outputsForSharing = MutableStateFlow(outputRepository.getOutputsForSharing()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            )
        }
    }
}
