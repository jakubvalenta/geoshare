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
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceConnectionPermissionListItem(
    index: Int,
    count: Int,
    values: UserPreferencesValues,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        supportingContent = {
            ConnectionPermissionPreferenceValue(
                value = ConnectionPermissionPreference.getValue(values),
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_connection_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceConnectionPermissionControls(
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_connection_title,
        description = {
            stringResource(
                R.string.user_preferences_connection_description,
                stringResource(R.string.app_name),
            )
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceOptionsControl(
            userPreference = ConnectionPermissionPreference,
            values = values,
            onValueChange = onValueChange,
            optionGroups = ConnectionPermissionPreference.getOptionGroups(),
            itemTestTag = { option -> "geoShareUserPreferenceConnectionPermission_${option}" },
        ) { option, modifier ->
            ConnectionPermissionPreferenceValue(option, modifier)
        }
    }
}

@Composable
private fun ConnectionPermissionPreferenceValue(value: Permission, modifier: Modifier = Modifier) {
    Text(
        stringResource(
            when (value) {
                Permission.ALWAYS -> R.string.user_preferences_connection_option_always
                Permission.ASK -> R.string.user_preferences_connection_option_ask
                Permission.NEVER -> R.string.user_preferences_connection_option_never
            }
        ),
        modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceConnectionPermissionListItem(
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
                UserPreferenceConnectionPermissionListItem(
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
            UserPreferenceConnectionPermissionControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = defaultFakeUserPreferences,
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
            UserPreferenceConnectionPermissionControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = defaultFakeUserPreferences,
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
            UserPreferenceConnectionPermissionControls(
                billingAppNameResId = R.string.app_name_pro,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
                values = defaultFakeUserPreferences,
                wide = false,
            )
        }
    }
}
