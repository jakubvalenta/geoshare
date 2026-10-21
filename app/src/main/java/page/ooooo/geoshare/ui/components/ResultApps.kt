package page.ooooo.geoshare.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.PlaceholderIconDescriptor
import page.ooooo.geoshare.data.outputs.toAction
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.OutputDetailsForApp
import page.ooooo.geoshare.ui.OutputDetailsForApps
import page.ooooo.geoshare.ui.OutputDetailsForLink
import page.ooooo.geoshare.ui.OutputDetailsForSharing
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.toOutputDetailsForApps
import page.ooooo.geoshare.ui.toOutputDetailsForLinks
import page.ooooo.geoshare.ui.toOutputDetailsForSharing

@Composable
fun ResultApps(
    outputsForApps: StateFlow<OutputDetailsForApps>,
    outputsForLinks: StateFlow<List<OutputDetailsForLink>>,
    outputsForSharing: StateFlow<OutputDetailsForSharing?>,
    points: Points,
    source: StateFlow<String>,
    modifier: Modifier = Modifier,
    gridHorizontalPadding: Dp = LocalSpacing.current.windowPadding - 10.dp,
    horizontalSpacing: Dp = LocalSpacing.current.tiny,
    iconSize: Dp = 46.dp,
    minItemWidth: Dp = 78.dp,
    onDisableLinkGroup: (group: String?) -> Unit,
    onExecute: (Action<*>) -> Unit,
    onHideApp: (packageName: String) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    message: (@Composable (paddingValues: PaddingValues) -> Unit)? = null,
) {
    val lastPoint = points.lastOrNull() ?: return
    val spacing = LocalSpacing.current

    val outputsForApps by outputsForApps.collectAsStateWithLifecycle()
    val outputsForLinks by outputsForLinks.collectAsStateWithLifecycle()
    val outputsForSharing by outputsForSharing.collectAsStateWithLifecycle()
    val source by source.collectAsStateWithLifecycle()

    Column(modifier) {
        // Map apps
        EvenlySpacedGrid(
            minItemWidth,
            modifier = Modifier.padding(horizontal = gridHorizontalPadding),
            horizontalSpacing = horizontalSpacing,
        ) { itemWidth ->
            outputsForApps.maps
                .forEach { outputsForApp ->
                    key(outputsForApp.packageName) {
                        ResultAppsAppIcon(
                            lastPoint = lastPoint,
                            outputsForApp = outputsForApp,
                            points = points,
                            iconSize = iconSize,
                            source = source,
                            width = itemWidth,
                            onExecute = onExecute,
                            onHideApp = { onHideApp(outputsForApp.packageName) },
                        )
                    }
                }
            // Share item
            outputsForSharing?.let { outputsForSharing ->
                ResultAppsShareIcon(
                    lastPoint = lastPoint,
                    outputsForSharing = outputsForSharing,
                    points = points,
                    iconSize = iconSize,
                    source = source,
                    width = itemWidth,
                    onExecute = onExecute,
                )
            }
        }

        // Messaging apps
        if (outputsForApps.messaging.isNotEmpty()) {
            ResultAppsHeadline(stringResource(R.string.output_send))
            EvenlySpacedGrid(
                minItemWidth,
                modifier = Modifier.padding(horizontal = gridHorizontalPadding),
                horizontalSpacing = horizontalSpacing,
            ) { itemWidth ->
                outputsForApps.messaging
                    .forEach { outputsForApp ->
                        key(outputsForApp.packageName) {
                            ResultAppsAppIcon(
                                iconSize = iconSize,
                                lastPoint = lastPoint,
                                outputsForApp = outputsForApp,
                                points = points,
                                source = source,
                                width = itemWidth,
                                onExecute = onExecute,
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
            EvenlySpacedGrid(
                minItemWidth,
                modifier = Modifier.padding(horizontal = gridHorizontalPadding),
                horizontalSpacing = horizontalSpacing,
            ) { itemWidth ->
                outputsForLinks.forEach { outputsForLink ->
                    key(outputsForLink.group) {
                        ResultAppsLinkIcon(
                            iconSize = iconSize,
                            lastPoint = lastPoint,
                            outputsForLink = outputsForLink,
                            points = points,
                            source = source,
                            width = itemWidth,
                            onExecute = onExecute,
                            onDisableLinkGroup = { onDisableLinkGroup(outputsForLink.group) },
                        )
                    }
                }
            }
        }

        // Message
        message?.invoke(
            PaddingValues(
                start = spacing.windowPadding,
                top = spacing.small,
                end = spacing.windowPadding,
            )
        )
    }
}

@Composable
private fun ResultAppsHeadline(text: String, extra: (@Composable RowScope.() -> Unit)? = null) {
    val spacing = LocalSpacing.current

    Row(
        Modifier
            .padding(top = spacing.tiny)
            .padding(horizontal = spacing.windowPadding)
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
    iconSize: Dp,
    lastPoint: Point,
    outputsForApp: OutputDetailsForApp,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (Action<*>) -> Unit,
    onHideApp: () -> Unit,
) {
    AppIcon(
        label = outputsForApp.label,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                lastPoint = lastPoint,
                outputDetails = outputsForApp.all,
                points = points,
                source = source,
                onDismissRequest = onDismissRequest,
                onExecute = onExecute,
                onHide = onHideApp,
            )
        },
        onClick = { onExecute(outputsForApp.default.output.toAction(lastPoint, points, source)) },
        modifier = Modifier
            .width(width)
            .testTag("geoShareApp_${outputsForApp.packageName}"),
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
    iconSize: Dp,
    lastPoint: Point,
    outputsForSharing: OutputDetailsForSharing,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (action: Action<*>) -> Unit,
) {
    AppIcon(
        label = null,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                lastPoint = lastPoint,
                outputDetails = outputsForSharing.all,
                points = points,
                source = source,
                onDismissRequest = onDismissRequest,
                onExecute = onExecute,
                onHide = null,
            )
        },
        onClick = { onExecute(outputsForSharing.default.output.toAction(lastPoint, points, source)) },
        modifier = Modifier
            .width(width)
            .testTag("geoShareAppShare"),
    ) {
        Surface(
            Modifier.requiredSize(iconSize),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape,
        ) {
            IconFromDescriptor(
                outputsForSharing.default.icon ?: PlaceholderIconDescriptor,
                contentDescription = outputsForSharing.default.label(),
                size = 24.dp,
            )
        }
    }
}

@Composable
private fun ResultAppsLinkIcon(
    iconSize: Dp,
    lastPoint: Point,
    outputsForLink: OutputDetailsForLink,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (action: Action<*>) -> Unit,
    onDisableLinkGroup: () -> Unit,
) {
    AppIcon(
        label = outputsForLink.group,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                expanded = expanded,
                lastPoint = lastPoint,
                outputDetails = outputsForLink.all,
                points = points,
                source = source,
                onExecute = onExecute,
                onDismissRequest = onDismissRequest,
                onHide = onDisableLinkGroup,
            )
        },
        onClick = { onExecute(outputsForLink.default.output.toAction(lastPoint, points, source)) },
        modifier = Modifier
            .width(width)
            .testTag("geoShareLink_${outputsForLink.group}"),
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
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails = getFakeAppDetails(context)
            ResultApps(
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(fakeActivities, emptySet()).toOutputDetailsForApps(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails)
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
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val appDetails = getFakeAppDetails(context)
            ResultApps(
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(fakeActivities, emptySet()).toOutputDetailsForApps(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails)
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

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun SmallPreview() {
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
            ResultApps(
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(fakeActivities, emptySet()).toOutputDetailsForApps(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails)
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

@Preview(showBackground = true, device = "spec:width=640dp,height=800dp,dpi=240")
@Composable
private fun TabletPreview() {
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
            ResultApps(
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(fakeActivities, emptySet()).toOutputDetailsForApps(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails)
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
            val uriQuote = DefaultUriQuote
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
                uriQuote = uriQuote,
            )
            val activities = fakeActivities.filter {
                it.packageName in setOf(
                    PackageNames.COMAPS_FDROID,
                    PackageNames.ORGANIC_MAPS,
                )
            }
            val appDetails: AppDetails = emptyMap()
            ResultApps(
                outputsForApps = MutableStateFlow(
                    outputRepository.getOutputsForApps(activities, emptySet()).toOutputDetailsForApps(appDetails)
                ),
                outputsForLinks = MutableStateFlow(
                    outputRepository.getOutputsForLinks(defaultFakeLinks).toOutputDetailsForLinks(appDetails)
                ),
                outputsForSharing = MutableStateFlow(
                    outputRepository.outputsForSharing.toOutputDetailsForSharing(appDetails)
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
                onHideApp = {},
                onNavigateToLinkScreen = {},
            ) { paddingValues ->
                if (helpLifecycleMessage == null) {
                    HelpOpenByDefaultMessage(
                        dismissedHelpMessages = MutableStateFlow(emptySet()),
                        sourceComesFromIntent = MutableStateFlow(true),
                        modifier = Modifier.padding(paddingValues),
                        onDismissHelpMessage = {},
                        onNavigateToFaqScreen = {},
                    )
                }
            }
        }
    }
}
