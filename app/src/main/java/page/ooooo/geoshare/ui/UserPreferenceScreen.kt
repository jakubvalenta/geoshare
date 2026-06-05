package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.components.LabelLarge
import page.ooooo.geoshare.ui.components.NavigableBasicListDetailScaffold
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.components.UserPreferenceAutomationControls
import page.ooooo.geoshare.ui.components.UserPreferenceAutomationDelayControls
import page.ooooo.geoshare.ui.components.UserPreferenceAutomationDelayListItem
import page.ooooo.geoshare.ui.components.UserPreferenceAutomationListItem
import page.ooooo.geoshare.ui.components.UserPreferenceConnectionPermissionControls
import page.ooooo.geoshare.ui.components.UserPreferenceConnectionPermissionListItem
import page.ooooo.geoshare.ui.components.UserPreferenceCoordinateFormatControls
import page.ooooo.geoshare.ui.components.UserPreferenceCoordinateFormatListItem
import page.ooooo.geoshare.ui.components.UserPreferenceDeveloperOptionsControls
import page.ooooo.geoshare.ui.components.UserPreferenceDeveloperOptionsListItem
import page.ooooo.geoshare.ui.components.UserPreferenceHiddenAppsControls
import page.ooooo.geoshare.ui.components.UserPreferenceHiddenAppsListItem
import page.ooooo.geoshare.ui.components.UserPreferenceLinksListItem
import page.ooooo.geoshare.ui.components.UserPreferenceServersListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import java.util.UUID

@Keep
enum class UserPreferenceGroupId {
    AUTOMATION,
    AUTOMATION_DELAY,
    CONNECTION_PERMISSION,
    COORDINATE_FORMAT,
    DEVELOPER_OPTIONS,
    HIDDEN_APPS,
    LINKS,
    SERVERS,
}

