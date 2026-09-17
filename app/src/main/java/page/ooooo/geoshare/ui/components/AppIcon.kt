package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
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
import page.ooooo.geoshare.lib.extensions.zipWithNextFirstNull
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.ui.OutputDetail
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppIcon(
    label: String?,
    menu: @Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    var expanded by retain { mutableStateOf(false) }

    Box(
        modifier
            .width(78.dp)
            .run {
                if (enabled) {
                    combinedClickable(
                        role = Role.Button,
                        onLongClick = { expanded = true },
                        onClick = onClick,
                    )
                } else {
                    this
                }
            },
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.extraTiny),
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
            { expanded = true },
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
        menu(expanded) { expanded = false }
    }
}

@Composable
fun AppMenu(
    expanded: Boolean,
    outputDetails: List<OutputDetail<Output>>,
    onClick: (Output) -> Unit,
    onDismissRequest: () -> Unit,
    onHide: (() -> Unit)?,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        shape = ShapeDefaults.Large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        outputDetails
            .zipWithNextFirstNull { prevOutputDetail, outputDetail ->
                DropdownMenuItem(
                    text = {
                        Text(
                            outputDetail.label(),
                            Modifier.testTag("geoShareAppOutput"),
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        onClick(outputDetail.output)
                    },
                    leadingIcon = {
                        IconFromDescriptor(
                            outputDetail.menuIcon?.takeIf { it != prevOutputDetail?.menuIcon } ?: SpacerIconDescriptor,
                            contentDescription = null,
                        )
                    },
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
                    onDismissRequest()
                    onHide()
                },
                leadingIcon = {
                    Icon(Icons.Default.Close, null)
                }
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
            AppIcon(
                label = FakeOpenStreetMapDisplayLink.group,
                menu = { _, _ -> },
                onClick = {},
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
            AppIcon(
                label = FakeOpenStreetMapDisplayLink.group,
                menu = { _, _ -> },
                onClick = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = ShareDisplayGeoUriOutput(coordinateConverter)
            AppIcon(
                label = null,
                menu = { _, _ -> },
                onClick = {},
            ) {
                Surface(
                    Modifier.requiredSize(46.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    IconFromDescriptor(
                        output.getIcon(null),
                        contentDescription = output.label(null),
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = ShareDisplayGeoUriOutput(coordinateConverter)
            AppIcon(
                label = null,
                menu = { _, _ -> },
                onClick = {},
            ) {
                Surface(
                    Modifier.requiredSize(46.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ) {
                    IconFromDescriptor(
                        output.getIcon(null),
                        contentDescription = output.label(null),
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
                label = FakeOpenStreetMapDisplayLink.group,
                menu = { _, _ -> },
                onClick = {},
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
                label = FakeOpenStreetMapDisplayLink.group,
                menu = { _, _ -> },
                onClick = {},
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
                label = null,
                menu = { _, _ -> },
                onClick = {},
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
                label = null,
                menu = { _, _ -> },
                onClick = {},
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
