package page.ooooo.geoshare.ui.components

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
import page.ooooo.geoshare.BuildConfig
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
    data class App(val app: IntentTools.App) : GridItem
    class ShareButton : GridItem
    class Empty : GridItem
}

private val iconSize = 46.dp
private val dropdownButtonSize = 30.dp
private val dropdownButtonOffset = 20.dp

@Composable
fun ResultSuccessApps(
    apps: List<IntentTools.App>,
    position: Position,
    onRun: (action: Action) -> Unit,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val spacing = LocalSpacing.current
    val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        5
    } else {
        4
    }
    val gridItems = buildList {
        apps.forEach { add(GridItem.App(it)) }
        add(GridItem.ShareButton())
        repeat(columnCount - (apps.size + 1) % columnCount) { add(GridItem.Empty()) }
    }
    val openAppItems: List<Output.Item<Action.OpenApp>> =
        Outputs.getActions(position).mapNotNull { (action, label) ->
            if (action is Action.OpenApp) Output.Item(action, label) else null
        }
    val openChooserAction: Action.OpenChooser? =
        Outputs.getActions(position).firstNotNullOfOrNull { it.action as? Action.OpenChooser }

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
                        is GridItem.App -> gridItem.let { (app) ->
                            ResultSuccessApp(
                                packageName = app.packageName,
                                label = app.label,
                                icon = app.icon,
                                items = openAppItems.filter { (action) -> action.packageName == app.packageName },
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
                items.firstOrNull()?.action?.let(onRun)
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
                val context = LocalContext.current
                ResultSuccessApps(
                    apps = List(8) { index ->
                        IntentTools.App(
                            if (index % 2 == 0) BuildConfig.APPLICATION_ID else MagicEarthOutput.PACKAGE_NAME,
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    position = Position.example,
                    onRun = {},
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
                            if (index % 2 == 0) BuildConfig.APPLICATION_ID else MagicEarthOutput.PACKAGE_NAME,
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    position = Position.example,
                    onRun = {},
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
                    onRun = {},
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
                    onRun = {},
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
                    apps = listOf(),
                    position = Position.example,
                    onRun = {},
                )
            }
        }
    }
}
