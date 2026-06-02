package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import java.util.UUID

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceAutomationListItem(
    index: Int,
    count: Int,
    appDetails: AppDetails,
    links: List<Link>,
    values: UserPreferencesValues,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
) {
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
                value = AutomationPreference.getValue(values),
                appDetails = appDetails,
                links = links,
                descriptionEnabled = false,
                onGetAutomationOutput = onGetAutomationOutput,
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
    appDetails: AppDetails,
    apps: DataTypes,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: List<Link>,
    onBack: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
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
        userPreferenceOptionsControl(
            userPreference = AutomationPreference,
            optionGroups = AutomationPreference.getOptionGroups(
                apps,
                appDetails,
                values.hiddenApps,
                links
            ),
            values = values,
            enabled = AutomationFeature in billingFeatures && billingStatus is BillingStatus.Purchased,
            itemTestTag = { option ->
                try {
                    Json.encodeToString(option)
                } catch (_: IllegalArgumentException) {
                    null
                }
                    .let { serializedString -> "geoShareUserPreferenceAutomation_$serializedString" }
            },
            onValueChange = onValueChange,
        ) { value ->
            AutomationPreferenceValue(
                value = value,
                appDetails = appDetails,
                links = links,
                onGetAutomationOutput = onGetAutomationOutput,
            )
        }
    }
}

@Composable
private fun AutomationPreferenceValue(
    value: Automation,
    appDetails: AppDetails,
    links: List<Link>,
    descriptionEnabled: Boolean = true,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
) {
    var output by retain { mutableStateOf<Output?>(null) }
    LaunchedEffect(value, links) {
        output = onGetAutomationOutput(value) { links.findByUUID(it) }
    }
    output?.let { output ->
        val label = output.automationLabel(appDetails)
        val description = output.takeIf { descriptionEnabled }?.getAutomationDescription()
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            output.getIcon(appDetails)?.let {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    IconFromDescriptor(it, contentDescription = null)
                }
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
                val outputRepository = OutputRepository(coordinateConverter)
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    links = emptyList(),
                    selected = false,
                    values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                    onClick = {},
                    onGetAutomationOutput = { automation, getLinkByUUID ->
                        outputRepository.getAutomationOutput(automation, getLinkByUUID)
                    },
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
                val outputRepository = OutputRepository(coordinateConverter)
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    links = emptyList(),
                    selected = false,
                    values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                    onClick = {},
                    onGetAutomationOutput = { automation, getLinkByUUID ->
                        outputRepository.getAutomationOutput(automation, getLinkByUUID)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun NoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(coordinateConverter)
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    links = emptyList(),
                    selected = false,
                    values = defaultFakeUserPreferences,
                    onClick = {},
                    onGetAutomationOutput = { automation, getLinkByUUID ->
                        outputRepository.getAutomationOutput(automation, getLinkByUUID)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(coordinateConverter)
                @SuppressLint("LocalContextGetResourceValueCall")
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 1,
                    billingFeatures = listOf(AutomationFeature),
                    billingStatus = BillingStatus.Purchased(
                        product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                        expired = false,
                        refundable = true,
                        token = "test_purchased",
                    ),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            "OsmAnd",
                            context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    links = emptyList(),
                    selected = false,
                    values = defaultFakeUserPreferences,
                    onClick = {},
                    onGetAutomationOutput = { automation, getLinkByUUID ->
                        outputRepository.getAutomationOutput(automation, getLinkByUUID)
                    },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
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
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
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
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
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
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
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
            val outputRepository = OutputRepository(coordinateConverter)
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceAutomationControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.OSMAND_PLUS to setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI),
                ),
                appDetails = mapOf(
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                links = defaultFakeLinks,
                values = UserPreferencesValues(automation = SavePointsGpxAutomation),
                wide = true,
                billingFeatures = listOf(AutomationFeature),
                billingStatus = BillingStatus.NotPurchased(),
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                onGetAutomationOutput = { automation, getLinkByUUID ->
                    outputRepository.getAutomationOutput(automation, getLinkByUUID)
                },
            )
        }
    }
}
