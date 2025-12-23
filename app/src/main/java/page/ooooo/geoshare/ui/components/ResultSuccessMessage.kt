package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.conversion.ActionFailed
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionSucceeded
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationFindingFailed
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.features.Features
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSuccessMessage(
    currentState: ConversionState.HasResult,
    automationFeatureValid: Boolean,
    loadingIndicator: LoadingIndicator?,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
    fun isMessageShown(state: ConversionState.HasResult?): Boolean =
        state is ActionWaiting && state.action is Automation.HasDelay ||
            state is ActionSucceeded && state.action is Action.HasSuccessMessage ||
            state is ActionFailed && state.action is Action.HasErrorMessage ||
            state is LocationFindingFailed ||
            loadingIndicator is LoadingIndicator.Small

    val spacing = LocalSpacing.current
    var counterSec by remember { mutableIntStateOf(0) }
    var targetState by remember {
        mutableStateOf(
            if (animationsEnabled && isMessageShown(currentState)) {
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
            } else {
                val initialStateMessageShown = isMessageShown(this.initialState)
                val targetStateMessageShown = isMessageShown(this.targetState)
                if (!initialStateMessageShown && !targetStateMessageShown) {
                    // Message stays hidden
                    EnterTransition.None togetherWith ExitTransition.None
                } else if (targetStateMessageShown) {
                    // Showing message or changing shown message
                    slideInHorizontally { fullWidth -> -fullWidth } togetherWith fadeOut()
                } else {
                    // Hiding message
                    fadeIn() togetherWith fadeOut()
                }
            }
        }
    ) { targetState ->
        when {
            targetState is ActionWaiting && targetState.action is Automation.HasDelay ->
                ResultMessageRow {
                    ResultMessageText(Modifier.testTag("geoShareConversionSuccessAutomationCounter")) {
                        LaunchedEffect(targetState.action) {
                            counterSec = targetState.delay.toInt(DurationUnit.SECONDS)
                            while (counterSec > 0) {
                                delay(1.seconds)
                                counterSec--
                            }
                        }
                        targetState.action.waitingText(counterSec)
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

            targetState is ActionSucceeded && targetState.action is Action.HasSuccessMessage ->
                ResultMessageRow {
                    ResultMessageText(Modifier.testTag("geoShareConversionSuccessMessage")) {
                        targetState.action.successText()
                    }
                }

            targetState is ActionFailed && targetState.action is Action.HasErrorMessage ->
                ResultMessageRow {
                    ResultMessageText(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        targetState.action
                        targetState.action.errorText()
                    }
                }

            targetState is LocationFindingFailed ->
                ResultMessageRow {
                    ResultMessageText(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        stringResource(R.string.conversion_succeeded_location_failed)
                    }
                }

            loadingIndicator is LoadingIndicator.Small ->
                ResultMessageRow {
                    ResultMessageText(Modifier.testTag("geoShareConversionSuccessSmallLoadingIndicatorMessage")) {
                        stringResource(loadingIndicator.messageResId)
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

            else ->
                ResultMessageRow {
                    Text(
                        stringResource(R.string.conversion_succeeded_apps_headline),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Box(contentAlignment = Alignment.CenterEnd) {
                        Button(
                            onNavigateToUserPreferencesAutomationScreen,
                            Modifier
                                .testTag("geoShareConversionSuccessAutomationPreferencesButton")
                                .run {
                                    if (!automationFeatureValid) {
                                        this.padding(end = 17.dp)
                                    } else {
                                        this
                                    }
                                },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(stringResource(R.string.user_preferences_automation_title))
                        }
                        if (!automationFeatureValid) {
                            FilledIconButton(
                                onNavigateToUserPreferencesAutomationScreen,
                                Modifier.offset(y = (-13).dp),
                                shape = MaterialShapes.Sunny.toShape(),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            ) {
                                Icon(
                                    painterResource(R.drawable.crown_24px),
                                    stringResource(R.string.premium_feature)
                                )
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun ResultMessageRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.ResultMessageText(
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
private fun ActionFinishedPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionFinishedPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionFinishedFeatureNotValidPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = false,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionFinishedFeatureNotValidPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = false,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionWaitingPreview() {
    AppTheme {
        Surface {
            val userPreferencesRepository = FakeUserPreferencesRepository()
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        userPreferencesRepository = userPreferencesRepository,
                        features = Features(userPreferencesRepository),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                    delay = 3.seconds,
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionWaitingPreview() {
    AppTheme {
        Surface {
            val userPreferencesRepository = FakeUserPreferencesRepository()
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        userPreferencesRepository = userPreferencesRepository,
                        features = Features(userPreferencesRepository),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                    delay = 3.seconds,
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionReceivedPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GpxOutput.ShareGpxRouteWithAppAction(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = LoadingIndicator.Small(
                    messageResId = R.string.conversion_succeeded_location_loading_indicator_title,
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
private fun DarkLocationPermissionReceivedPreview() {
    AppTheme {
        Surface {
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GpxOutput.ShareGpxRouteWithAppAction(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = LoadingIndicator.Small(
                    messageResId = R.string.conversion_succeeded_location_loading_indicator_title,
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
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
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
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
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
            ResultSuccessMessage(
                currentState = ActionFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
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
            ResultSuccessMessage(
                currentState = ActionFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME),
                ),
                automationFeatureValid = true,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}
