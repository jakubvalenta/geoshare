package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.billing.AutomationFeature
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.billing.CustomLinkFeature
import page.ooooo.geoshare.data.billing.Feature
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.database.Server
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.preferences.DynamicColorPreference
import page.ooooo.geoshare.data.preferences.Permission
import page.ooooo.geoshare.data.preferences.UserPreferenceValues
import page.ooooo.geoshare.ui.components.LargeTopAppBarPane
import page.ooooo.geoshare.ui.components.NavigableStyledListDetailPaneScaffold
import page.ooooo.geoshare.ui.components.SegmentedListLabel
import page.ooooo.geoshare.ui.components.StyledPaneScaffoldDefaults
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
import page.ooooo.geoshare.ui.components.UserPreferenceDynamicColorControls
import page.ooooo.geoshare.ui.components.UserPreferenceDynamicColorListItem
import page.ooooo.geoshare.ui.components.UserPreferenceFinishControls
import page.ooooo.geoshare.ui.components.UserPreferenceFinishListItem
import page.ooooo.geoshare.ui.components.UserPreferenceHiddenAppsControls
import page.ooooo.geoshare.ui.components.UserPreferenceHiddenAppsListItem
import page.ooooo.geoshare.ui.components.UserPreferenceLinkListItem
import page.ooooo.geoshare.ui.components.UserPreferenceServerControls
import page.ooooo.geoshare.ui.components.UserPreferenceServerListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Keep
enum class UserPreferenceGroupId {
    APPEARANCE_DYNAMIC_COLOR,
    AUTOMATION,
    AUTOMATION_DELAY,
    CONNECTION_PERMISSION,
    COORDINATE_FORMAT,
    DEVELOPER_OPTIONS,
    FINISH,
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
    onNavigateToServerScreen: (uid: Int?) -> Unit,
    billingViewModel: BillingViewModel,
    linkViewModel: LinkViewModel = hiltViewModel(),
    serverViewModel: ServerViewModel = hiltViewModel(),
    viewModel: UserPreferenceViewModel = hiltViewModel(),
) {
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val billingFeatures = billingViewModel.billingFeatures
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()

    UserPreferenceScreen(
        initialGroupId = initialGroupId,
        appDetails = viewModel.appDetails,
        automationOptions = viewModel.automationOptions,
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingStatus = billingStatus,
        hiddenAppsOptions = viewModel.hiddenAppsOptions,
        links = linkViewModel.all,
        selectedAutomationOption = viewModel.selectedAutomationOption,
        selectedHiddenApps = viewModel.selectedHiddenApps,
        selectedServers = serverViewModel.selectedServers,
        servers = serverViewModel.all,
        values = viewModel.values,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
        onNavigateToLinkScreen = onNavigateToLinkScreen,
        onNavigateToServerScreen = onNavigateToServerScreen,
        onSelectServerGoogleMapsAddress = serverViewModel::selectServerGoogleMapsAddress,
        onSelectServerGoogleMapsPlace = serverViewModel::selectServerGoogleMapsPlace,
        onSelectServerSearch = serverViewModel::selectServerSearch,
        onValueChange = { transform: (preferences: MutablePreferences) -> Unit ->
            viewModel.editUserPreferences(transform)
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun UserPreferenceScreen(
    initialGroupId: UserPreferenceGroupId?,
    appDetails: StateFlow<AppDetails>,
    automationOptions: StateFlow<AutomationOptionsState>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    hiddenAppsOptions: StateFlow<HiddenAppsOptionsState>,
    links: StateFlow<List<Link>>,
    selectedAutomationOption: StateFlow<SelectedAutomationOptionState>,
    selectedHiddenApps: StateFlow<SelectedHiddenAppsState>,
    selectedServers: StateFlow<SelectedServers>,
    servers: StateFlow<List<Server>>,
    values: StateFlow<UserPreferenceValues>,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToLinkScreen: () -> Unit,
    onNavigateToServerScreen: (uid: Int?) -> Unit,
    onSelectServerGoogleMapsAddress: (uid: Int?) -> Unit,
    onSelectServerGoogleMapsPlace: (uid: Int?) -> Unit,
    onSelectServerSearch: (uid: Int?) -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val defaultDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    val navigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = defaultDirective.copy(
            horizontalPartitionSpacerSize = LocalSpacing.current.windowPadding,
        ),
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

    NavigableStyledListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            UserPreferenceListPane(
                currentGroupId = currentGroupId,
                appDetails = appDetails,
                billingStatus = billingStatus,
                billingFeatures = billingFeatures,
                selectedHiddenApps = selectedHiddenApps,
                links = links,
                selectedAutomationOption = selectedAutomationOption,
                values = values,
                onBack = {
                    coroutineScope.launch {
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        } else {
                            onBack()
                        }
                    }
                },
                onNavigateToGroup = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
                onNavigateToLinkScreen = onNavigateToLinkScreen,
            )
        },
        detailPane = { wide ->
            if (currentGroupId != null) {
                UserPreferenceDetailPane(
                    currentGroupId = currentGroupId,
                    appDetails = appDetails,
                    automationOptions = automationOptions,
                    billingAppNameResId = billingAppNameResId,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    hiddenAppsOptions = hiddenAppsOptions,
                    selectedServers = selectedServers,
                    servers = servers,
                    values = values,
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
                    onNavigateToBillingScreen = onNavigateToBillingScreen,
                    onNavigateToServerScreen = onNavigateToServerScreen,
                    onSelectServerGoogleMapsAddress = onSelectServerGoogleMapsAddress,
                    onSelectServerGoogleMapsPlace = onSelectServerGoogleMapsPlace,
                    onSelectServerSearch = onSelectServerSearch,
                    onValueChange = onValueChange,
                )
            }
        },
        colors = StyledPaneScaffoldDefaults.colors(
            wideMainContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
private fun UserPreferenceListPane(
    currentGroupId: UserPreferenceGroupId?,
    appDetails: StateFlow<AppDetails>,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    links: StateFlow<List<Link>>,
    selectedAutomationOption: StateFlow<SelectedAutomationOptionState>,
    selectedHiddenApps: StateFlow<SelectedHiddenAppsState>,
    values: StateFlow<UserPreferenceValues>,
    onBack: () -> Unit,
    onNavigateToGroup: (id: UserPreferenceGroupId) -> Unit,
    onNavigateToLinkScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current

    val values by values.collectAsStateWithLifecycle()

    LargeTopAppBarPane(
        modifier = Modifier.testTag("geoShareUserPreferencesListPane"),
        title = {
            Text(stringResource(R.string.user_preferences_title))
        },
        onBack = onBack,
    ) {
        item(key = "input_list_label", contentType = "segmented_list_label") {
            SegmentedListLabel(
                stringResource(R.string.user_preferences_section_input),
                Modifier.padding(horizontal = spacing.windowPadding),
                paddingTop = spacing.tiny,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item(key = "input_list", contentType = "column") {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(horizontal = spacing.windowPadding),
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
                UserPreferenceServerListItem(
                    index = 1,
                    count = 2,
                    selected = currentGroupId == UserPreferenceGroupId.SERVERS,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.SERVERS}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.SERVERS) },
                )
            }
        }
        item(key = "automation_list_label", contentType = "segmented_list_label") {
            SegmentedListLabel(
                stringResource(R.string.user_preferences_automation_title),
                modifier = Modifier.padding(horizontal = spacing.windowPadding),
            )
        }
        item(key = "automation_list", contentType = "column") {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(horizontal = spacing.windowPadding),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                UserPreferenceAutomationListItem(
                    appDetails = appDetails,
                    state = selectedAutomationOption,
                    index = 0,
                    count = 2,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    selected = currentGroupId == UserPreferenceGroupId.AUTOMATION,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.AUTOMATION}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.AUTOMATION) },
                )
                UserPreferenceAutomationDelayListItem(
                    state = selectedAutomationOption,
                    index = 1,
                    count = 2,
                    billingFeatures = billingFeatures,
                    billingStatus = billingStatus,
                    selected = currentGroupId == UserPreferenceGroupId.AUTOMATION_DELAY,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.AUTOMATION_DELAY}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.AUTOMATION_DELAY) },
                )
            }
        }
        item(key = "output_list_label", contentType = "segmented_list_label") {
            SegmentedListLabel(
                stringResource(R.string.user_preferences_section_output),
                modifier = Modifier.padding(horizontal = spacing.windowPadding),
            )
        }
        item(key = "output_list", contentType = "column") {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(horizontal = spacing.windowPadding),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                UserPreferenceHiddenAppsListItem(
                    state = selectedHiddenApps,
                    index = 0,
                    count = 4,
                    selected = currentGroupId == UserPreferenceGroupId.HIDDEN_APPS,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.HIDDEN_APPS}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.HIDDEN_APPS) },
                )
                UserPreferenceLinkListItem(
                    index = 1,
                    count = 4,
                    links = links,
                    selected = currentGroupId == UserPreferenceGroupId.LINKS,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.LINKS}"),
                    onClick = onNavigateToLinkScreen,
                )
                UserPreferenceCoordinateFormatListItem(
                    index = 2,
                    count = 4,
                    selected = currentGroupId == UserPreferenceGroupId.COORDINATE_FORMAT,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.COORDINATE_FORMAT}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.COORDINATE_FORMAT) },
                )
                UserPreferenceFinishListItem(
                    index = 3,
                    count = 4,
                    selected = currentGroupId == UserPreferenceGroupId.FINISH,
                    values = values,
                    modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.FINISH}"),
                    onClick = { onNavigateToGroup(UserPreferenceGroupId.FINISH) },
                )
            }
        }
        if (DynamicColorPreference.isAvailable()) {
            item(key = "dynamic_color_list_label", contentType = "segmented_list_label") {
                SegmentedListLabel(
                    stringResource(R.string.user_preferences_section_appearance),
                    modifier = Modifier.padding(horizontal = spacing.windowPadding),
                )
            }
            item(key = "dynamic_color_list", contentType = "column") {
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(horizontal = spacing.windowPadding),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    UserPreferenceDynamicColorListItem(
                        index = 0,
                        count = 1,
                        selected = currentGroupId == UserPreferenceGroupId.APPEARANCE_DYNAMIC_COLOR,
                        values = values,
                        modifier = Modifier.testTag("geoShareUserPreferencesGroup_${UserPreferenceGroupId.APPEARANCE_DYNAMIC_COLOR}"),
                        onClick = { onNavigateToGroup(UserPreferenceGroupId.APPEARANCE_DYNAMIC_COLOR) },
                    )
                }
            }
        }
        if (BuildConfig.DEBUG) {
            item(key = "developer_options_list_label", contentType = "segmented_list_label") {
                SegmentedListLabel(
                    stringResource(R.string.user_preferences_developer_title),
                    modifier = Modifier.padding(horizontal = spacing.windowPadding),
                )
            }
            item(key = "developer_options_list", contentType = "column") {
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(horizontal = spacing.windowPadding),
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
    appDetails: StateFlow<AppDetails>,
    automationOptions: StateFlow<AutomationOptionsState>,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
    hiddenAppsOptions: StateFlow<HiddenAppsOptionsState>,
    servers: StateFlow<List<Server>>,
    selectedServers: StateFlow<SelectedServers>,
    values: StateFlow<UserPreferenceValues>,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToServerScreen: (uid: Int?) -> Unit,
    onSelectServerGoogleMapsAddress: (uid: Int?) -> Unit,
    onSelectServerGoogleMapsPlace: (uid: Int?) -> Unit,
    onSelectServerSearch: (uid: Int?) -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val values by values.collectAsStateWithLifecycle()

    when (currentGroupId) {
        UserPreferenceGroupId.APPEARANCE_DYNAMIC_COLOR -> UserPreferenceDynamicColorControls(
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.AUTOMATION -> UserPreferenceAutomationControls(
            appDetails = appDetails,
            state = automationOptions,
            billingAppNameResId = billingAppNameResId,
            billingFeatures = billingFeatures,
            billingStatus = billingStatus,
            onBack = onBack,
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

        UserPreferenceGroupId.FINISH -> UserPreferenceFinishControls(
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.HIDDEN_APPS -> UserPreferenceHiddenAppsControls(
            appDetails = appDetails,
            state = hiddenAppsOptions,
            billingAppNameResId = billingAppNameResId,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onValueChange = onValueChange,
            values = values,
            wide = wide,
        )

        UserPreferenceGroupId.LINKS -> {}

        UserPreferenceGroupId.SERVERS -> UserPreferenceServerControls(
            billingAppNameResId = billingAppNameResId,
            selectedServers = selectedServers,
            servers = servers,
            onBack = onBack,
            onNavigateToBillingScreen = onNavigateToBillingScreen,
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelectServerGoogleMapsAddress = onSelectServerGoogleMapsAddress,
            onSelectServerGoogleMapsPlace = onSelectServerGoogleMapsPlace,
            onSelectServerSearch = onSelectServerSearch,
            wide = wide,
        )
    }
}

// Previews

@Preview(showBackground = true, device = "spec:width=1080px,height=3200px,dpi=440")
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferenceScreen(
                    initialGroupId = null,
                    appDetails = MutableStateFlow(emptyMap()),
                    automationOptions = MutableStateFlow(AutomationOptionsState()),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading,
                    hiddenAppsOptions = MutableStateFlow(HiddenAppsOptionsState()),
                    selectedAutomationOption = MutableStateFlow(SelectedAutomationOptionState()),
                    selectedHiddenApps = MutableStateFlow(SelectedHiddenAppsState()),
                    links = MutableStateFlow(defaultFakeLinks),
                    selectedServers = MutableStateFlow(SelectedServers()),
                    servers = MutableStateFlow(emptyList()),
                    values = MutableStateFlow(
                        defaultFakeUserPreferences.copy(connectionPermission = Permission.NEVER)
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3200px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferenceScreen(
                    initialGroupId = null,
                    appDetails = MutableStateFlow(emptyMap()),
                    automationOptions = MutableStateFlow(AutomationOptionsState()),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading,
                    hiddenAppsOptions = MutableStateFlow(HiddenAppsOptionsState()),
                    selectedAutomationOption = MutableStateFlow(SelectedAutomationOptionState()),
                    selectedHiddenApps = MutableStateFlow(SelectedHiddenAppsState()),
                    links = MutableStateFlow(defaultFakeLinks),
                    selectedServers = MutableStateFlow(SelectedServers()),
                    servers = MutableStateFlow(emptyList()),
                    values = MutableStateFlow(
                        defaultFakeUserPreferences.copy(connectionPermission = Permission.NEVER)
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
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
                    appDetails = MutableStateFlow(emptyMap()),
                    automationOptions = MutableStateFlow(AutomationOptionsState()),
                    billingAppNameResId = R.string.app_name_pro,
                    billingFeatures = listOf(AutomationFeature, CustomLinkFeature),
                    billingStatus = BillingStatus.Loading,
                    hiddenAppsOptions = MutableStateFlow(HiddenAppsOptionsState()),
                    selectedAutomationOption = MutableStateFlow(SelectedAutomationOptionState()),
                    selectedHiddenApps = MutableStateFlow(SelectedHiddenAppsState()),
                    links = MutableStateFlow(defaultFakeLinks),
                    selectedServers = MutableStateFlow(SelectedServers()),
                    servers = MutableStateFlow(emptyList()),
                    values = MutableStateFlow(
                        defaultFakeUserPreferences.copy(connectionPermission = Permission.NEVER)
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onNavigateToLinkScreen = {},
                    onNavigateToServerScreen = {},
                    onSelectServerGoogleMapsAddress = {},
                    onSelectServerGoogleMapsPlace = {},
                    onSelectServerSearch = {},
                    onValueChange = {},
                )
            }
        }
    }
}
