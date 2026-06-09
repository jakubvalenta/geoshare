package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingImpl
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.conversion.ActionAutomationFailed
import page.ooooo.geoshare.lib.conversion.ActionAutomationSucceeded
import page.ooooo.geoshare.lib.conversion.ActionFailed
import page.ooooo.geoshare.lib.conversion.ActionFinished
import page.ooooo.geoshare.lib.conversion.ActionSucceeded
import page.ooooo.geoshare.lib.conversion.ActionWaiting
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.LocationFindingFailed
import page.ooooo.geoshare.lib.conversion.LocationPermissionReceived
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

// TODO Rename ResultSuccessMessage to ResultTitle
@Composable
fun ResultSuccessMessage(
    currentState: ConversionState.HasResult,
    appDetails: AppDetails,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesAutomationScreen: () -> Unit,
) {
    var counterSec by remember { mutableIntStateOf(0) }

    AnimatedMessage(
        state = currentState,
        isMessageShown = { state ->
            state is ActionWaiting ||
                state is ActionSucceeded ||
                state is ActionAutomationSucceeded ||
                state is ActionFailed ||
                state is ActionAutomationFailed ||
                state is LocationFindingFailed ||
                state is ConversionState.HasSmallLoadingIndicator
        },
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        animationsEnabled = animationsEnabled,
    ) { targetState ->
        when (targetState) {
            is ActionWaiting -> ResultMessageRow {
                LaunchedEffect(targetState.action) {
                    counterSec = targetState.delay.toInt(DurationUnit.SECONDS)
                    while (counterSec > 0) {
                        delay(1.seconds)
                        counterSec--
                    }
                }
                ResultMessageText(
                    targetState.output.automationWaitingText(counterSec, appDetails),
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

            is ActionSucceeded -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.successText(appDetails),
                    Modifier.testTag("geoShareResultSuccessMessage"),
                )
            }

            is ActionFailed -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.errorText(appDetails),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            is ActionAutomationSucceeded -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.automationSuccessText(appDetails),
                    Modifier.testTag("geoShareResultSuccessMessage"),
                )
            }

            is ActionAutomationFailed -> ResultMessageRow {
                ResultMessageText(
                    targetState.output.automationErrorText(appDetails),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
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
                    targetState.getLoadingIndicator().message,
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

            else -> ResultMessageRow {
                Text(
                    stringResource(R.string.conversion_succeeded_apps_headline),
                    style = MaterialTheme.typography.headlineSmall,
                )
                FeatureBadged(
                    enabled = AutomationFeature in billingFeatures && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased,
                    badge = { modifier ->
                        FeatureBadgeSmall(
                            onNavigateToUserPreferencesAutomationScreen,
                            modifier.testTag("geoShareAutomationFeatureBadge")
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
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    actionResult = ActionResult.Succeeded,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
private fun DarkActionFinishedPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFinished(
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    actionResult = ActionResult.Succeeded,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
private fun ActionFinishedFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                val context = LocalContext.current
                @SuppressLint("LocalContextGetResourceValueCall")
                ResultSuccessMessage(
                    currentState = ActionFinished(
                        source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        points = persistentListOf(WGS84Point(NaivePoint.example)),
                        actionResult = ActionResult.Succeeded,
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.NotPurchased(),
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
                        source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                        points = persistentListOf(WGS84Point(NaivePoint.example)),
                        actionResult = ActionResult.Succeeded,
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.NotPurchased(),
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
            val resources = LocalResources.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            val output = OpenDisplayGeoUriOutput(PackageNames.OSMAND_PLUS, coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        outputRepository = outputRepository,
                        resources = resources,
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    action = output.toAction(WGS84Point(NaivePoint.example)),
                    output = output,
                    isAutomation = true,
                    delay = 3.seconds,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
private fun DarkActionWaitingPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val resources = LocalResources.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            val output = OpenDisplayGeoUriOutput(PackageNames.OSMAND_PLUS, coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionWaiting(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        outputRepository = outputRepository,
                        resources = resources,
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    action = output.toAction(WGS84Point(NaivePoint.example)),
                    output = output,
                    isAutomation = true,
                    delay = 3.seconds,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
private fun LocationPermissionReceivedPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val resources = LocalResources.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        outputRepository = outputRepository,
                        resources = resources,
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter)
                        .toAction(WGS84Point(NaivePoint.example)),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
            val context = LocalContext.current
            val resources = LocalResources.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
            )
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = LocationPermissionReceived(
                    stateContext = ConversionStateContext(
                        linkRepository = FakeLinkRepository(),
                        outputRepository = outputRepository,
                        resources = resources,
                        userPreferencesRepository = FakeUserPreferencesRepository(),
                        billing = BillingImpl(LocalContext.current),
                    ),
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter)
                        .toAction(WGS84Point(NaivePoint.example)),
                    isAutomation = true,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    output = SavePointsGpxOutput(coordinateConverter),
                    actionResult = ActionResult.Succeeded,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionSucceeded(
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    output = SavePointsGpxOutput(coordinateConverter),
                    actionResult = ActionResult.Succeeded,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFailed(
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    output = SavePointsGpxOutput(coordinateConverter),
                    actionResult = ActionResult.Failed,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            @SuppressLint("LocalContextGetResourceValueCall")
            ResultSuccessMessage(
                currentState = ActionFailed(
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    points = persistentListOf(WGS84Point(NaivePoint.example)),
                    output = SavePointsGpxOutput(coordinateConverter),
                    actionResult = ActionResult.Failed,
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                billingStatus = BillingStatus.Purchased(
                    BillingProduct("test", BillingProduct.Type.DONATION),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
                animationsEnabled = false,
                onCancel = {},
                onNavigateToUserPreferencesAutomationScreen = {},
            )
        }
    }
}
