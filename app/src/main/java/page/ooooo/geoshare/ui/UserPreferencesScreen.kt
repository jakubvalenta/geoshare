package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import page.ooooo.geoshare.data.local.preferences.AutomationDelay
import page.ooooo.geoshare.data.local.preferences.AutomationFeatureValidatedAt
import page.ooooo.geoshare.data.local.preferences.AutomationUserPreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCode
import page.ooooo.geoshare.data.local.preferences.ConnectionPermission
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCode
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.ui.components.UserPreferencesDetailPane
import page.ooooo.geoshare.ui.components.UserPreferencesListPane
import page.ooooo.geoshare.ui.theme.AppTheme

@Keep
enum class UserPreferencesGroupId {
    AUTOMATION, AUTOMATION_DELAY, CONNECTION_PERMISSION, DEVELOPER_OPTIONS,
}

sealed class UserPreferencesGroup(
    val id: UserPreferencesGroupId,
    val titleResId: Int,
    val userPreferences: List<UserPreference<*>>,
    val visible: Boolean = true,
    open val featureValid: Boolean? = null,
) {
    open fun enabled(values: UserPreferencesValues): Boolean = true
}

object ConnectionPermissionUserPreferencesGroup : UserPreferencesGroup(
    id = UserPreferencesGroupId.CONNECTION_PERMISSION,
    titleResId = R.string.user_preferences_connection_title,
    userPreferences = listOf(ConnectionPermission),
)

class AutomationUserPreferencesGroup(override val featureValid: Boolean?) : UserPreferencesGroup(
    id = UserPreferencesGroupId.AUTOMATION,
    titleResId = R.string.user_preferences_automation_title,
    userPreferences = listOf(AutomationUserPreference),
)

object AutomationDelayUserPreferencesGroup : UserPreferencesGroup(
    id = UserPreferencesGroupId.AUTOMATION_DELAY,
    titleResId = R.string.user_preferences_automation_delay_sec_title,
    userPreferences = listOf(AutomationDelay),
) {
    override fun enabled(values: UserPreferencesValues) = values.automationValue is Automation.HasDelay
}

object DeveloperOptionsUserPreferencesGroup : UserPreferencesGroup(
    id = UserPreferencesGroupId.DEVELOPER_OPTIONS,
    titleResId = R.string.user_preferences_developer_title,
    userPreferences = listOf(
        ChangelogShownForVersionCode,
        IntroShowForVersionCode,
        AutomationFeatureValidatedAt,
    ),
    visible = BuildConfig.DEBUG,
)

@Composable
fun UserPreferencesScreen(
    initialGroupId: UserPreferencesGroupId?,
    onBack: () -> Unit,
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val automationFeatureValid by viewModel.automationFeatureValid.collectAsStateWithLifecycle()
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()

    UserPreferencesScreen(
        groups = listOf(
            ConnectionPermissionUserPreferencesGroup,
            AutomationUserPreferencesGroup(automationFeatureValid),
            AutomationDelayUserPreferencesGroup,
            DeveloperOptionsUserPreferencesGroup,
        ),
        initialGroupId = initialGroupId,
        userPreferencesValues = userPreferencesValues,
        onBack = onBack,
        onValueChange = { transform -> viewModel.editUserPreferences(transform) },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UserPreferencesScreen(
    groups: List<UserPreferencesGroup>,
    initialGroupId: UserPreferencesGroupId?,
    userPreferencesValues: UserPreferencesValues,
    onBack: () -> Unit,
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
    val currentGroup = navigator.currentDestination?.contentKey?.let { id -> groups.find { it.id == id } }
    val listExpanded = navigator.scaffoldState.targetState.primary == PaneAdaptedValue.Hidden
    val detailExpanded = navigator.scaffoldState.targetState.secondary == PaneAdaptedValue.Hidden

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
    ) { innerPadding ->
        NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    UserPreferencesListPane(
                        currentGroup = currentGroup,
                        groups = groups,
                        expanded = listExpanded || detailExpanded,
                        userPreferencesValues = userPreferencesValues,
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
                }
            },
            detailPane = {
                AnimatedPane {
                    if (currentGroup != null) {
                        UserPreferencesDetailPane(
                            currentGroup = currentGroup,
                            expanded = detailExpanded,
                            userPreferencesValues = userPreferencesValues,
                            onBack = {
                                coroutineScope.launch {
                                    if (navigator.canNavigateBack()) {
                                        navigator.navigateBack()
                                    } else {
                                        onBack()
                                    }
                                }
                            },
                            onValueChange = onValueChange,
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
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
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
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
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
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
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = UserPreferencesGroupId.CONNECTION_PERMISSION,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletEmptyPreview() {
    AppTheme {
        Surface {
            Column {
                UserPreferencesScreen(
                    groups = listOf(
                        ConnectionPermissionUserPreferencesGroup,
                        AutomationUserPreferencesGroup(null),
                        AutomationDelayUserPreferencesGroup,
                        DeveloperOptionsUserPreferencesGroup,
                    ),
                    initialGroupId = null,
                    userPreferencesValues = defaultFakeUserPreferences,
                    onBack = {},
                    onValueChange = {},
                )
            }
        }
    }
}
