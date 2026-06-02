package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.ui.theme.AppTheme
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceAutomationDelayListItem(
    index: Int,
    count: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: List<Link>,
    selected: Boolean,
    values: UserPreferencesValues,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
) {
    var enabled by retain { mutableStateOf(true) }

    LaunchedEffect(values, billingStatus, links) {
        enabled = billingStatus is BillingStatus.Purchased &&
            AutomationFeature in billingFeatures &&
            onGetAutomationOutput(values.automation) { links.findByUUID(it) } is Output.HasAutomationDelay
    }

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        enabled = enabled,
        supportingContent = {
            AutomationDelayPreferenceValue(
                value = AutomationDelayPreference.getValue(values),
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_automation_delay_sec_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceAutomationDelayControls(
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_automation_delay_sec_title,
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        description = {
            stringResource(R.string.user_preferences_automation_delay_sec_description)
        },
        featureNotPurchased = AutomationFeature in billingFeatures && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceTextControl(
            userPreference = AutomationDelayPreference,
            values = values,
            onValueChange = onValueChange,
            error = {
                stringResource(
                    R.string.user_preferences_number_error_range,
                    AutomationDelayPreference.minSec,
                    AutomationDelayPreference.maxSec,
                )
            },
            suffix = {
                stringResource(R.string.seconds_unit)
            },
        )
    }
}

@Composable
private fun AutomationDelayPreferenceValue(value: Duration) {
    val seconds = value.toInt(DurationUnit.SECONDS)
    Text(pluralStringResource(R.plurals.seconds, seconds, seconds))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                UserPreferenceAutomationDelayListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    links = emptyList(),
                    selected = false,
                    values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                    onClick = {},
                    onGetAutomationOutput = { _, _ -> SavePointsGpxOutput(coordinateConverter) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                UserPreferenceAutomationDelayListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    links = emptyList(),
                    selected = false,
                    values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                    onClick = {},
                    onGetAutomationOutput = { _, _ -> SavePointsGpxOutput(coordinateConverter) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(
                    automation = SavePointsGpxAutomation,
                    hiddenApps = emptySet(),
                ),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(
                    automation = SavePointsGpxAutomation,
                    hiddenApps = emptySet(),
                ),
                wide = false,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(
                    automation = SavePointsGpxAutomation,
                    hiddenApps = emptySet(),
                ),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletNotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceAutomationDelayControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = UserPreferencesValues(
                    automation = SavePointsGpxAutomation,
                    hiddenApps = emptySet(),
                ),
                wide = false,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
            )
        }
    }
}
