package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BasicAutomation
import page.ooooo.geoshare.data.local.preferences.LinkAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.toOutput
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.ui.AutomationDetail
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.toAutomationDetail

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceAutomationListItem(
    index: Int,
    count: Int,
    automationDetail: StateFlow<AutomationDetail>,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val automationDetail by automationDetail.collectAsStateWithLifecycle()

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        trailingContent = if (AutomationFeature in billingFeatures && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased) {
            {
                FeatureBadgeSmall(onClick)
            }
        } else {
            null
        },
        supportingContent = {
            AutomationPreferenceValue(
                automationDetail = automationDetail,
                descriptionEnabled = false,
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_automation_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceAutomationControls(
    automationDetails: StateFlow<List<List<AutomationDetail>>>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    val automationDetails by automationDetails.collectAsStateWithLifecycle()

    UserPreferenceControls(
        titleResId = R.string.user_preferences_automation_title,
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        description = {
            stringResource(R.string.user_preferences_automation_description)
        },
        featureNotPurchased = AutomationFeature in billingFeatures && billingStatus !is BillingStatus.Loading && billingStatus !is BillingStatus.Purchased,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        val enabled = AutomationFeature in billingFeatures && billingStatus is BillingStatus.Purchased
        val selectedValue = if (enabled) {
            AutomationPreference.getValue(values)
        } else {
            AutomationPreference.default
        }
        userPreferenceOptionsControl(
            isSelected = { it.automation == selectedValue },
            onSelect = {
                onValueChange { preferences ->
                    AutomationPreference.setValue(preferences, it.automation)
                }
            },
            optionGroups = automationDetails,
            enabled = enabled,
            itemTestTag = { option ->
                try {
                    Json.encodeToString(option)
                } catch (_: IllegalArgumentException) {
                    null
                }
                    .let { serializedString -> "geoShareUserPreferenceAutomation_$serializedString" }
            },
        ) { value, modifier ->
            AutomationPreferenceValue(
                automationDetail = value,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AutomationPreferenceValue(
    automationDetail: AutomationDetail,
    modifier: Modifier = Modifier,
    descriptionEnabled: Boolean = true,
) {
    val label = automationDetail.automationLabel()
    val description = if (descriptionEnabled) {
        automationDetail.output.getAutomationDescription()
    } else {
        null
    }
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            IconFromDescriptor(automationDetail.icon ?: PlaceholderIconDescriptor, contentDescription = null)
        }
        if (description != null) {
            Column {
                Text(label)
                Text(
                    description(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            Text(label)
        }
    }
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
                val log = DefaultLog
                val appDetails = fakeAppDetails()
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    automationDetail = MutableStateFlow(
                        SavePointsGpxAutomation.let { automation ->
                            automation
                                .toOutput(coordinateConverter, log)
                                .toAutomationDetail(automation, appDetails)
                        }
                    ),
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val log = DefaultLog
                val appDetails = fakeAppDetails()
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    automationDetail = MutableStateFlow(
                        SavePointsGpxAutomation.let { automation ->
                            automation
                                .toOutput(coordinateConverter, log)
                                .toAutomationDetail(automation, appDetails)
                        }
                    ),
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    automationDetail = MutableStateFlow(NoopOutput.toAutomationDetail(NoopAutomation, emptyMap())),
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    automationDetail = MutableStateFlow(NoopOutput.toAutomationDetail(NoopAutomation, emptyMap())),
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    selected = false,
                    onClick = {},
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
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                    token = "test_purchased",
                ),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletNotPurchasedControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val appDetails = fakeAppDetails()
            UserPreferenceAutomationControls(
                automationDetails = MutableStateFlow(
                    AutomationPreference.getOptionGroups(
                        activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                        appDetails = appDetails,
                        hiddenApps = emptySet(),
                        links = defaultFakeLinks,
                    )
                        .map { group ->
                            group.map { automation ->
                                when (automation) {
                                    is BasicAutomation -> automation.toOutput(coordinateConverter, log)
                                    is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                                        automation.toOutput(coordinateConverter, link)
                                    }
                                }
                                    .toAutomationDetail(automation, appDetails)
                            }
                        }
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}
