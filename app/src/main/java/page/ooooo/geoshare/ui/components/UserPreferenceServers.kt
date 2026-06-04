package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.BuildConfig
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
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceServersListItem(
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
            stringResource(R.string.user_preferences_servers_title),
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
        titleResId = R.string.user_preferences_servers_title,
        description = {
            stringResource(R.string.user_preferences_servers_description, stringResource(R.string.app_name))
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        item {
            Button(
                onClick = onNavigateToServerScreen,
                modifier = Modifier.testTag("geoShareUserPreferenceNavigateToServerList"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(stringResource(R.string.user_preferences_navigate_to_server_list))
            }
        }
        item {
            val spacing = LocalSpacing.current
            LabelLarge(
                stringResource(R.string.user_preferences_servers_google_maps_address_title),
                Modifier.padding(top = spacing.mediumAdaptive),
            )
        }
        userPreferenceServerControls(
            selected = selectedServerGoogleMapsAddress,
            servers = servers,
            itemNoneDescription = { stringResource(R.string.user_preferences_servers_google_maps_none_description) },
            itemTestTag = { item -> "geoShareUserPreferenceServerGoogleMapsAddress_${item?.name}" },
            onSelect = onSelectServerGoogleMapsAddress,
        )
        item {
            val spacing = LocalSpacing.current
            LabelLarge(
                stringResource(R.string.user_preferences_servers_google_maps_place_title),
                Modifier.padding(top = spacing.mediumAdaptive),
            )
        }
        userPreferenceServerControls(
            selected = selectedServerGoogleMapsPlace,
            servers = servers,
            itemNoneDescription = { stringResource(R.string.user_preferences_servers_google_maps_none_description) },
            itemTestTag = { item -> "geoShareUserPreferenceServerGoogleMapsPlace_${item?.name}" },
            onSelect = onSelectServerGoogleMapsPlace,
        )
        if (BuildConfig.DEBUG) {
            item {
                val spacing = LocalSpacing.current
                LabelLarge(
                    stringResource(R.string.user_preferences_servers_search_title),
                    Modifier.padding(top = spacing.mediumAdaptive),
                )
            }
            userPreferenceServerControls(
                selected = selectedServerSearch,
                servers = servers,
                itemNoneDescription = { stringResource(R.string.user_preferences_servers_search_none_description) },
                itemTestTag = { item -> "geoShareUserPreferenceServerSearch_${item?.name}" },
                onSelect = onSelectServerSearch,
            )
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
                UserPreferenceServersListItem(
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
                    values = defaultFakeUserPreferences,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=3500px,dpi=440")
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

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=3500px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
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
