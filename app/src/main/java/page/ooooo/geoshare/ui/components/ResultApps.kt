package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppOutputs
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.ShareOutputs
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.PackageNames
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
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.outputs.PlaceholderIconDescriptor
import page.ooooo.geoshare.data.outputs.toAction
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.ResultAppsState
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ResultApps(
    appDetails: StateFlow<AppDetails>,
    state: StateFlow<ResultAppsState>,
    points: Points,
    source: StateFlow<String>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 46.dp,
    onExecute: (Action<*>) -> Unit,
    onHideApp: (packageName: String) -> Unit,
) {
    val lastPoint = points.lastOrNull() ?: return

    val appDetails by appDetails.collectAsStateWithLifecycle()
    val state by state.collectAsStateWithLifecycle()
    val source by source.collectAsStateWithLifecycle()

    Column(modifier) {
        // Map apps
        ResultGrid { itemWidth ->
            state.appsOutputs.mapAppsOutputs
                .sortedBy { appDetails[it.packageName]?.label }
                .forEach { appOutputs ->
                    key(appOutputs.packageName) {
                        ResultAppIcon(
                            appDetails = appDetails,
                            appOutputs = appOutputs,
                            lastPoint = lastPoint,
                            points = points,
                            iconSize = iconSize,
                            source = source,
                            width = itemWidth,
                            onExecute = onExecute,
                            onHideApp = { onHideApp(appOutputs.packageName) },
                        )
                    }
                }
            // Share item
            state.shareOutputs?.let { shareOutputs ->
                key("share") {
                    ResultShareIcon(
                        appDetails = appDetails,
                        shareOutputs = shareOutputs,
                        lastPoint = lastPoint,
                        points = points,
                        iconSize = iconSize,
                        source = source,
                        width = itemWidth,
                        onExecute = onExecute,
                    )
                }
            }
        }

        // Messaging apps
        if (state.appsOutputs.messagingAppsOutputs.isNotEmpty()) {
            ResultHeadline(stringResource(R.string.output_send))
            ResultGrid { itemWidth ->
                state.appsOutputs.messagingAppsOutputs
                    .sortedBy { appDetails[it.packageName]?.label }
                    .forEach { appOutputs ->
                        key(appOutputs.packageName) {
                            ResultAppIcon(
                                appDetails = appDetails,
                                appOutputs = appOutputs,
                                iconSize = iconSize,
                                lastPoint = lastPoint,
                                points = points,
                                source = source,
                                width = itemWidth,
                                onExecute = onExecute,
                                onHideApp = { onHideApp(appOutputs.packageName) },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun ResultAppIcon(
    appDetails: AppDetails,
    appOutputs: AppOutputs<Output<*>>,
    iconSize: Dp,
    lastPoint: Point,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (Action<*>) -> Unit,
    onHideApp: () -> Unit,
) {
    AppIcon(
        label = appDetails[appOutputs.packageName]?.label.orEmpty(),
        menu = { expanded, onDismissRequest ->
            AppMenu(
                appDetails = appDetails,
                expanded = expanded,
                lastPoint = lastPoint,
                outputs = appOutputs.allOutputs,
                points = points,
                source = source,
                onDismissRequest = onDismissRequest,
                onExecute = onExecute,
                onHide = onHideApp,
            )
        },
        onClick = { onExecute(appOutputs.defaultOutput.toAction(lastPoint, points, source)) },
        modifier = Modifier
            .width(width)
            .testTag("geoShareApp_${appOutputs.packageName}"),
    ) {
        IconFromDescriptor(
            appOutputs.defaultOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
            contentDescription = null,
            size = iconSize,
            placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun ResultShareIcon(
    appDetails: AppDetails,
    shareOutputs: ShareOutputs,
    iconSize: Dp,
    lastPoint: Point,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (action: Action<*>) -> Unit,
) {
    AppIcon(
        label = null,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                appDetails = appDetails,
                expanded = expanded,
                lastPoint = lastPoint,
                outputs = shareOutputs.allOutputs,
                points = points,
                source = source,
                onDismissRequest = onDismissRequest,
                onExecute = onExecute,
                onHide = null,
            )
        },
        onClick = { onExecute(shareOutputs.defaultOutput.toAction(lastPoint, points, source)) },
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
                shareOutputs.defaultOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
                contentDescription = shareOutputs.defaultOutput.label(appDetails),
                size = 24.dp,
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
            ResultApps(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                state = MutableStateFlow(
                    ResultAppsState(
                        appsOutputs = outputRepository.getAppsOutputs(
                            fakeActivities,
                            emptySet()
                        )
                    )
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onExecute = {},
                onHideApp = {},
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
            ResultApps(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                state = MutableStateFlow(
                    ResultAppsState(
                        appsOutputs = outputRepository.getAppsOutputs(
                            fakeActivities,
                            emptySet()
                        )
                    )
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onExecute = {},
                onHideApp = {},
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
            ResultApps(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                state = MutableStateFlow(
                    ResultAppsState(
                        appsOutputs = outputRepository.getAppsOutputs(
                            fakeActivities,
                            emptySet()
                        )
                    )
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onExecute = {},
                onHideApp = {},
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
            ResultApps(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                state = MutableStateFlow(
                    ResultAppsState(
                        appsOutputs = outputRepository.getAppsOutputs(
                            fakeActivities,
                            emptySet()
                        )
                    )
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onExecute = {},
                onHideApp = {},
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
            ResultApps(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                state = MutableStateFlow(
                    ResultAppsState(
                        appsOutputs = outputRepository.getAppsOutputs(
                            activities,
                            emptySet()
                        )
                    )
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onExecute = {},
                onHideApp = {},
            )
        }
    }
}
