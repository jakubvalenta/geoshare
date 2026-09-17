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
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.ui.ActionAutomationFailedDetail
import page.ooooo.geoshare.ui.ActionAutomationSucceededDetail
import page.ooooo.geoshare.ui.ActionDetail
import page.ooooo.geoshare.ui.ActionFailedDetail
import page.ooooo.geoshare.ui.ActionSucceededDetail
import page.ooooo.geoshare.ui.ActionWaitingDetail
import page.ooooo.geoshare.ui.LocationFindingFailedDetail
import page.ooooo.geoshare.ui.LocationPermissionReceivedDetail
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun ResultTitle(
    actionDetail: StateFlow<ActionDetail?>,
    billingFeatures: List<Feature>,
    billingStatus: StateFlow<BillingStatus>,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    onCancel: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId?) -> Unit,
) {
    val actionDetail by actionDetail.collectAsStateWithLifecycle()
    var counter by retain { mutableStateOf(Duration.ZERO) }

    val billingStatus by billingStatus.collectAsStateWithLifecycle()

    AnimatedMessage(
        state = actionDetail,
        isMessageShown = { state -> state != null },
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        animationsEnabled = animationsEnabled,
    ) { targetState ->
        when (targetState) {
            is ActionWaitingDetail -> ResultMessageRow {
                LaunchedEffect(Unit) {
                    counter = targetState.delay
                    while (counter.isPositive()) {
                        delay(1.seconds)
                        counter -= 1.seconds
                    }
                }
                ResultMessageText(
                    targetState.automationWaitingText(counter.toInt(DurationUnit.SECONDS)),
                    Modifier.testTag("geoShareResultAutomationCounter"),
                )
                FilledIconButton(
                    onCancel,
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

            is ActionSucceededDetail -> ResultMessageRow {
                ResultMessageText(
                    targetState.successText(),
                    Modifier.testTag("geoShareResultMessageSuccess"),
                )
            }

            is ActionFailedDetail -> ResultMessageRow {
                ResultMessageText(
                    targetState.errorText(),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is ActionAutomationSucceededDetail -> ResultMessageRow {
                ResultMessageText(
                    targetState.automationSuccessText(),
                    Modifier.testTag("geoShareResultMessageSuccess"),
                )
            }

            is ActionAutomationFailedDetail -> ResultMessageRow {
                ResultMessageText(
                    targetState.automationErrorText(),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is LocationFindingFailedDetail -> ResultMessageRow {
                ResultMessageText(
                    stringResource(R.string.conversion_succeeded_location_failed),
                    Modifier.testTag("geoShareResultMessageError"),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }

            is LocationPermissionReceivedDetail -> ResultMessageRow {
                ResultMessageText(
                    stringResource(R.string.conversion_succeeded_location_loading_indicator_title),
                    Modifier.testTag("geoShareResultSmallLoadingIndicatorMessage"),
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

            null -> ResultMessageRow {
                Text(
                    stringResource(R.string.conversion_succeeded_apps_headline),
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
private fun ActionCompletedPreview() {
    AppTheme {
        Surface {
            ResultTitle(
                actionDetail = MutableStateFlow(null),
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
                onCancel = {},
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
                actionDetail = MutableStateFlow(null),
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
                onCancel = {},
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
                    actionDetail = MutableStateFlow(null),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
                    animationsEnabled = false,
                    onCancel = {},
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
                    actionDetail = MutableStateFlow(null),
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = MutableStateFlow(BillingStatus.NotPurchased()),
                    animationsEnabled = false,
                    onCancel = {},
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
            val output = OpenDisplayGeoUriOutput(
                UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
                coordinateConverter,
            )
            val appDetails = getFakeAppDetails(context)
            ResultTitle(
                actionDetail = MutableStateFlow(
                    ActionWaitingDetail(
                        appDetail = output.getAppDetail(appDetails),
                        delay = 3.seconds,
                        output = output,
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
                onCancel = {},
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
            val output = OpenDisplayGeoUriOutput(
                UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
                coordinateConverter,
            )
            val appDetails = getFakeAppDetails(context)
            ResultTitle(
                actionDetail = MutableStateFlow(
                    ActionWaitingDetail(
                        appDetail = output.getAppDetail(appDetails),
                        delay = 3.seconds,
                        output = output,
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
                onCancel = {},
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
            ResultTitle(
                actionDetail = MutableStateFlow(LocationPermissionReceivedDetail),
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
                onCancel = {},
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
            ResultTitle(
                actionDetail = MutableStateFlow(LocationPermissionReceivedDetail),
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
                onCancel = {},
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
                actionDetail = MutableStateFlow(
                    ActionSucceededDetail(
                        appDetail = null,
                        output = output,
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
                onCancel = {},
                onNavigateToUserPreferencesScreen = {},
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
            val output = SavePointsGpxOutput(coordinateConverter)
            ResultTitle(
                actionDetail = MutableStateFlow(
                    ActionSucceededDetail(
                        appDetail = null,
                        output = output,
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
                onCancel = {},
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
                actionDetail = MutableStateFlow(
                    ActionFailedDetail(
                        appDetail = null,
                        output = output,
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
                onCancel = {},
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
                actionDetail = MutableStateFlow(
                    ActionFailedDetail(
                        appDetail = null,
                        output = output,
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
                onCancel = {},
                onNavigateToUserPreferencesScreen = {},
            )
        }
    }
}
