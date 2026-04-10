package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeOpenStreetMapDisplayLink
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenPointOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.point.CoordinateConverter
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    label: String? = null,
    appDetails: AppDetails = emptyMap(),
    outputs: List<Output> = emptyList(),
    onClick: (Output) -> Unit = {},
    onHide: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    val (expanded, setExpanded) = retain { mutableStateOf(false) }

    Box(
        modifier
            .run {
                if (enabled) {
                    combinedClickable(
                        role = Role.Button,
                        onLongClick = { setExpanded(true) },
                        onClick = { outputs.firstOrNull()?.let { onClick(it) } },
                    )
                } else {
                    this
                }
            }
            .run {
                outputs.firstOrNull()?.let { firstOutput ->
                    ((firstOutput as? OpenPointOutput)?.packageName
                        ?: (firstOutput as? OpenRouteOnePointGpxOutput)?.packageName
                        ?: (firstOutput as? ShareLinkUriOutput)?.link?.uuid)?.let { id ->
                        testTag("geoShareApp_$id")
                    }
                } ?: this
            },
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive),
        ) {
            content()
            if (label != null) {
                Text(
                    label,
                    Modifier
                        .fillMaxWidth()
                        .testTag("geoShareAppLabel"),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        FilledIconButton(
            { setExpanded(true) },
            Modifier.size(30.dp),
            shape = MaterialShapes.ClamShell.toShape(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(
                painterResource(R.drawable.more_horiz_24px),
                contentDescription = stringResource(R.string.nav_menu_content_description),
                Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            shape = ShapeDefaults.Large,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            var prevIconDescriptor: IconDescriptor? = null
            outputs.forEach { output ->
                DropdownMenuItem(
                    text = {
                        Text(
                            output.label(appDetails),
                            Modifier.testTag("geoShareAppOutput"),
                        )
                    },
                    onClick = {
                        setExpanded(false)
                        onClick(output)
                    },
                    leadingIcon = output.getMenuIcon(appDetails)
                        ?.takeIf { it != prevIconDescriptor }
                        ?.also { prevIconDescriptor = it }
                        ?.let { { IconFromDescriptor(it, contentDescription = null) } }
                        ?: { Spacer(Modifier.size(24.dp)) },
                )
            }
            if (onHide != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.conversion_succeeded_hide),
                            Modifier.testTag("geoShareAppHide"),
                        )
                    },
                    onClick = {
                        setExpanded(false)
                        onHide()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Close, null)
                    }
                )
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
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val geoUriFormatter = GeoUriFormatter(coordinateConverter)
            AppIcon(
                modifier = Modifier.width(85.dp),
                label = FakeOpenStreetMapDisplayLink.group,
                outputs = listOf(ShareDisplayGeoUriOutput(geoUriFormatter), CopyGeoUriOutput(geoUriFormatter)),
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                    IconFromDescriptor(
                        FakeOpenStreetMapDisplayLink.icon,
                        contentDescription = null,
                        size = 46.dp,
                        inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val geoUriFormatter = GeoUriFormatter(coordinateConverter)
            AppIcon(
                modifier = Modifier.width(85.dp),
                label = FakeOpenStreetMapDisplayLink.group,
                outputs = listOf(ShareDisplayGeoUriOutput(geoUriFormatter), CopyGeoUriOutput(geoUriFormatter)),
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                    IconFromDescriptor(
                        FakeOpenStreetMapDisplayLink.icon,
                        contentDescription = null,
                        size = 46.dp,
                        inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareItemPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val geoUriFormatter = GeoUriFormatter(coordinateConverter)
            val output = ShareDisplayGeoUriOutput(geoUriFormatter)
            AppIcon(
                modifier = Modifier.width(85.dp),
            ) {
                Surface(
                    Modifier.requiredSize(46.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    IconFromDescriptor(
                        output.getIcon(emptyMap()),
                        contentDescription = output.label(emptyMap()),
                        size = 24.dp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkShareItemPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val chinaGeometry = ChinaGeometry(context)
            val coordinateConverter = CoordinateConverter(chinaGeometry)
            val geoUriFormatter = GeoUriFormatter(coordinateConverter)
            val output = ShareDisplayGeoUriOutput(geoUriFormatter)
            AppIcon(
                modifier = Modifier.width(85.dp),
            ) {
                Surface(
                    Modifier.requiredSize(46.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    IconFromDescriptor(
                        output.getIcon(emptyMap()),
                        contentDescription = output.label(emptyMap()),
                        size = 24.dp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkPreview() {
    AppTheme {
        Surface {
            AppIcon(
                modifier = Modifier.width(85.dp),
                label = FakeOpenStreetMapDisplayLink.group,
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                    IconFromDescriptor(
                        FakeOpenStreetMapDisplayLink.icon,
                        contentDescription = null,
                        size = 46.dp,
                        inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLinkPreview() {
    AppTheme {
        Surface {
            AppIcon(
                modifier = Modifier.width(85.dp),
                label = FakeOpenStreetMapDisplayLink.group,
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                    IconFromDescriptor(
                        FakeOpenStreetMapDisplayLink.icon,
                        contentDescription = null,
                        size = 46.dp,
                        inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    AppTheme {
        Surface {
            AppIcon(
                modifier = Modifier.width(85.dp),
            ) {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .requiredSize(46.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPlaceholderPreview() {
    AppTheme {
        Surface {
            AppIcon(
                modifier = Modifier.width(85.dp),
            ) {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .requiredSize(46.dp)
                )
            }
        }
    }
}