@Composable
fun UserPreferenceScreen(
    initialGroupId: UserPreferenceGroupId?,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToServerScreen: () -> Unit,
    billingViewModel: BillingViewModel,
    linkViewModel: LinkViewModel = hiltViewModel(),
    outputViewModel: OutputViewModel = hiltViewModel(),
    viewModel: UserPreferenceViewModel = hiltViewModel(),
) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val appDetails by viewModel.appDetails.collectAsStateWithLifecycle()
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val billingFeatures = billingViewModel.billingFeatures
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()
    val links by linkViewModel.all.collectAsStateWithLifecycle()
    val userPreferencesValues by viewModel.values.collectAsStateWithLifecycle()

    UserPreferenceScreen(
        initialGroupId = initialGroupId,
        apps = apps,
        appDetails = appDetails,
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingStatus = billingStatus,
        links = links,
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onGetAutomationOutput = { automation, getLinkByUUID ->
            outputViewModel.getAutomationOutput(automation, getLinkByUUID)
        },
        onNavigateToBillingScreen = onNavigateToBillingScreen,
        onNavigateToLinkScreen = onNavigateToLinkScreen,
        onNavigateToServerScreen = onNavigateToServerScreen,
        onValueChange = { transform: (preferences: MutablePreferences) -> Unit ->
            viewModel.editUserPreferences(transform)
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferenceScreen(
    initialGroupId: UserPreferenceGroupId?,
    apps: DataTypes,
    appDetails: AppDetails,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: List<Link>,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToServerScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (initialGroupId == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, initialGroupId)
            },
        ),
    )
    val currentGroupId = remember(navigator.currentDestination) {
        navigator.currentDestination?.contentKey
    }

    NavigableBasicListDetailScaffold(
        navigator = navigator,
        listPane = { _, containerColor ->
            UserPreferenceListPane(
                currentGroupId = currentGroupId,
                apps = apps,
                appDetails = appDetails,
                billingStatus = billingStatus,
                billingFeatures = billingFeatures,
                containerColor = containerColor,
                links = links,
                values = userPreferencesValues,
                onBack = {
                    coroutineScope.launch {
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        } else {
                            onBack()
                        }
                    }
                },
                onGetAutomationOutput = onGetAutomationOutput,
                onNavigateToGroup = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
                onNavigateToLinkScreen = onNavigateToLinkScreen,
                onNavigateToServerScreen = onNavigateToServerScreen,
            )
        },
        detailPane = { wide ->
            if (currentGroupId != null) {
                UserPreferenceDetailPane(
                    currentGroupId = currentGroupId,
                    apps = apps,
                    appDetails = appDetails,
                    billingAppNameResId = billingAppNameResId,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    links = links,
                    values = userPreferencesValues,
                    wide = wide,
                    onBack = {
                        coroutineScope.launch {
                            if (navigator.canNavigateBack()) {
                                navigator.navigateBack()
                            } else {
                                onBack()
                            }
                        }
                    },
                    onGetAutomationOutput = onGetAutomationOutput,
                    onNavigateToBillingScreen = onNavigateToBillingScreen,
                    onValueChange = onValueChange,
                )
            }
        },
        listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserPreferenceListPane(
    currentGroupId: UserPreferenceGroupId?,
    values: UserPreferencesValues,
    apps: DataTypes,
    appDetails: AppDetails,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    containerColor: Color,
    links: List<Link>,
    onBack: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
    onNavigateToGroup: (id: UserPreferenceGroupId) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToServerScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current

    ScrollablePane(
        title = {
            Text(stringResource(R.string.user_preferences_title))
        },
        onBack = onBack,
        modifier = Modifier
            .padding(horizontal = spacing.windowPadding)
            .testTag("geoShareUserPreferencesListPane"),
        containerColor = containerColor,
    ) {
        item {
            LabelLarge(
                stringResource(R.string.user_preferences_section_input),
                Modifier.padding(top = spacing.tiny, bottom = spacing.tiny),
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                UserPreferenceConnectionPermissionListItem(
                    index = 0,
                    count = 2,
                    selected = currentGroupId == UserPreferenceGroupId.CONNECTION_PERMISSION,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.CONNECTION_PERMISSION}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.CONNECTION_PERMISSION) },
                )
                UserPreferenceServersListItem(
                    index = 1,
                    count = 2,
                    selected = currentGroupId == UserPreferenceGroupId.SERVERS,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.SERVERS}"),
                    onClick = onNavigateToServerScreen,
                )
            }
        }
        item {
            SegmentedListLabel(stringResource(R.string.user_preferences_automation_title))
        }
        item {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                UserPreferenceAutomationListItem(
                    index = 0,
                    count = 2,
                    appDetails = appDetails,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    links = links,
                    selected = currentGroupId == UserPreferenceGroupId.AUTOMATION,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.AUTOMATION}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.AUTOMATION) },
                    onGetAutomationOutput = onGetAutomationOutput,
                )
                UserPreferenceAutomationDelayListItem(
                    index = 1,
                    count = 2,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    links = links,
                    selected = currentGroupId == UserPreferenceGroupId.AUTOMATION_DELAY,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.AUTOMATION_DELAY}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.AUTOMATION_DELAY) },
                    onGetAutomationOutput = onGetAutomationOutput,
                )
            }
        }
        item {
            SegmentedListLabel(stringResource(R.string.user_preferences_section_output))
        }
        item {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                UserPreferenceHiddenAppsListItem(
                    index = 0,
                    count = 3,
                    apps = apps,
                    selected = currentGroupId == UserPreferenceGroupId.HIDDEN_APPS,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.HIDDEN_APPS}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.HIDDEN_APPS) },
                )
                UserPreferenceLinksListItem(
                    index = 1,
                    count = 3,
                    links = links,
                    selected = currentGroupId == UserPreferenceGroupId.LINKS,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.LINKS}"),
                    onClick = onNavigateToLinkScreen,
                )
                UserPreferenceCoordinateFormatListItem(
                    index = 1,
                    count = 2,
                    selected = currentGroupId == UserPreferenceGroupId.COORDINATE_FORMAT,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.COORDINATE_FORMAT}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.COORDINATE_FORMAT) },
                )
            }
        }
        if (BuildConfig.DEBUG) {
            item {
                SegmentedListLabel(stringResource(R.string.user_preferences_developer_title))
            }
            item {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    UserPreferenceDeveloperOptionsListItem(
                        index = 0,
                        count = 1,
                        selected = currentGroupId == UserPreferenceGroupId.DEVELOPER_OPTIONS,
                        modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.DEVELOPER_OPTIONS}"),
                        onClick = { onNavigateToGroup(UserPreferenceGroupId.DEVELOPER_OPTIONS) },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserPreferenceDetailPane(
    currentGroupId: UserPreferenceGroupId,
    apps: DataTypes,
    appDetails: AppDetails,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: List<Link>,
    values: UserPreferencesValues,
    wide: Boolean,
    onBack: () -> Unit,
    onGetAutomationOutput: suspend (automation: Automation, getLinkByUUID: suspend (linkUUID: UUID) -> Link?) -> Output?,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    when (currentGroupId) {
        UserPreferenceGroupId.AUTOMATION -> UserPreferenceAutomationControls(
            appDetails = appDetails,
            apps = apps,
            billingAppNameResId = billingAppNameResId,
            billingFeatures = billingFeatures,
            billingStatus = billingStatus,
            links = links,
            onBack = onBack,
            onGetAutomationOutput = onGetAutomationOutput,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.AUTOMATION_DELAY -> UserPreferenceAutomationDelayControls(
            billingAppNameResId = billingAppNameResId,
            billingFeatures = billingFeatures,
            billingStatus = billingStatus,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.CONNECTION_PERMISSION -> UserPreferenceConnectionPermissionControls(
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.COORDINATE_FORMAT -> UserPreferenceCoordinateFormatControls(
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.DEVELOPER_OPTIONS -> UserPreferenceDeveloperOptionsControls(
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.HIDDEN_APPS -> UserPreferenceHiddenAppsControls(
            appDetails = appDetails,
            apps = apps,
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.LINKS -> {}

        UserPreferenceGroupId.SERVERS -> {}
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferenceScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading(),
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences.copy(
                        connectionPermission = Permission.NEVER,
                    ),
                    onBack = {},
                    onGetAutomationOutput = { _, _ -> null },
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferenceScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading(),
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences.copy(
                        connectionPermission = Permission.NEVER,
                    ),
                    onBack = {},
                    onGetAutomationOutput = { _, _ -> null },
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferenceScreen(
                    initialGroupId = null,
                    apps = emptyMap(),
                    appDetails = emptyMap(),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading(),
                    links = defaultFakeLinks,
                    userPreferencesValues = defaultFakeUserPreferences.copy(
                        connectionPermission = Permission.NEVER,
                    ),
                    onBack = {},
                    onGetAutomationOutput = { _, _ -> null },
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}
