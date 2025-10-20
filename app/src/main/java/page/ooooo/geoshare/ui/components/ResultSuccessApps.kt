package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private sealed class GridItem {
    class App(val app: IntentTools.App) : GridItem()
    class ShareButton : GridItem()
    class Empty : GridItem()
}

@Composable
fun ResultSuccessApps(
    apps: List<IntentTools.App>,
    onOpenApp: (packageName: String) -> Boolean,
    onOpenChooser: () -> Boolean,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val columnCount = 4
    val iconSize = 46.dp
    val gridItems = apps.map { GridItem.App(it) } +
            listOf(GridItem.ShareButton()) +
            List(columnCount - (apps.size + 1) % columnCount) { GridItem.Empty() }

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
                        is GridItem.App ->
                            Column(
                                Modifier
                                    .clickable {
                                        if (!onOpenApp(gridItem.app.packageName)) {
                                            Toast.makeText(
                                                context,
                                                R.string.conversion_automation_open_app_failed,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .weight(1f)
                                    .testTag("geoShareResultCardApp_${gridItem.app.packageName}"),
                                verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                                Image(
                                    rememberDrawablePainter(gridItem.app.icon),
                                    gridItem.app.label,
                                    Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(iconSize),
                                )
                                Text(
                                    gridItem.app.label,
                                    Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                        is GridItem.ShareButton ->
                            Column(Modifier.weight(1f)) {
                                FilledIconButton(
                                    {
                                        if (!onOpenChooser()) {
                                            Toast.makeText(
                                                context,
                                                R.string.conversion_succeeded_apps_not_found,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
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

                        is GridItem.Empty ->
                            Box(Modifier.weight(1f))
                    }
                }
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
            Column {
                val context = LocalContext.current
                ResultSuccessApps(
                    apps = List(8) { index ->
                        IntentTools.App(
                            BuildConfig.APPLICATION_ID,
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onOpenApp = { true },
                    onOpenChooser = { true },
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
                            BuildConfig.APPLICATION_ID,
                            "My Map ${index + 1}",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                        )
                    },
                    onOpenApp = { true },
                    onOpenChooser = { true },
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
                    onOpenApp = { true },
                    onOpenChooser = { true },
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
                    onOpenApp = { true },
                    onOpenChooser = { true },
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
                    onOpenApp = { true },
                    onOpenChooser = { true },
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
                    onOpenApp = { true },
                    onOpenChooser = { true },
                )
            }
        }
    }
}
