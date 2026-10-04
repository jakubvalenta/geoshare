package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeServers
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.SelectedServers
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun UserPreferenceServerListItem(
    index: Int,
    count: Int,
    selected: Boolean,
    values: UserPreferencesValues,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        enabled = ConnectionPermissionPreference.getValue(values) != Permission.NEVER,
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.server_list_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceServerControls(
    billingAppNameResId: Int,
    selectedServers: StateFlow<SelectedServers>,
    servers: StateFlow<List<Server>>,
    onBack: () -> Unit,
    onSelectServerGoogleMapsAddress: (server: Server?) -> Unit,
    onSelectServerGoogleMapsPlace: (server: Server?) -> Unit,
    onSelectServerSearch: (server: Server?) -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToServerScreen: (uid: Int?) -> Unit,
    wide: Boolean,
) {
    val spacing = LocalSpacing.current

    val selectedServers by selectedServers.collectAsStateWithLifecycle()
    val servers by servers.collectAsStateWithLifecycle()
    val optionGroups = remember(servers) { listOf(listOf(null) + servers) }

    UserPreferenceControls(
        titleResId = R.string.server_list_title,
        description = {
            stringResource(R.string.server_list_description, stringResource(R.string.app_name))
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceServerControl(
            group = "google_maps_address",
            isSelected = { server -> server == selectedServers.googleMapsAddress },
            label = { stringResource(R.string.server_list_google_maps_address_title) },
            noneDescription = { stringResource(R.string.server_list_google_maps_none_description) },
            optionGroups = optionGroups,
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelectServerGoogleMapsAddress,
        )
        userPreferenceServerDivider(group = "google_maps_place")
        userPreferenceServerControl(
            group = "google_maps_place",
            isSelected = { server -> server == selectedServers.googleMapsPlace },
            label = { stringResource(R.string.server_list_google_maps_place_title) },
            noneDescription = { stringResource(R.string.server_list_google_maps_none_description) },
            optionGroups = optionGroups,
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelectServerGoogleMapsPlace,
        )
        if (BuildConfig.DEBUG) {
            userPreferenceServerDivider(group = "search")
            userPreferenceServerControl(
                group = "search",
                isSelected = { server -> server == selectedServers.search },
                label = { stringResource(R.string.server_list_search_title) },
                noneDescription = { stringResource(R.string.server_list_search_none_description) },
                optionGroups = optionGroups,
                onNavigateToServerScreen = onNavigateToServerScreen,
                onSelect = onSelectServerSearch,
            )
        }
        item(key = "edit", contentType = "text_button") {
            TextButton(
                onClick = { onNavigateToServerScreen(null) },
                modifier = Modifier
                    .padding(horizontal = spacing.windowPadding)
                    .padding(top = spacing.small, bottom = spacing.tiny)
                    .testTag("geoShareUserPreferenceServerEdit"),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
            ) {
                Text(stringResource(R.string.user_preferences_server_edit))
            }
        }
    }
}

fun LazyListScope.userPreferenceServerControl(
    group: String,
    isSelected: (server: Server?) -> Boolean,
    label: @Composable () -> String,
    noneDescription: @Composable () -> String,
    optionGroups: List<List<Server?>>,
    onNavigateToServerScreen: (uid: Int?) -> Unit,
    onSelect: (server: Server?) -> Unit,
) {
    item(key = "${group}_label", contentType = "text") {
        val spacing = LocalSpacing.current
        Text(
            label(),
            modifier = Modifier.padding(
                start = spacing.windowPadding,
                top = spacing.tiny,
                end = spacing.windowPadding,
                bottom = spacing.extraTiny,
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    userPreferenceOptionsControl(
        key = "${group}_control",
        isSelected = isSelected,
        onSelect = onSelect,
        optionGroups = optionGroups,
        itemEnabled = { server -> server?.isValid() != false },
        itemTestTag = { server -> "geoShareUserPreferenceServer_${group}_${server?.name}" },
    ) { server, modifier ->
        Column(modifier) {
            Text(server?.name ?: stringResource(R.string.server_list_none))
            if (server == null) {
                Text(
                    noneDescription(),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if (!server.isValid()) {
                Text(
                    stringResource(R.string.server_invalid),
                    Modifier.clickable {
                        onNavigateToServerScreen(server.uid)
                    },
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if (server.description.isNotEmpty()) {
                Text(
                    server.description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

fun LazyListScope.userPreferenceServerDivider(group: String) {
    item(key = "${group}_divider", contentType = "horizontal_divider") {
        val spacing = LocalSpacing.current
        HorizontalDivider(Modifier.padding(vertical = spacing.small), thickness = Dp.Hairline)
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceServerListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    values = defaultFakeUserPreferences,
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
                UserPreferenceServerListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    values = defaultFakeUserPreferences,
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
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(
                    SelectedServers(
                        googleMapsAddress = defaultFakeServers[0],
                        googleMapsPlace = defaultFakeServers[1],
                        search = defaultFakeServers[2],
                    )
                ),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = true,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(
                    SelectedServers(
                        googleMapsAddress = defaultFakeServers[0],
                        googleMapsPlace = defaultFakeServers[1],
                        search = defaultFakeServers[2],
                    )
                ),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = true,
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(
                    SelectedServers(
                        googleMapsAddress = defaultFakeServers[0],
                        googleMapsPlace = defaultFakeServers[1],
                        search = defaultFakeServers[2],
                    )
                ),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(SelectedServers()),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = true,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(SelectedServers()),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = true,
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceServerControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServers = MutableStateFlow(SelectedServers()),
                servers = MutableStateFlow(defaultFakeServers),
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
                wide = false,
            )
        }
    }
}
