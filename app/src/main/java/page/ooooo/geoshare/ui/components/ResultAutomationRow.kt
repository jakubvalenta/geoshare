package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultAutomationRow(
    automationState: AutomationState,
    automationCounterSec: Int,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
    onShare: () -> Boolean,
) {
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(automationState is AutomationState.Nothing) {
            Button(
                {
                    if (!onShare()) {
                        Toast.makeText(
                            context, R.string.conversion_succeeded_apps_not_found, Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Modifier.padding(start = Spacing.tiny, end = Spacing.tiny / 2),
            ) {
                Text(stringResource(R.string.conversion_succeeded_share))
            }
        }
        AnimatedVisibility(automationState is AutomationState.Nothing) {
            Button(onNavigateToUserPreferencesAutomationScreen) {
                Icon(
                    painterResource(R.drawable.automation_24px),
                    null,
                    Modifier.padding(end = Spacing.tiny),
                )
                Text(stringResource(R.string.user_preferences_automation_title))
            }
        }
        AnimatedVisibility(automationState !is AutomationState.Nothing, Modifier.weight(1f)) {
            Card(
                shape = MaterialTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = if (automationState is AutomationState.Failed) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                    contentColor = if (automationState is AutomationState.Failed) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                ),
            ) {
                Row(
                    Modifier
                        .fillMaxHeight()
                        .padding(horizontal = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (automationState) {
                            is AutomationState.Nothing -> stringResource(R.string.user_preferences_automation_title)
                            is AutomationState.Running -> stringResource(
                                automationState.messageResId, automationCounterSec
                            )

                            is AutomationState.Failed -> stringResource(automationState.messageResId)
                            is AutomationState.Succeeded -> stringResource(automationState.messageResId)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        AnimatedVisibility(automationState is AutomationState.Running) {
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

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    automationState = AutomationState.Nothing(),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
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
                    automationState = AutomationState.Nothing(),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    automationState = AutomationState.Running(R.string.conversion_automation_open_app_running),
                    automationCounterSec = 3,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkRunningPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultAutomationRow(
                    automationState = AutomationState.Running(R.string.conversion_automation_open_app_running),
                    automationCounterSec = 3,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
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
                    automationState = AutomationState.Succeeded(R.string.conversion_automation_open_app_succeeded),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
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
                    automationState = AutomationState.Succeeded(R.string.conversion_automation_open_app_succeeded),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
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
                    automationState = AutomationState.Failed(R.string.conversion_automation_open_app_failed),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
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
                    automationState = AutomationState.Failed(R.string.conversion_automation_open_app_failed),
                    automationCounterSec = 0,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                    onShare = { true },
                )
            }
        }
    }
}
