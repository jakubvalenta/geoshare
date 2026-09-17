package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.key
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
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.isMessagingApp
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.toAction
import page.ooooo.geoshare.ui.OutputDetailsForApp
import page.ooooo.geoshare.ui.OutputDetailsForAppsByCategory
import page.ooooo.geoshare.ui.OutputDetailsForLink
import page.ooooo.geoshare.ui.OutputDetailsForSharing
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.toOutputDetailsForAppsByCategory
import page.ooooo.geoshare.ui.toOutputDetailsForLinks
import page.ooooo.geoshare.ui.toOutputDetailsForSharing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultApps(
    outputsForAppsByCategory: StateFlow<OutputDetailsForAppsByCategory>,
    outputsForLinks: StateFlow<List<OutputDetailsForLink>>,
    outputsForSharing: StateFlow<OutputDetailsForSharing?>,
    points: Points,
    source: StateFlow<String>,
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

    val outputsForAppsByCategory by outputsForAppsByCategory.collectAsStateWithLifecycle()
    val outputsForLinks by outputsForLinks.collectAsStateWithLifecycle()
    val outputsForSharing by outputsForSharing.collectAsStateWithLifecycle()
    val source by source.collectAsStateWithLifecycle()

    Column(modifier) {
        // Map apps
        // TODO Distribute space between flow row items
        FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
            outputsForAppsByCategory.mapApps
                // TODO Sort by label
                .forEach { outputsForApp ->
                    key(outputsForApp.packageName) {
                        ResultAppsAppIcon(
                            outputsForApp = outputsForApp,
                            iconSize = iconSize,
                            onClick = { output -> onExecute(output.toAction(lastPoint, points, source)) },
                            onHideApp = { onHideApp(outputsForApp.packageName) },
                        )
                    }
                }
            // Share item
            outputsForSharing?.let { outputsForSharing ->
                key(outputsForSharing.defaultOutputDetail.output.id) {
                    ResultAppsShareIcon(
                        outputsForSharing = outputsForSharing,
                        iconSize = iconSize,
                        onClick = { output -> onExecute(output.toAction(lastPoint, points, source)) },
                    )
                }
            }
        }

        // Messaging apps
        if (outputsForAppsByCategory.messagingApps.isNotEmpty()) {
            ResultAppsHeadline(stringResource(R.string.output_send))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
                outputsForAppsByCategory.messagingApps
                    // TODO Sort by label
                    .forEach { outputsForApp ->
                        key(outputsForApp.packageName) {
                            ResultAppsAppIcon(
                                outputsForApp = outputsForApp,
                                iconSize = iconSize,
                                onClick = { output -> onExecute(output.toAction(lastPoint, points, source)) },
                                onHideApp = { onHideApp(outputsForApp.packageName) },
                            )
                        }
                    }
            }
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
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
                outputsForLinks.forEach { outputsForLink ->
                    key(outputsForLink.group) {
                        ResultAppsLinkIcon(
                            outputsForLink = outputsForLink,
                            iconSize = iconSize,
                            onClick = { output -> onExecute(output.toAction(lastPoint, points, source)) },
                            onDisableLinkGroup = { onDisableLinkGroup(outputsForLink.group) },
                        )
                    }
                }
            }
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
private fun ResultAppsAppIcon(
    outputsForApp: OutputDetailsForApp,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
    onHideApp: () -> Unit,
) {
    AppIcon(
        label = outputsForApp.label,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                outputDetails = outputsForApp.all,
                onClick = onClick,
                onDismissRequest = onDismissRequest,
                onHide = onHideApp,
            )
        },
        onClick = { onClick(outputsForApp.default.output) },
        modifier = Modifier.testTag("geoShareApp_${outputsForApp.packageName}"),
    ) {
        IconFromDescriptor(
            outputsForApp.default.icon ?: PlaceholderIconDescriptor,
            contentDescription = null,
            size = iconSize,
            placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun ResultAppsShareIcon(
    outputsForSharing: OutputDetailsForSharing,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
) {
    AppIcon(
        label = null,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                outputDetails = outputsForSharing.outputDetails,
                onClick = onClick,
                onDismissRequest = onDismissRequest,
                onHide = null,
            )
        },
        onClick = { onClick(outputsForSharing.defaultOutputDetail.output) },
        modifier = Modifier.testTag("geoShareAppShare"),
    ) {
        Surface(
            Modifier.requiredSize(iconSize),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape,
        ) {
            IconFromDescriptor(
                outputsForSharing.defaultOutputDetail.icon ?: PlaceholderIconDescriptor,
                contentDescription = outputsForSharing.defaultOutputDetail.label(),
                size = 24.dp,
            )
        }
    }
}

@Composable
private fun ResultAppsLinkIcon(
    outputsForLink: OutputDetailsForLink,
    iconSize: Dp,
    onClick: (output: Output) -> Unit,
    onDisableLinkGroup: () -> Unit,
) {
    AppIcon(
        label = outputsForLink.group,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                outputDetails = outputsForLink.all,
                onClick = onClick,
                onDismissRequest = onDismissRequest,
                onHide = onDisableLinkGroup,
            )
        },
        onClick = { onClick(outputsForLink.default.output) },
        modifier = Modifier.testTag("geoShareLink_${outputsForLink.group}"),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
            IconFromDescriptor(
                outputsForLink.default.icon ?: PlaceholderIconDescriptor,
                contentDescription = null,
                size = iconSize,
                inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
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
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails = getFakeAppDetails(context)
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    fakeActivities
                        .partition { !it.isMessagingApp() }
                        .let { (mapAppActivities, messagingAppActivities) ->
                            Pair(
                                outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                                outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                            )
                        }
                        .toOutputDetailsForAppsByCategory(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
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
            val appDetails = getFakeAppDetails(context)
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    fakeActivities
                        .partition { !it.isMessagingApp() }
                        .let { (mapAppActivities, messagingAppActivities) ->
                            Pair(
                                outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                                outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                            )
                        }
                        .toOutputDetailsForAppsByCategory(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
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
            val appDetails: AppDetails = emptyMap()
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    fakeActivities
                        .filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.ORGANIC_MAPS,
                            )
                        }
                        .partition { !it.isMessagingApp() }
                        .let { (mapAppActivities, messagingAppActivities) ->
                            Pair(
                                outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                                outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                            )
                        }
                        .toOutputDetailsForAppsByCategory(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
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
            val appDetails: AppDetails = emptyMap()
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    fakeActivities
                        .filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.ORGANIC_MAPS,
                            )
                        }
                        .partition { !it.isMessagingApp() }
                        .let { (mapAppActivities, messagingAppActivities) ->
                            Pair(
                                outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                                outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                            )
                        }
                        .toOutputDetailsForAppsByCategory(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.getOutputsForSharing().toOutputDetailsForSharing(appDetails)
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
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
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
                ),
                outputsForLinks = MutableStateFlow(emptyList()),
                outputsForSharing = MutableStateFlow(null),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
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
            ResultApps(
                outputsForAppsByCategory = MutableStateFlow(
                    OutputDetailsForAppsByCategory(mapApps = emptyList(), messagingApps = emptyList())
                ),
                outputsForLinks = MutableStateFlow(emptyList()),
                outputsForSharing = MutableStateFlow(null),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            )
        }
    }
}
