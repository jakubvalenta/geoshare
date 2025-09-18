package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultSuccessCard(
    geoUriApps: List<ConversionViewModel.App>,
    position: Position,
    onCopy: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenChooser: () -> Unit,
    onSave: () -> Unit,
) {
    val columnCount = 4
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        ResultCard(
            main = {
                SelectionContainer {
                    Text(
                        position.toNorthSouthWestEastDecCoordsString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                position.toParamsString().takeIf { it.isNotEmpty() }?.let {
                    SelectionContainer {
                        Text(
                            it,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                position.points?.takeIf { it.size > 1 }?.mapIndexed { i, (lat, lon) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                        Text(
                            stringResource(R.string.conversion_succeeded_point_number, i + 1),
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        SelectionContainer {
                            Text(
                                Position(lat, lon).toCoordsDecString(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            },
            after = {
                Box {
                    IconButton({ menuExpanded = true }) {
                        Icon(
                            painterResource(R.drawable.content_copy_24px),
                            contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        listOf(
                            position.toCoordsDecString(),
                            position.toNorthSouthWestEastDecCoordsString(),
                            position.toGeoUriString(),
                            position.toGoogleMapsUriString(),
                            position.toAppleMapsUriString(),
                            position.toMagicEarthUriString()
                        ).map { text ->
                            DropdownMenuItem(
                                text = { Text(text) },
                                onClick = {
                                    menuExpanded = false
                                    onCopy(text)
                                },
                            )
                        }
                    }
                }
            },
            chips = {
                ResultCardChip(stringResource(R.string.conversion_succeeded_copy_geo)) {
                    onCopy(position.toGeoUriString())
                }
                ResultCardChip(stringResource(R.string.conversion_succeeded_copy_google_maps)) {
                    onCopy(position.toGoogleMapsUriString())
                }
                ResultCardChip(
                    stringResource(R.string.conversion_succeeded_save_gpx),
                    Modifier.padding(end = Spacing.small),
                ) {
                    onSave()
                }
            }
        )

        Text(
            stringResource(R.string.conversion_succeeded_apps_headline),
            Modifier
                .padding(
                    start = Spacing.small,
                    top = Spacing.large,
                    end = Spacing.small,
                    bottom = Spacing.medium,
                ),
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.tiny),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            for (geoUriAppsChunk in geoUriApps.chunked(columnCount)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    for (geoUriApp in geoUriAppsChunk) {
                        Column(
                            Modifier
                                .clickable { onOpenApp(geoUriApp.packageName) }
                                .weight(1f)
                                .testTag("geoShareResultCardApp_${geoUriApp.packageName}"),
                            verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                            Image(
                                rememberDrawablePainter(geoUriApp.icon),
                                geoUriApp.label,
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .widthIn(max = 46.dp),
                            )
                            Text(
                                geoUriApp.label,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    repeat(columnCount - geoUriAppsChunk.size) {
                        Box(Modifier.weight(1f))
                    }
                }
            }
            Button({ onOpenChooser() }, Modifier.padding(horizontal = Spacing.tiny)) {
                Text(stringResource(R.string.conversion_succeeded_share))
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
            ResultSuccessCard(
                geoUriApps = List(4) { index ->
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map ${index + 1}",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    )
                },
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
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
            ResultSuccessCard(
                geoUriApps = List(4) { index ->
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map ${index + 1}",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    )
                },
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneAppPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOneAppPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParamsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkParamsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultSuccessCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoAppsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCard(
                geoUriApps = listOf(),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoAppsPreview() {
    AppTheme {
        Surface {
            ResultSuccessCard(
                geoUriApps = listOf(),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onOpenApp = {},
                onOpenChooser = {},
                onSave = {},
            )
        }
    }
}
