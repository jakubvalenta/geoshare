package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.OSMAND_PLUS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.conversion.ActionFailed
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionSucceeded
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.LocationFindingFailed
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private fun isMessageShown(state: ConversionState.HasResult?): Boolean =
    state is ActionWaiting && state.action.output is Output.HasAutomationDelay ||
        state is ActionSucceeded && !state.isAutomation && state.action.output is Output.HasSuccessText ||
        state is ActionFailed && !state.isAutomation && state.action.output is Output.HasErrorText ||
        state is ActionSucceeded && state.isAutomation && state.action.output is Output.HasAutomationSuccessText ||
        state is ActionFailed && state.isAutomation && state.action.output is Output.HasAutomationErrorText ||
        state is LocationFindingFailed ||
        state is ConversionState.HasSmallLoadingIndicator

@Composable
fun ResultSuccessMessage(
    currentState: ConversionState.HasResult,
    appDetails: AppDetails,
    automationFeatureStatus: FeatureStatus,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
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
            .padding(horizontal = spacing.windowPadding)
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
        when (targetState) {
            is ActionWaiting if targetState.action.output is Output.HasAutomationDelay ->
                (targetState.action.output as? Output.HasAutomationDelay)?.let { output ->
                    ResultMessageRow {
                        LaunchedEffect(targetState.action) {
                            counterSec = targetState.delay.toInt(DurationUnit.SECONDS)
                            while (counterSec > 0) {
                                delay(1.seconds)
                                counterSec--
                            }
                        }
                        ResultMessageText(
                            output.automationWaitingText(counterSec, appDetails),
                            Modifier.testTag("geoShareResultSuccessAutomationCounter"),
                        )
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

            is ActionSucceeded if !targetState.isAutomation && targetState.action.output is Output.HasSuccessText ->
                (targetState.action.output as? Output.HasSuccessText)?.let { output ->
                    ResultMessageRow {
                        ResultMessageText(
                            output.successText(appDetails),
                            Modifier.testTag("geoShareResultSuccessMessage"),
                        )
                    }
                }

            is ActionFailed if !targetState.isAutomation && targetState.action.output is Output.HasErrorText ->
                (targetState.action.output as? Output.HasErrorText)?.let { output ->
                    ResultMessageRow {
                        ResultMessageText(
                            output.errorText(appDetails),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

            is ActionSucceeded if targetState.isAutomation && targetState.action.output is Output.HasAutomationSuccessText ->
                (targetState.action.output as? Output.HasAutomationSuccessText)?.let { output ->
                    ResultMessageRow {
                        ResultMessageText(
                            output.automationSuccessText(appDetails),
                            Modifier.testTag("geoShareResultSuccessMessage"),
                        )
                    }
                }

            is ActionFailed if targetState.isAutomation && targetState.action.output is Output.HasAutomationErrorText ->
                (targetState.action.output as? Output.HasAutomationErrorText)?.let { output ->
                    ResultMessageRow {
                        ResultMessageText(
                            output.automationErrorText(appDetails),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

            is LocationFindingFailed -> ResultMessageRow {
                ResultMessageText(
                    stringResource(R.string.conversion_succeeded_location_failed),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            is ConversionState.HasSmallLoadingIndicator -> ResultMessageRow {
                ResultMessageText(
                    targetState.getSmallLoadingIndicator(LocalResources.current).message,
                    Modifier.testTag("geoShareResultSuccessSmallLoadingIndicatorMessage"),
                )
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

            else -> DefaultResultMessageRow(automationFeatureStatus, onNavigateToUserPreferencesAutomationScreen)
        }
    }
}

@Composable
private fun DefaultResultMessageRow(
    automationFeatureStatus: FeatureStatus,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
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
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
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
            text,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFinished(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessMessage(
                    currentState = ActionFinished(
                        inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        points = persistentListOf(Point.example),
                        action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                        isAutomation = true,
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
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
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessMessage(
                    currentState = ActionFinished(
                        inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        points = persistentListOf(Point.example),
                        action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                        isAutomation = true,
                    ),
                    appDetails = mapOf(
                        OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    automationFeatureStatus = FeatureStatus.NOT_AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                    delay = 3.seconds,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                    delay = 3.seconds,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SucceededNoMessagePreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = SharePointsGpxOutput.toAction(persistentListOf(Point.example)),
                    isAutomation = false,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSucceededNoMessagePreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = SharePointsGpxOutput.toAction(persistentListOf(Point.example)),
                    isAutomation = false,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFailed(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(Point.example),
                    action = OpenDisplayGeoUriOutput(OSMAND_PLUS_PACKAGE_NAME).toAction(Point.example),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    OSMAND_PLUS_PACKAGE_NAME to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                automationFeatureStatus = FeatureStatus.AVAILABLE,
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}
