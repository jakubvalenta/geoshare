package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.outputs.GeoUriOutputManager
import page.ooooo.geoshare.lib.outputs.allOutputManagers
import page.ooooo.geoshare.lib.outputs.getOutputs
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun ResultSuccessAutomation(
    currentState: HasResult,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var counterSec by remember { mutableIntStateOf(0) }
    var targetState by remember {
        mutableStateOf(
            if (
                animationsEnabled &&
                (currentState is AutomationWaiting ||
                        currentState is AutomationSucceeded ||
                        currentState is AutomationFailed)
            ) {
                // To make the message appear with an animation, first start with null state and only later change it to
                // the current state (using LaunchedEffect).
                null
            } else {
                currentState
            }
        )
    }

    LaunchedEffect(currentState) {
        targetState = currentState
    }

    AnimatedContent(
        targetState,
        modifier = Modifier
            .padding(top = spacing.large)
            .fillMaxWidth()
            .height(40.dp),
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else if (
                this.targetState is AutomationWaiting ||
                this.targetState is AutomationSucceeded ||
                this.targetState is AutomationFailed
            ) {
                slideInHorizontally { fullWidth -> -fullWidth } togetherWith fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        }
    ) { targetState ->
        when (targetState) {
            is AutomationWaiting ->
                ResultAutomationRow {
                    ResultAutomationMessage(Modifier.testTag("geoShareConversionSuccessAutomationCounter")) {
                        LaunchedEffect(targetState.automation) {
                            counterSec = targetState.automation.delay.toInt(DurationUnit.SECONDS)
                            while (counterSec > 0) {
                                delay(1.seconds)
                                counterSec--
                            }
                        }
                        targetState.automation.waitingText(counterSec)
                    }
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

            is AutomationSucceeded ->
                ResultAutomationRow {
                    ResultAutomationMessage(Modifier.testTag("geoShareConversionSuccessAutomationSuccess")) {
                        targetState.automation.successText()
                    }
                }

            is AutomationFailed ->
                ResultAutomationRow {
                    ResultAutomationMessage(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        targetState.automation.errorText()
                    }
                }

            else ->
                ResultAutomationRow {
                    Text(
                        stringResource(R.string.conversion_succeeded_apps_headline),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Button(
                        onNavigateToUserPreferencesAutomationScreen,
                        Modifier.testTag("geoShareConversionSuccessAutomationPreferencesButton"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    ) {
                        Text(stringResource(R.string.user_preferences_automation_title))
                    }
                }
        }
    }
}

@Composable
private fun ResultAutomationRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.ResultAutomationMessage(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    text: @Composable () -> String,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier
            .weight(1f)
            .background(containerColor, MaterialTheme.shapes.extraSmall)
            .fillMaxHeight()
            .padding(horizontal = spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text(),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val clipboard = LocalClipboard.current
            val saveGpxLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
            ResultSuccessAutomation(
                currentState = AutomationWaiting(
                    stateContext = ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                    runContext = ConversionRunContext(context, clipboard, saveGpxLauncher),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkWaitingPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val clipboard = LocalClipboard.current
            val saveGpxLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
            ResultSuccessAutomation(
                currentState = AutomationWaiting(
                    stateContext = ConversionStateContext(userPreferencesRepository = FakeUserPreferencesRepository()),
                    runContext = ConversionRunContext(context, clipboard, saveGpxLauncher),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SucceededPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarSucceededPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FailedPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFailedPreview() {
    AppTheme {
        Surface {
            ResultSuccessAutomation(
                currentState = AutomationFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    outputs = allOutputManagers.getOutputs(emptyList()),
                    automation = GeoUriOutputManager.OpenAppAutomation(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}
