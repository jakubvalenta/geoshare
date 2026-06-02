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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsPlaceServer
import page.ooooo.geoshare.data.di.defaultFakeServers
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceServersListItem(
    index: Int,
    count: Int,
    selected: Boolean,
    selectedServerGoogleMapsAddress: Server?,
    selectedServerGoogleMapsPlace: Server?,
    selectedServerSearch: Server?,
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
            stringResource(R.string.user_preferences_server_google_maps_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceServersControls(
    selectedServerGoogleMapsAddress: Server?,
    selectedServerGoogleMapsPlace: Server?,
    selectedServerSearch: Server?,
    servers: List<Server>,
    wide: Boolean,
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToServerScreen: () -> Unit,
    onSelectServerGoogleMapsAddress: (Server?) -> Unit,
    onSelectServerGoogleMapsPlace: (Server?) -> Unit,
    onSelectServerSearch: (Server?) -> Unit,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_server_google_maps_title,
        description = {
            stringResource(R.string.user_preferences_server_google_maps_description)
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceServerControls(
            selected = selectedServerGoogleMapsAddress,
            servers = servers,
            itemNoneDescription = { stringResource(R.string.user_preferences_server_google_maps_none_description) },
            itemTestTag = { item -> "geoShareUserPreferenceServer_${item?.name}" },
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelectServerGoogleMapsAddress,
        )
        userPreferenceServerControls(
            selected = selectedServerGoogleMapsPlace,
            servers = servers,
            itemNoneDescription = { "" }, // TODO
            itemTestTag = { item -> "geoShareUserPreferenceServer_${item?.name}" },
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelectServerGoogleMapsPlace,
        )
        item {
            Text(stringResource(R.string.user_preferences_server_search_title))
        }
        item {
            Text(stringResource(R.string.user_preferences_server_search_description))
        }
        userPreferenceServerControls(
            selected = selectedServerSearch,
            servers = servers,
            itemNoneDescription = { stringResource(R.string.user_preferences_server_search_none_description) }, // TODO
            itemTestTag = { item -> "geoShareUserPreferenceServer_${item?.name}" },
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelectServerSearch,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceServersListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    values = defaultFakeUserPreferences,
                    onClick = {},
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
                UserPreferenceServersListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                    selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                    selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                    values = defaultFakeUserPreferences,
                    onClick = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun NoneSelectedListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceServersListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServerGoogleMapsAddress = null,
                    selectedServerGoogleMapsPlace = null,
                    selectedServerSearch = null,
                    values = defaultFakeUserPreferences,
                    onClick = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoneSelectedListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceServersListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServerGoogleMapsAddress = null,
                    selectedServerGoogleMapsPlace = null,
                    selectedServerSearch = null,
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
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = FakeGeoShareGoogleMapsAddressServer,
                selectedServerGoogleMapsPlace = FakeGeoShareGoogleMapsPlaceServer,
                selectedServerSearch = FakeGeoShareGoogleMapsAddressServer,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlsNoneSelectedPreview() {
    AppTheme {
        Surface {
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = null,
                selectedServerGoogleMapsPlace = null,
                selectedServerSearch = null,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoneSelectedControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = null,
                selectedServerGoogleMapsPlace = null,
                selectedServerSearch = null,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletNoneSelectedControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceServersControls(
                billingAppNameResId = R.string.app_name_pro,
                selectedServerGoogleMapsAddress = null,
                selectedServerGoogleMapsPlace = null,
                selectedServerSearch = null,
                servers = defaultFakeServers,
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onNavigateToServerScreen = {},
                onSelectServerGoogleMapsAddress = {},
                onSelectServerGoogleMapsPlace = {},
                onSelectServerSearch = {},
            )
        }
    }
}
