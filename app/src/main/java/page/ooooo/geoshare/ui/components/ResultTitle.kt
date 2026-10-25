package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
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
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.FileType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.billing.AutomationFeature
import page.ooooo.geoshare.data.billing.BillingProduct
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.billing.CustomLinkFeature
import page.ooooo.geoshare.data.billing.Feature
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.data.state.ActionAutomationFailed
import page.ooooo.geoshare.data.state.ActionAutomationSucceeded
import page.ooooo.geoshare.data.state.ActionCompleted
import page.ooooo.geoshare.data.state.ActionFailed
import page.ooooo.geoshare.data.state.ActionState
import page.ooooo.geoshare.data.state.ActionSucceeded
import page.ooooo.geoshare.data.state.ActionWaiting
import page.ooooo.geoshare.data.state.LocationFindingFailed
import page.ooooo.geoshare.data.state.LocationPermissionReceived
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun ResultTitle(
    appDetails: StateFlow<AppDetails>,
    actionState: StateFlow<ActionState>,
    billingFeatures: List<Feature>,
    billingStatus: StateFlow<BillingStatus>,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    onCancelAction: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId?) -> Unit,
) {
    var counter by retain { mutableStateOf(Duration.ZERO) }

    val appDetails by appDetails.collectAsStateWithLifecycle()
    val actionState by actionState.collectAsStateWithLifecycle()
    val billingStatus by billingStatus.collectAsStateWithLifecycle()

    // TODO Don't animate when state is completed
    AnimatedMessage(
        state = actionState,
        isMessageShown = { state -> state != null },
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        animationsEnabled = animationsEnabled,
    ) { targetState ->
        when (targetState) {
            is ActionWaiting -> ResultMessageRow {
                LaunchedEffect(Unit) {
                    counter = targetState.delay
                    while (counter.isPositive()) {
                        delay(1.seconds)
                        counter -= 1.seconds
                    }
                }
                ResultMessageText(
                    targetState.output.automationWaitingText(counter.toInt(DurationUnit.SECONDS), appDetails),
                    Modifier.testTag("geoShareResultAutomationCounter"),
                )
                FilledIconButton(
                    onCancelAction,
                    Modifier.testTag("geoShareResultAutomationCancel"),
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

            is ActionSucceeded -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.successText(appDetails),
                    Modifier.testTag("geoShareResultMessageSuccess"),
                )
            }

            is ActionFailed -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.errorText(appDetails),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is ActionAutomationSucceeded -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.automationSuccessText(appDetails),
                    Modifier.testTag("geoShareResultMessageSuccess"),
                )
            }

            is ActionAutomationFailed -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.automationErrorText(appDetails),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is LocationFindingFailed -> ResultMessageRow {
                ResultMessageText(
                    stringResource(R.string.conversion_succeeded_location_failed),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is LocationPermissionReceived -> ResultMessageRow {
                ResultMessageText(
                    stringResource(R.string.conversion_succeeded_location_loading_indicator_title),
                    Modifier.testTag("geoShareResultSmallLoadingIndicatorMessage"),
                )
                FilledIconButton(
                    onCancelAction,
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

            else -> ResultMessageRow {
                Text(
                    stringResource(R.string.open_with),
                    style = MaterialTheme.typography.headlineSmall,
                )
                FeatureBadged(
                    enabled = AutomationFeature in billingFeatures && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased,
                    badge = { modifier ->
                        FeatureBadgeSmall(
                            { onNavigateToUserPreferencesScreen(UserPreferenceGroupId.AUTOMATION) },
                            modifier.testTag("geoShareAutomationFeatureBadge")
                        )
                    },
                ) { modifier ->
                    Button(
                        { onNavigateToUserPreferencesScreen(UserPreferenceGroupId.AUTOMATION) },
                        modifier.testTag("geoShareResultAutomationButton"),
                        colors = ButtonDefaults.buttonColors(
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
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    val spacing = LocalSpacing.current

    Row(
        modifier
            .weight(1f)
            .background(containerColor, MaterialTheme.shapes.extraSmall)
            .fillMaxHeight()
            .padding(horizontal = spacing.small), verticalAlignment = Alignment.CenterVertically
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
private fun ActionCompletedPreview() {
    AppTheme {
        Surface {
            ResultTitle(
                appDetails = MutableStateFlow(emptyMap()),
                actionState = MutableStateFlow(ActionCompleted(actionResult = ActionResult.SUCCEEDED)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionCompletedPreview() {
    AppTheme {
        Surface {
            ResultTitle(
                appDetails = MutableStateFlow(emptyMap()),
                actionState = MutableStateFlow(ActionCompleted(actionResult = ActionResult.SUCCEEDED)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionCompletedFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                ResultTitle(
                    appDetails = MutableStateFlow(emptyMap()),
                    actionState = MutableStateFlow(ActionCompleted(actionResult = ActionResult.SUCCEEDED)),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
                    animationsEnabled = false,
                    onCancelAction = {},
                    onNavigateToUserPreferencesScreen = {},
                )
                Spacer(Modifier.height(15.dp))
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkActionCompletedFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                ResultTitle(
                    appDetails = MutableStateFlow(emptyMap()),
                    actionState = MutableStateFlow(ActionCompleted(actionResult = ActionResult.SUCCEEDED)),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = MutableStateFlow(BillingStatus.NotPurchased),
                    animationsEnabled = false,
                    onCancelAction = {},
                    onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val uriQuote = DefaultUriQuote
            val output = OpenDisplayGeoUriOutput(
                UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
                coordinateConverter,
                uriQuote,
            )
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(
                    ActionWaiting(
                        action = output.toAction(WGS84Point(NaivePoint.example)),
                        output = output,
                        isAutomation = true,
                        delay = 3.seconds,
                    )
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val uriQuote = DefaultUriQuote
            val output = OpenDisplayGeoUriOutput(
                UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
                coordinateConverter,
                uriQuote,
            )
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(
                    ActionWaiting(
                        action = output.toAction(WGS84Point(NaivePoint.example)),
                        output = output,
                        isAutomation = true,
                        delay = 3.seconds,
                    )
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val output = OpenRouteOnePointGpxOutput(
                FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                coordinateConverter,
                log,
            )
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(
                    LocationPermissionReceived(
                        action = output.toAction(WGS84Point(NaivePoint.example)),
                        isAutomation = false,
                    )
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val output = OpenRouteOnePointGpxOutput(
                FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                coordinateConverter,
                log,
            )
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(
                    LocationPermissionReceived(
                        action = output.toAction(WGS84Point(NaivePoint.example)),
                        isAutomation = false,
                    )
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = SavePointsGpxOutput(coordinateConverter)
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(ActionSucceeded(actionResult = ActionResult.SUCCEEDED, output = output)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSucceededPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = SavePointsGpxOutput(coordinateConverter)
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(ActionSucceeded(actionResult = ActionResult.SUCCEEDED, output = output)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = SavePointsGpxOutput(coordinateConverter)
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(ActionFailed(output = output)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
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
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val output = SavePointsGpxOutput(coordinateConverter)
            ResultTitle(
                appDetails = MutableStateFlow(getFakeAppDetails(context)),
                actionState = MutableStateFlow(ActionFailed(output = output)),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = MutableStateFlow(
                    BillingStatus.Purchased(
                        BillingProduct("test", BillingProduct.Type.DONATION),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    )
                ),
                animationsEnabled = false,
                onCancelAction = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}
