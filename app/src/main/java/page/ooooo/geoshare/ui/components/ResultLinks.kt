package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import page.ooooo.geoshare.data.LinkOutputs
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.di.defaultFakeLinks
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
import page.ooooo.geoshare.ui.ResultLinksState
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ResultLinks(
    state: StateFlow<ResultLinksState>,
    points: Points,
    source: StateFlow<String>,
    iconSize: Dp = 46.dp,
    onExecute: (Action<*>) -> Unit,
    onDisableLinkGroup: (group: String?) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
) {
    val lastPoint = points.lastOrNull() ?: return

    val state by state.collectAsStateWithLifecycle()
    val source by source.collectAsStateWithLifecycle()

    // Links
    if (state.linksOutputs.isNotEmpty()) {
        Column {
            ResultHeadline(stringResource(R.string.links_title)) {
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
            ResultGrid { itemWidth ->
                state.linksOutputs.forEach { linkOutputs ->
                    key(linkOutputs.group) {
                        ResultLinkIcon(
                            iconSize = iconSize,
                            lastPoint = lastPoint,
                            linkOutputs = linkOutputs,
                            points = points,
                            source = source,
                            width = itemWidth,
                            onExecute = onExecute,
                            onDisableLinkGroup = { onDisableLinkGroup(linkOutputs.group) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultLinkIcon(
    iconSize: Dp,
    lastPoint: Point,
    linkOutputs: LinkOutputs,
    points: Points,
    source: String,
    width: Dp,
    onExecute: (action: Action<*>) -> Unit,
    onDisableLinkGroup: () -> Unit,
) {
    val appDetails: AppDetails = emptyMap() // Link outputs never require app details

    AppIcon(
        label = linkOutputs.group,
        menu = { expanded, onDismissRequest ->
            AppMenu(
                appDetails = appDetails,
                expanded = expanded,
                lastPoint = lastPoint,
                outputs = linkOutputs.allOutputs,
                points = points,
                source = source,
                onExecute = onExecute,
                onDismissRequest = onDismissRequest,
                onHide = onDisableLinkGroup,
            )
        },
        onClick = { onExecute(linkOutputs.defaultOutput.toAction(lastPoint, points, source)) },
        modifier = Modifier
            .width(width)
            .testTag("geoShareLink_${linkOutputs.group}"),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
            IconFromDescriptor(
                linkOutputs.defaultOutput.getIcon(appDetails) ?: PlaceholderIconDescriptor,
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
            ResultLinks(
                state = MutableStateFlow(
                    ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
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
            ResultLinks(
                state = MutableStateFlow(
                    ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
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
            ResultLinks(
                state = MutableStateFlow(
                    ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
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
            ResultLinks(
                state = MutableStateFlow(
                    ResultLinksState(linksOutputs = outputRepository.getLinksOutputs(defaultFakeLinks))
                ),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
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
            ResultLinks(
                state = MutableStateFlow(ResultLinksState()),
                points = persistentListOf(WGS84Point(NaivePoint.example)),
                source = MutableStateFlow(""),
                onDisableLinkGroup = {},
                onExecute = {},
                onNavigateToLinkScreen = {},
            )
        }
    }
}
