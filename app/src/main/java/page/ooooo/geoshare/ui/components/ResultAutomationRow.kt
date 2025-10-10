package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationImplementation
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultAutomationRow(
    currentState: HasAutomation,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
    var automationCounterSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentState) {
        if (currentState is AutomationWaiting) {
            automationCounterSec = currentState.automation.delaySec
            while (automationCounterSec > 0) {
                delay(1000L)
                automationCounterSec--
            }
        }
    }

    Row(
        Modifier
            .padding(top = Spacing.large)
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(currentState is AutomationFinished, Modifier.weight(1f)) {
            Text(
                stringResource(R.string.conversion_succeeded_apps_headline),
                Modifier.padding(horizontal = Spacing.small),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        AnimatedVisibility(currentState is AutomationFinished) {
            Button(
                onNavigateToUserPreferencesAutomationScreen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            ) {
                Text(stringResource(R.string.user_preferences_automation_title))
            }
        }
        ResultAutomationNotification(currentState is AutomationWaiting) {
            currentState.automation.takeIf { it is Automation.CanWait }?.let { automation ->
                (automation as Automation.CanWait).waitingText(automationCounterSec)
            } ?: ""
        }
        ResultAutomationNotification(currentState is AutomationSucceeded) {
            currentState.automation.takeIf { it is Automation.HasSuccessMessage }?.let { automation ->
                (automation as Automation.HasSuccessMessage).successText()
            } ?: ""
        }
        ResultAutomationNotification(
            currentState is AutomationFailed,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            currentState.automation.takeIf { it is Automation.CanFail }?.let { automation ->
                (automation as Automation.CanFail).errorText()
            } ?: ""
        }
        AnimatedVisibility(currentState is AutomationWaiting) {
            FilledIconButton(
                onCancel,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                Icon(
                    Icons.Default.Close,
                    stringResource(R.string.conversion_loading_indicator_cancel),
                )
            }
        }
    }
}

@Composable
private fun RowScope.ResultAutomationNotification(
    visible: Boolean,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    text: @Composable () -> String,
) {
    AnimatedVisibility(visible, Modifier.weight(1f)) {
        Card(
            shape = MaterialTheme.shapes.extraSmall,
            colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        ) {
            Row(
                Modifier
                    .fillMaxHeight()
                    .padding(horizontal = Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text(),
                    style = MaterialTheme.typography.labelLarge,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationFinished(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationFinished(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationWaiting(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWaitingPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationWaiting(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SucceededPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationSucceeded(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarSucceededPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationSucceeded(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FailedPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationFailed(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFailedPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    currentState = AutomationFailed(
                        "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        Position("50.123456", "11.123456"),
                        AutomationImplementation.OpenApp(GOOGLE_MAPS_PACKAGE_NAME),
                    ),
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
            }
        }
    }
}
