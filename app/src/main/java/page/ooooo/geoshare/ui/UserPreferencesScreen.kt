package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.FeatureBadgeLarge
import page.ooooo.geoshare.ui.components.FeatureBadgeSmall
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.ScrollablePane
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Keep
enum class UserPreferencesGroupId {
    AUTOMATION,
    AUTOMATION_DELAY,
    CONNECTION_PERMISSION,
    DEVELOPER_OPTIONS,
}

sealed interface UserPreferencesGroup {
    val id: UserPreferencesGroupId
    val titleResId: Int
    val userPreferences: List<UserPreference<*>>
    val visible: Boolean
    val featureStatus: FeatureStatus

    fun enabled(values: UserPreferencesValues): Boolean = true
}

object ConnectionPermissionUserPreferencesGroup : UserPreferencesGroup {
    override val id = UserPreferencesGroupId.CONNECTION_PERMISSION
    override val titleResId = R.string.user_preferences_connection_title
    override val userPreferences = listOf(ConnectionPermissionPreference)
    override val visible = true
    override val featureStatus = FeatureStatus.AVAILABLE
}

class AutomationUserPreferencesGroup(override val featureStatus: FeatureStatus) : UserPreferencesGroup {
    override val id = UserPreferencesGroupId.AUTOMATION
    override val titleResId = R.string.user_preferences_automation_title
    override val userPreferences = listOf(AutomationPreference)
    override val visible = true
}

class AutomationDelayUserPreferencesGroup(val featureStatusParam: FeatureStatus) : UserPreferencesGroup {
    override val id = UserPreferencesGroupId.AUTOMATION_DELAY
    override val titleResId = R.string.user_preferences_automation_delay_sec_title
    override val userPreferences = listOf(AutomationDelayPreference)
    override val visible = true
    override val featureStatus = FeatureStatus.AVAILABLE

    override fun enabled(values: UserPreferencesValues) =
        featureStatusParam == FeatureStatus.AVAILABLE && values.automation is Automation.HasDelay
}

object DeveloperOptionsUserPreferencesGroup : UserPreferencesGroup {
    override val id = UserPreferencesGroupId.DEVELOPER_OPTIONS
    override val titleResId = R.string.user_preferences_developer_title
    override val userPreferences = listOf(
        ChangelogShownForVersionCodePreference,
        IntroShowForVersionCodePreference,
        BillingCachedProductIdPreference,
    )
    override val visible = BuildConfig.DEBUG
    override val featureStatus = FeatureStatus.AVAILABLE
}

@Composable
fun UserPreferencesScreen(
    initialGroupId: UserPreferencesGroupId?,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val automationFeatureStatus by viewModel.automationFeatureStatus.collectAsStateWithLifecycle()
    val billingAppNameResId = viewModel.billingAppNameResId
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesScreen(
        groups = listOf(
            ConnectionPermissionUserPreferencesGroup,
            AutomationUserPreferencesGroup(automationFeatureStatus),
            AutomationDelayUserPreferencesGroup(automationFeatureStatus),
            DeveloperOptionsUserPreferencesGroup,
        ),
        initialGroupId = initialGroupId,
        billingAppNameResId = billingAppNameResId,
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
        onValueChange = { transform -> viewModel.editUserPreferences(transform) },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesScreen(
    groups: List<UserPreferencesGroup>,
    initialGroupId: UserPreferencesGroupId?,
    billingAppNameResId: Int,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
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
    val currentGroup = remember(navigator.currentDestination, groups) {
        navigator.currentDestination?.contentKey?.let { id -> groups.find { it.id == id } }
    }

    BasicListDetailScaffold(
        navigator = navigator,
        listPane = { wide, _ ->
            UserPreferencesListPane(
                currentGroup = currentGroup,
                groups = groups,
                userPreferencesValues = userPreferencesValues,
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
                onNavigateToGroup = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
            )
        },
        detailPane = { wide ->
            if (currentGroup != null) {
                UserPreferencesDetailPane(
                    billingAppNameResId = billingAppNameResId,
                    currentGroup = currentGroup,
                    userPreferencesValues = userPreferencesValues,
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
                    onValueChange = onValueChange,
                )
            }
        },
        listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserPreferencesListPane(
    currentGroup: UserPreferencesGroup?,
    groups: List<UserPreferencesGroup>,
    userPreferencesValues: UserPreferencesValues,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
) {
    val itemColor = if (wide) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val selectedItemColor = if (wide) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    ScrollablePane(
        titleResId = R.string.user_preferences_title,
        onBack = onBack,
    ) {
        groups.filter { it.visible }.forEach { group ->
            val enabled = group.enabled(userPreferencesValues)
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(group.titleResId),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.7f)
                    .clickable(enabled = enabled, onClick = { onNavigateToGroup(group.id) })
                    .testTag("geoShareUserPreferencesGroup_${group.id}"),
                supportingContent = group.userPreferences.takeIf { it.size == 1 }?.firstOrNull()
                    ?.let { userPreference ->
                        {
                            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                                userPreference.ValueLabel(userPreferencesValues, group.featureStatus)
                            }
                        }
                    },
                trailingContent = if (group.featureStatus == FeatureStatus.NOT_AVAILABLE) {
                    {
                        FeatureBadgeSmall(onClick = { onNavigateToGroup(group.id) })
                    }
                } else {
                    null
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (currentGroup == group) selectedItemColor else itemColor,
                ),
            )
        }
    }
}

@Composable
private fun UserPreferencesDetailPane(
    currentGroup: UserPreferencesGroup,
    billingAppNameResId: Int,
    userPreferencesValues: UserPreferencesValues,
    wide: Boolean,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
) {
    val spacing = LocalSpacing.current
    val enabled = currentGroup.enabled(userPreferencesValues)

    Box {
        Column {
            ScrollablePane(
                titleResId = currentGroup.titleResId,
                onBack = onBack.takeUnless { wide },
            ) {
                Column(
                    Modifier.padding(horizontal = spacing.windowPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.mediumAdaptive),
                ) {
                    for (userPreference in currentGroup.userPreferences) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
                            if (currentGroup.userPreferences.size > 1) {
                                ParagraphHtml(userPreference.title())
                            }
                            userPreference.Description(enabled && currentGroup.featureStatus == FeatureStatus.AVAILABLE)
                            userPreference.Component(
                                values = userPreferencesValues,
                                onValueChange = onValueChange,
                                enabled = enabled && currentGroup.featureStatus == FeatureStatus.AVAILABLE,
                                featureStatus = currentGroup.featureStatus,
                            )
                        }
                    }
                }
            }
        }
        if (currentGroup.featureStatus == FeatureStatus.NOT_AVAILABLE) {
            FeatureBadgeLarge(
                billingAppNameResId = billingAppNameResId,
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
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
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
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
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletConnectionPermissionPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.NOT_AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.NOT_AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletAutomationFeatureNotAvailablePreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.NOT_AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TableAutomationDelayPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.AVAILABLE),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.AUTOMATION_DELAY,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TableDeveloperOptionsPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(FeatureStatus.LOADING),
                        AutomationDelayUserPreferencesGroup(FeatureStatus.LOADING),
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.DEVELOPER_OPTIONS,
                    billingAppNameResId = R.string.app_name_pro,
                    userPreferencesValues = UserPreferencesValues(
                        automation = GpxOutput.SaveGpxPointsAutomation,
                    ),
                    onBack = {},
                    onNavigateToBillingScreen = {},
                    onValueChange = {},
                )
            }
        }
    }
}
