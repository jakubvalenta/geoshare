package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.MagicEarthOutput
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private sealed interface GridItem {
    data class App(val app: IntentTools.App) : GridItem
    class ShareButton : GridItem
    class Empty : GridItem
}

@Composable
fun ResultSuccessApps(
    apps: List<IntentTools.App>,
    position: Position,
    onOpenApp: (packageName: String, uriString: String) -> Unit,
    onOpenChooser: (uriString: String) -> Unit,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val spacing = LocalSpacing.current
    val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        5
    } else {
        4
    }
    val gridItems =
        apps.map { GridItem.App(it) } + listOf(GridItem.ShareButton()) + List(columnCount - (apps.size + 1) % columnCount) { GridItem.Empty() }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        gridItems.chunked(columnCount).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                row.forEach { gridItem ->
                    when (gridItem) {
                        is GridItem.App -> ResultSuccessApp(gridItem.app, position, onOpenApp)

                        is GridItem.ShareButton -> ResultSuccessShare(position, onOpenChooser)

                        is GridItem.Empty -> Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private val iconSize = 46.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowScope.ResultSuccessApp(
    app: IntentTools.App,
    position: Position,
    onOpenApp: (packageName: String, uriString: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val openAppUriStrings = Outputs.getOpenAppAllUriStrings(app.packageName, position)
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .combinedClickable(onLongClick = {
                menuExpanded = true
            }) {
                onOpenApp(app.packageName, Outputs.getOpenAppUriString(app.packageName, position))
            }
            .weight(1f)
            .testTag("geoShareResultCardApp_${app.packageName}"),
        verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(iconSize),
        ) {
            Image(
                rememberDrawablePainter(app.icon),
                app.label,
            )
            if (openAppUriStrings.size > 1) {
                Box(Modifier.align(Alignment.TopEnd)) {
                    FilledIconButton(
                        { menuExpanded = true },
                        Modifier.size(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ),
                    ) {
                        Icon(
                            painterResource(R.drawable.more_horiz_24px),
                            contentDescription = stringResource(R.string.nav_menu_content_description),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        openAppUriStrings.forEach { uriString ->
                            DropdownMenuItem(
                                text = { Text(uriString, overflow = TextOverflow.Ellipsis, maxLines = 1) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenApp(app.packageName, uriString)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Share,
                                        stringResource(R.string.conversion_succeeded_share),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        Text(
            app.label,
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun RowScope.ResultSuccessShare(position: Position, onOpenChooser: (uriString: String) -> Unit) {
    Column(Modifier.weight(1f)) {
        FilledIconButton(
            {
                onOpenChooser(Outputs.default.getPositionUriString(position))
            },
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(iconSize),
        ) {
            Icon(
                Icons.Default.Share,
                stringResource(R.string.conversion_succeeded_share),
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
            Column {
                val context = LocalContext.current
                ResultSuccessApps(
                    apps = List(8) { index ->
                        IntentTools.App(
                            if (index % 2 == 0) BuildConfig.APPLICATION_ID else MagicEarthOutput.packageNames[0],
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
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
                ResultSuccessApps(
                    apps = List(8) { index ->
                        IntentTools.App(
                            if (index % 2 == 0) BuildConfig.APPLICATION_ID else MagicEarthOutput.packageNames[0],
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneAppPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                ResultSuccessApps(
                    apps = listOf(
                        IntentTools.App(
                            BuildConfig.APPLICATION_ID,
                            "My Map App",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        ),
                    ),
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOneAppPreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                ResultSuccessApps(
                    apps = listOf(
                        IntentTools.App(
                            BuildConfig.APPLICATION_ID,
                            "My Map App",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        ),
                    ),
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoAppsPreview() {
    AppTheme {
        Surface {
            Column {
                ResultSuccessApps(
                    apps = listOf(),
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoAppsPreview() {
    AppTheme {
        Surface {
            Column {
                ResultSuccessApps(
                    apps = listOf(),
                    position = Position.example,
                    onOpenApp = { _, _ -> },
                    onOpenChooser = {},
                )
            }
        }
    }
}
