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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceServerGoogleMapsListItem(
    index: Int,
    count: Int,
    selected: Boolean,
    selectedServer: Server?,
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
        supportingContent = {
            Text(
                selectedServer?.name ?: stringResource(R.string.user_preferences_google_maps_server_none),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_google_maps_server_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceServerGoogleMapsControls(
    selected: Server?,
    servers: List<Server>,
    wide: Boolean,
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onNavigateToServerScreen: () -> Unit,
    onSelect: (Server?) -> Unit,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_google_maps_server_title,
        description = {
            stringResource(R.string.user_preferences_google_maps_server_description)
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceServerControls(
            selected = selected,
            servers = servers,
            itemNoneHeadline = { stringResource(R.string.user_preferences_google_maps_server_none) },
            itemTestTag = { item, selected -> "geoShareUserPreferenceServer_${item?.name}_selected_${selected}" },
            onNavigateToServerScreen = onNavigateToServerScreen,
            onSelect = onSelect,
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
                UserPreferenceServerGoogleMapsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServer = FakeGeoShareGoogleMapsAddressServer,
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
                UserPreferenceServerGoogleMapsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServer = FakeGeoShareGoogleMapsAddressServer,
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
                UserPreferenceServerGoogleMapsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServer = null,
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
                UserPreferenceServerGoogleMapsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
                    selectedServer = null,
                    values = defaultFakeUserPreferences,
                    onClick = {},
                )
            }
        }
    }
}
