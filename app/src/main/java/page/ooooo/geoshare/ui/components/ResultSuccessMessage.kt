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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.conversion.ActionFailed
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionSucceeded
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.LoadingIndicator
import page.ooooo.geoshare.lib.conversion.LocationFindingFailed
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private fun isMessageShown(state: ConversionState.HasResult?, loadingIndicator: LoadingIndicator?): Boolean =
    state is ActionWaiting && state.action is Automation.HasDelay ||
        state is ActionSucceeded && state.action is Action.HasSuccessMessage ||
        state is ActionFailed && state.action is Action.HasErrorMessage ||
        state is LocationFindingFailed ||
        loadingIndicator is LoadingIndicator.Small

@Composable
fun ResultSuccessMessage(
    currentState: ConversionState.HasResult,
    automationFeatureStatus: FeatureStatus,
    loadingIndicator: LoadingIndicator?,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var counterSec by remember { mutableIntStateOf(0) }
    var targetState by remember {
        mutableStateOf(
            if (animationsEnabled && isMessageShown(currentState, loadingIndicator)) {
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
            .padding(horizontal = spacing.windowPadding)
            .fillMaxWidth()
            .height(40.dp),
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val initialStateMessageShown = isMessageShown(this.initialState, loadingIndicator)
                val targetStateMessageShown = isMessageShown(this.targetState, loadingIndicator)
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
                    FeatureBadged(
                        enabled = automationFeatureStatus == FeatureStatus.NOT_AVAILABLE,
                        badge = { modifier ->
                            FeatureBadgeSmall(
                                onNavigateToUserPreferencesAutomationScreen,
                                modifier.testTag("geoShareResultAutomationBadge")
                            )
                        },
                    ) { modifier ->
                        Button(
                            onNavigateToUserPreferencesAutomationScreen,
                            modifier.testTag("geoShareResultAutomationButton"),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(stringResource(R.string.user_preferences_automation_title))
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
private fun ActionFinishedFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                ResultSuccessMessage(
                    currentState = ActionFinished(
                        inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        position = Position.example,
                        action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
                    loadingIndicator = null,
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
                Spacer(Modifier.height(15.dp))
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionFinishedFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                ResultSuccessMessage(
                    currentState = ActionFinished(
                        inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        position = Position.example,
                        action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
                    loadingIndicator = null,
                    animationsEnabled = false,
                    onCancel = {},
                    onNavigateToUserPreferencesAutomationScreen = {},
                )
                Spacer(Modifier.height(15.dp))
            }
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
                        billing = BillingImpl(LocalContext.current),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                    delay = 3.seconds,
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                        billing = BillingImpl(LocalContext.current),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    position = Position.example,
                    i = null,
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                    delay = 3.seconds,
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GpxOutput.ShareGpxRouteWithAppAction(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GpxOutput.ShareGpxRouteWithAppAction(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                    action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
                loadingIndicator = null,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}
