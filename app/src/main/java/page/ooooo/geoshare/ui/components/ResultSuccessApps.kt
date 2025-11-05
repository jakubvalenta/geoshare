package page.ooooo.geoshare.ui.components

import android.content.pm.PackageManager
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private sealed interface GridItem {
    data class App(val app: IntentTools.App, val outputs: List<Output.App<Position>>) : GridItem
    class ShareButton : GridItem
    class Empty : GridItem
}

private val iconSize = 46.dp
private val dropdownButtonSize = 30.dp
private val dropdownButtonOffset = 20.dp

@Composable
fun ResultSuccessApps(
    position: Position,
    onRun: (action: Action) -> Unit,
    intentTools: IntentTools = IntentTools(),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        5
    } else {
        4
    }
    val appOutputs = allOutputGroups.getAppOutputs(
        packageNames = intentTools.queryGeoUriPackageNames(context.packageManager),
    )
    val apps: Map<IntentTools.App, List<Output.App<Position>>> = appOutputs
        .groupBy { it.packageName }
        .mapNotNull { (packageName, items) ->
            intentTools.queryApp(context.packageManager, packageName)?.let { app -> app to items }
        }
        .sortedBy { (app) -> app.label }
        .toMap()
    val grid = buildList {
        apps.forEach { (app, items) -> add(GridItem.App(app, items)) }
        add(GridItem.ShareButton())
        repeat(columnCount - (apps.size + 1) % columnCount) { add(GridItem.Empty()) }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        grid.chunked(columnCount).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                row.forEach { gridItem ->
                    when (gridItem) {
                        is GridItem.App -> gridItem.let { (app, outputs) ->
                            ResultSuccessApp(position, app, outputs, onRun)
                        }

                        is GridItem.ShareButton -> ResultSuccessAppShare {
                            allOutputGroups.getChooserOutput()?.getAction(position)?.let(onRun)
                        }

                        is GridItem.Empty -> ResultSuccessAppEmpty()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowScope.ResultSuccessApp(
    position: Position,
    app: IntentTools.App,
    outputs: List<Output.App<Position>>,
    onRun: (action: Action) -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .combinedClickable(onLongClick = {
                menuExpanded = true
            }) {
                outputs.firstOrNull()?.getAction(position)?.let(onRun)
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
            outputs.drop(1).takeIf { it.isNotEmpty() }?.let { outputs ->
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(dropdownButtonOffset, -dropdownButtonOffset),
                ) {
                    FilledIconButton(
                        { menuExpanded = true },
                        Modifier.size(dropdownButtonSize),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            painterResource(R.drawable.more_horiz_24px),
                            contentDescription = stringResource(R.string.nav_menu_content_description),
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        outputs.forEach {
                            DropdownMenuItem(
                                text = { Text(it.label()) },
                                onClick = { onRun(it.getAction(position)) },
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
fun RowScope.ResultSuccessAppShare(onClick: () -> Unit) {
    Column(Modifier.weight(1f)) {
        FilledIconButton(
            onClick,
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

@Composable
fun RowScope.ResultSuccessAppEmpty() {
    Box(Modifier.weight(1f))
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
                    position = Position.example,
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )

                        @Suppress("SpellCheckingInspection")
                        override fun queryGeoUriPackageNames(packageManager: PackageManager) = listOf(
                            GOOGLE_MAPS_PACKAGE_NAME,
                            MagicEarthOutputGroup.PACKAGE_NAME,
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.here.app.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        )
                    }
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
                    position = Position.example,
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )

                        @Suppress("SpellCheckingInspection")
                        override fun queryGeoUriPackageNames(packageManager: PackageManager) = listOf(
                            GOOGLE_MAPS_PACKAGE_NAME,
                            MagicEarthOutputGroup.PACKAGE_NAME,
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.here.app.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        )
                    }
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
                    position = Position.example,
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )

                        override fun queryGeoUriPackageNames(packageManager: PackageManager) = listOf(
                            GOOGLE_MAPS_PACKAGE_NAME,
                        )
                    }
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
                    position = Position.example,
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )

                        override fun queryGeoUriPackageNames(packageManager: PackageManager) = listOf(
                            GOOGLE_MAPS_PACKAGE_NAME,
                        )
                    }
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
                    position = Position.example,
                    onRun = {},
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
                    position = Position.example,
                    onRun = {},
                )
            }
        }
    }
}
