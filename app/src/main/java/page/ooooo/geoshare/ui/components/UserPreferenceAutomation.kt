package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.billing.AutomationFeature
import page.ooooo.geoshare.data.billing.BillingProduct
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.billing.Feature
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.NoopOutput
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.preferences.AutomationPreference
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.UserPreferencesValues
import page.ooooo.geoshare.data.toOutput
import page.ooooo.geoshare.ui.AutomationOption
import page.ooooo.geoshare.ui.AutomationOptionsState
import page.ooooo.geoshare.ui.SelectedAutomationOptionState
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.toOutput

@Composable
fun UserPreferenceAutomationListItem(
    state: StateFlow<SelectedAutomationOptionState>,
    index: Int,
    count: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val state by state.collectAsStateWithLifecycle()

    val notPurchased = AutomationFeature in billingFeatures &&
        billingStatus !is BillingStatus.Loading &&
        billingStatus !is BillingStatus.Purchased

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        trailingContent = if (notPurchased) {
            {
                FeatureBadgeSmall(onClick)
            }
        } else {
            null
        },
        supportingContent = {
            AutomationPreferenceValue(
                appDetails = state.appDetails,
                output = state.option.output,
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
    state: StateFlow<AutomationOptionsState>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    val state by state.collectAsStateWithLifecycle()

    val notPurchased = AutomationFeature in billingFeatures &&
        billingStatus !is BillingStatus.Loading &&
        billingStatus !is BillingStatus.Purchased

    UserPreferenceControls(
        titleResId = R.string.user_preferences_automation_title,
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        description = {
            stringResource(R.string.user_preferences_automation_description)
        },
        featureNotPurchased = notPurchased,
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
            key = "automation_control",
            isSelected = { option -> option.automation == selectedValue },
            onSelect = { option ->
                onValueChange { preferences ->
                    AutomationPreference.setValue(preferences, option.automation)
                }
            },
            optionGroups = state.optionGroups,
            enabled = enabled,
            itemTestTag = { option ->
                val serializedString = try {
                    Json.encodeToString(option.automation)
                } catch (_: IllegalArgumentException) {
                    null
                }
                "geoShareUserPreferenceAutomation_$serializedString"
            },
        ) { option, modifier ->
            AutomationPreferenceValue(
                appDetails = state.appDetails,
                output = option.output,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AutomationPreferenceValue(
    appDetails: AppDetails,
    output: Output<*>,
    modifier: Modifier = Modifier,
    descriptionEnabled: Boolean = true,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = output.getIcon(appDetails)
        if (icon != null) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                IconFromDescriptor(icon, contentDescription = null)
            }
        }
        val label = output.automationLabel(appDetails)
        val description = if (descriptionEnabled) output.automationDescription() else null
        if (description != null) {
            Column {
                Text(label)
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            Text(label)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                UserPreferenceAutomationListItem(
                    state = MutableStateFlow(
                        SelectedAutomationOptionState(
                            appDetails = getFakeAppDetails(context),
                            option = AutomationOption(
                                automation = SavePointsGpxAutomation,
                                output = SavePointsGpxAutomation.toOutput(coordinateConverter),
                            ),
                        ),
                    ),
                    index = 0,
                    count = 1,
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
                UserPreferenceAutomationListItem(
                    state = MutableStateFlow(
                        SelectedAutomationOptionState(
                            appDetails = getFakeAppDetails(context),
                            option = AutomationOption(
                                automation = SavePointsGpxAutomation,
                                output = SavePointsGpxAutomation.toOutput(coordinateConverter),
                            ),
                        ),
                    ),
                    index = 0,
                    count = 1,
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
                    state = MutableStateFlow(
                        SelectedAutomationOptionState(
                            appDetails = emptyMap(),
                            option = AutomationOption(NoopAutomation, NoopOutput),
                        ),
                    ),
                    index = 0,
                    count = 1,
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
                    state = MutableStateFlow(
                        SelectedAutomationOptionState(
                            appDetails = emptyMap(),
                            option = AutomationOption(NoopAutomation, NoopOutput),
                        ),
                    ),
                    index = 0,
                    count = 1,
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

@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun ControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
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

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
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
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
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
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased,
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
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased,
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
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = getFakeAppDetails(context),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun LoadingControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val links = defaultFakeLinks
            val log = DefaultLog
            UserPreferenceAutomationControls(
                state = MutableStateFlow(
                    AutomationOptionsState(
                        appDetails = emptyMap(),
                        optionGroups = AutomationPreference.getOptionGroups(
                            activities = fakeActivities.filter { it.packageName == PackageNames.OSMAND_PLUS },
                            hiddenApps = emptySet(),
                            links = links,
                        )
                            .map { group ->
                                group.map { automation ->
                                    AutomationOption(automation, automation.toOutput(coordinateConverter, links, log))
                                }
                            }
                    ),
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
