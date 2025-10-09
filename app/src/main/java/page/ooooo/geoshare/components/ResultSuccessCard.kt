package page.ooooo.geoshare.components

import android.content.res.Configuration
import android.widget.Toast
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.ui.components.AutomationState
import page.ooooo.geoshare.ui.components.ResultAutomationRow
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSuccessCard(
    apps: List<IntentTools.App>,
    automation: Automation,
    position: Position,
    onCopy: (text: String) -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onOpenApp: (packageName: String) -> Boolean,
    onShare: () -> Boolean,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    val openAppCoroutineScope = rememberCoroutineScope()
    val columnCount = 4
    val automationDelaySec = 5

    var automationState by remember { mutableStateOf<AutomationState>(AutomationState.Nothing()) }
    var automationCounterSec by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }

    // TODO Don't run automatic action after changing preferences and going back to this screen

    // TODO Add instrumented test

    LaunchedEffect(automation) {
        when (automation.type) {
            Automation.Type.NOTHING -> Unit
            Automation.Type.COPY_APPLE_MAPS_URI -> {
                onCopy(position.toAppleMapsUriString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.COPY_COORDS_DEC -> {
                onCopy(position.toCoordsDecString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.COPY_COORDS_NSWE_DEC -> {
                onCopy(position.toNorthSouthWestEastDecCoordsString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.COPY_GEO_URI -> {
                onCopy(position.toGeoUriString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.COPY_GOOGLE_MAPS_URI -> {
                onCopy(position.toGoogleMapsUriString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.COPY_MAGIC_EARTH_URI -> {
                onCopy(position.toMagicEarthUriString())
                automationState = AutomationState.Succeeded(R.string.conversion_automation_copy_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.OPEN_APP -> automation.packageName?.takeIf { it.isNotEmpty() }?.let { packageName ->
                automationState = AutomationState.Running(R.string.conversion_automation_open_app_running)
                openAppCoroutineScope.launch {
                    try {
                        automationCounterSec = automationDelaySec
                        while (automationCounterSec > 0) {
                            delay(1000L)
                            automationCounterSec--
                        }
                        automationState = if (onOpenApp(packageName)) {
                            AutomationState.Succeeded(R.string.conversion_automation_open_app_succeeded)
                        } else {
                            AutomationState.Failed(R.string.conversion_automation_open_app_failed)
                        }
                        delay(automationDelaySec * 1000L)
                    } catch (_: CancellationException) {
                        // Do nothing
                    } finally {
                        automationState = AutomationState.Nothing()
                    }
                }
            }

            Automation.Type.SAVE_GPX -> {
                onSave()
                AutomationState.Succeeded(R.string.conversion_automation_save_gpx_succeeded)
                delay(automationDelaySec * 1000L)
                automationState = AutomationState.Nothing()
            }

            Automation.Type.SHARE -> {
                if (onShare()) {
                    automationState = AutomationState.Succeeded(R.string.conversion_automation_share_succeeded)
                    delay(automationDelaySec * 1000L)
                    automationState = AutomationState.Nothing()
                } else {
                    automationState = AutomationState.Succeeded(R.string.conversion_automation_share_failed)
                }
            }
        }
    }

    Column {
        ResultCard(
            main = {
                SelectionContainer {
                    Text(
                        position.toNorthSouthWestEastDecCoordsString(),
                        Modifier.testTag("geoShareConversionSuccessPositionCoordinates"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                position.toParamsString().takeIf { it.isNotEmpty() }?.let {
                    SelectionContainer {
                        Text(
                            it,
                            Modifier.testTag("geoShareConversionSuccessPositionParams"),
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
                        onDismissRequest = { menuExpanded = false },
                    ) {
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
            },
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
            apps.chunked(columnCount).forEach { appsChunk ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    appsChunk.forEach { app ->
                        Column(
                            Modifier
                                .clickable {
                                    if (!onOpenApp(app.packageName)) {
                                        Toast.makeText(
                                            context,
                                            R.string.conversion_automation_open_app_failed,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .weight(1f)
                                .testTag("geoShareResultCardApp_${app.packageName}"),
                            verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                            Image(
                                rememberDrawablePainter(app.icon),
                                app.label,
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .widthIn(max = 46.dp),
                            )
                            Text(
                                app.label,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    repeat(columnCount - apps.size) {
                        Box(Modifier.weight(1f))
                    }
                }
            }
            ResultAutomationRow(
                automationState = automationState,
                automationCounterSec = automationCounterSec,
                onCancel = { openAppCoroutineScope.cancel() },
                onNavigateToUserPreferencesAutomationScreen = onNavigateToUserPreferencesAutomationScreen,
                onShare = onShare,
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
            ResultSuccessCard(
                apps = List(4) { index ->
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map ${index + 1}",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    )
                },
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = List(4) { index ->
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map ${index + 1}",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    )
                },
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456", q = "Berlin, Germany", z = "13"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(
                    IntentTools.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                automation = Automation(Automation.Type.NOTHING),
                position = Position(
                    points = listOf(
                        "59.1293656" to "11.4585672",
                        "59.4154007" to "11.659710599999999",
                        "59.147731699999994" to "11.550661199999999",
                    ),
                ),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
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
                apps = listOf(),
                automation = Automation(Automation.Type.NOTHING),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onNavigateToUserPreferencesAutomationScreen = {},
                onOpenApp = { true },
                onShare = { true },
                onSave = {},
            )
        }
    }
}
