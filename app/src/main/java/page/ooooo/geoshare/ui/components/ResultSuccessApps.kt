package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.runtime.retain.retain
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
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AndroidTools.AppType
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private sealed interface GridItem {
    data class App(val appDetails: AndroidTools.AppDetails, val actions: List<Action>) : GridItem
    class ShareButton : GridItem
    class Empty : GridItem
}

private val iconSize = 46.dp
private val dropdownButtonSize = 30.dp
private val dropdownButtonOffset = 20.dp

@Composable
fun ResultSuccessApps(
    onRun: (action: Action, i: Int?) -> Unit,
    onQueryAppDetails: (packageManager: PackageManager, packageName: String) -> AndroidTools.AppDetails? = { packageManager, packageName ->
        AndroidTools.queryAppDetails(packageManager, packageName)
    },
    onQueryApps: (packageManager: PackageManager) -> List<AndroidTools.App> = { packageManager ->
        AndroidTools.queryApps(packageManager)
    },
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        5
    } else {
        4
    }
    val apps = onQueryApps(context.packageManager)
    val actionsByAppDetails: Map<AndroidTools.AppDetails, List<Action>> = allOutputs.getAppActions(apps)
        .groupOrNull()
        .mapNotNull { (packageName, actions) ->
            onQueryAppDetails(context.packageManager, packageName)?.let { app -> app to actions }
        }
        .sortedBy { (appDetails) -> appDetails.label }
        .toMap()
    val grid = buildList {
        actionsByAppDetails.forEach { (appDetails, actions) -> add(GridItem.App(appDetails, actions)) }
        add(GridItem.ShareButton())
        repeat(columnCount - (actionsByAppDetails.size + 1) % columnCount) { add(GridItem.Empty()) }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.largeAdaptive),
        verticalArrangement = Arrangement.spacedBy(spacing.largeAdaptive),
    ) {
        grid.chunked(columnCount).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
                row.forEach { gridItem ->
                    when (gridItem) {
                        is GridItem.App -> gridItem.let { (app, actions) ->
                            ResultSuccessApp(app, actions, onRun)
                        }

                        is GridItem.ShareButton -> ResultSuccessAppShare {
                            allOutputs.getChooserAction()?.let { action -> onRun(action, null) }
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
    appDetails: AndroidTools.AppDetails,
    actions: List<Action>,
    onRun: (action: Action, i: Int?) -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by retain { mutableStateOf(false) }

    Column(
        Modifier
            .combinedClickable(onLongClick = {
                menuExpanded = true
            }) {
                actions.firstOrNull()?.let { action -> onRun(action, null) }
            }
            .weight(1f)
            .testTag("geoShareResultCardApp_${appDetails.packageName}"),
        verticalArrangement = Arrangement.spacedBy(spacing.tinyAdaptive)) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(iconSize),
        ) {
            Image(
                rememberDrawablePainter(appDetails.icon),
                appDetails.label,
            )
            actions.takeIf { it.size > 1 }?.let { actions ->
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
                        actions.forEach { action ->
                            DropdownMenuItem(
                                text = { action.Label() },
                                onClick = {
                                    menuExpanded = false
                                    onRun(action, null)
                                },
                            )
                        }
                    }
                }
            }
        }
        Text(
            appDetails.label,
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    onRun = { _, _ -> },
                    onQueryAppDetails = { _, packageName ->
                        AndroidTools.AppDetails(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onQueryApps = {
                        @Suppress("SpellCheckingInspection")
                        listOf(
                            "com.google.android.apps.maps",
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.generalmagic.magicearth",
                            "com.here.appDetails.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        ).map { AndroidTools.App(it, AppType.GEO_URI) }
                    },
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    onRun = { _, _ -> },
                    onQueryAppDetails = { _, packageName ->
                        AndroidTools.AppDetails(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onQueryApps = {
                        @Suppress("SpellCheckingInspection")
                        listOf(
                            "com.google.android.apps.maps",
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.generalmagic.magicearth",
                            "com.here.appDetails.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        ).map { AndroidTools.App(it, AppType.GEO_URI) }
                    },
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    onRun = { _, _ -> },
                    onQueryAppDetails = { _, packageName ->
                        AndroidTools.AppDetails(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onQueryApps = {
                        @Suppress("SpellCheckingInspection")
                        listOf(
                            "com.google.android.apps.maps",
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.generalmagic.magicearth",
                            "com.here.appDetails.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        ).map { AndroidTools.App(it, AppType.GEO_URI) }
                    },
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
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessApps(
                    onRun = { _, _ -> },
                    onQueryAppDetails = { _, packageName ->
                        AndroidTools.AppDetails(
                            packageName,
                            "$packageName label",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onQueryApps = {
                        @Suppress("SpellCheckingInspection")
                        listOf(
                            "com.google.android.apps.maps",
                            "app.comaps.fdroid",
                            "app.organicmaps",
                            "com.generalmagic.magicearth",
                            "com.here.appDetails.maps",
                            "cz.seznam.mapy",
                            "net.osmand.plus",
                            "us.spotco.maps",
                        ).map { AndroidTools.App(it, AppType.GEO_URI) }
                    },
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
                    onRun = { _, _ -> },
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
                    onRun = { _, _ -> },
                )
            }
        }
    }
}
