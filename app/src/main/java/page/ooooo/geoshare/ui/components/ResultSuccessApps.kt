package page.ooooo.geoshare.ui.components

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
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
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.MagicEarthOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private sealed interface GridItem {
    data class App(val app: IntentTools.App, val items: List<Output.Item<Action.OpenApp>>) : GridItem
    class ShareButton : GridItem
    class Empty : GridItem
}

private val iconSize = 46.dp
private val dropdownButtonSize = 30.dp
private val dropdownButtonOffset = 20.dp

@Composable
fun ResultSuccessApps(
    items: List<Output.Item<Action>>,
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
    val apps: Map<IntentTools.App, List<Output.Item<Action.OpenApp>>> = items
        .mapNotNull { (action, label) -> (action as? Action.OpenApp)?.let { action -> Output.Item(action, label) } }
        .groupBy { (action) -> action.packageName }
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
    val openChooserAction: Action.OpenChooser? =
        items.firstNotNullOfOrNull { (action) -> (action as? Action.OpenChooser) }

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
                        is GridItem.App -> gridItem.let { (app, items) ->
                            ResultSuccessApp(
                                packageName = app.packageName,
                                label = app.label,
                                icon = app.icon,
                                items = items,
                                onRun = onRun,
                            )
                        }

                        is GridItem.ShareButton -> ResultSuccessAppShare { openChooserAction?.let(onRun) }

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
    packageName: String,
    label: String,
    icon: Drawable,
    items: List<Output.Item<Action.OpenApp>>,
    onRun: (action: Action) -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .combinedClickable(onLongClick = {
                menuExpanded = true
            }) {
                items.firstOrNull()?.let { (action) -> onRun(action) }
            }
            .weight(1f)
            .testTag("geoShareResultCardApp_${packageName}"),
        verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(iconSize),
        ) {
            Image(
                rememberDrawablePainter(icon),
                label,
            )
            items.takeIf { it.size > 1 }?.let {
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
                        items.forEach { (action, label) ->
                            DropdownMenuItem(
                                text = { Text(label()) },
                                onClick = { onRun(action) },
                            )
                        }
                    }
                }
            }
        }
        Text(
            label,
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
                @Suppress("SpellCheckingInspection")
                val packageNames = listOf(
                    IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                    MagicEarthOutput.PACKAGE_NAME,
                    "app.comaps.fdroid",
                    "app.organicmaps",
                    "com.here.app.maps",
                    "cz.seznam.mapy",
                    "net.osmand.plus",
                    "us.spotco.maps",
                )
                val context = LocalContext.current
                val position = Position.example
                ResultSuccessApps(
                    items = Outputs.getActions(position, packageNames),
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
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
                @Suppress("SpellCheckingInspection")
                val packageNames = listOf(
                    IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                    MagicEarthOutput.PACKAGE_NAME,
                    "app.comaps.fdroid",
                    "app.organicmaps",
                    "com.here.app.maps",
                    "cz.seznam.mapy",
                    "net.osmand.plus",
                    "us.spotco.maps",
                )
                val context = LocalContext.current
                val position = Position.example
                ResultSuccessApps(
                    items = Outputs.getActions(position, packageNames),
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
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
                val packageNames = listOf(
                    IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                )
                val context = LocalContext.current
                val position = Position.example
                ResultSuccessApps(
                    items = Outputs.getActions(position, packageNames),
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
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
                val packageNames = listOf(
                    IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                )
                val context = LocalContext.current
                val position = Position.example
                ResultSuccessApps(
                    items = Outputs.getActions(position, packageNames),
                    onRun = {},
                    intentTools = object : IntentTools() {
                        override fun queryApp(packageManager: PackageManager, packageName: String) = App(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
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
                    items = emptyList(),
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
                    items = emptyList(),
                    onRun = {},
                )
            }
        }
    }
}
