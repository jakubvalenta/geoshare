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
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.CoordinateFormatPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceCoordinateFormatListItem(
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
            CoordinateFormatPreferenceValue(
                value = CoordinateFormatPreference.getValue(values),
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_coordinate_format_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceCoordinateFormatControls(
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_coordinate_format_title,
        description = {
            stringResource(R.string.user_preferences_coordinate_format_description)
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceOptionsControl(
            userPreference = CoordinateFormatPreference,
            values = values,
            onValueChange = onValueChange,
            optionGroups = CoordinateFormatPreference.getOptionGroups(),
            itemTestTag = { option -> "geoShareUserPreferenceCoordinateFormat_${option}" },
        ) { option, modifier ->
            Column(modifier) {
                CoordinateFormatPreferenceValue(option)
                Text(
                    when (option) {
                        CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(WGS84Point(NaivePoint.example))
                        CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                            WGS84Point(
                                NaivePoint.example
                            )
                        )
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CoordinateFormatPreferenceValue(value: CoordinateFormat) {
    Text(
        stringResource(
            when (value) {
                CoordinateFormat.DEC -> R.string.user_preferences_coordinate_format_option_dec
                CoordinateFormat.DEG_MIN_SEC -> R.string.user_preferences_coordinate_format_option_deg_min_sec
            }
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceCoordinateFormatListItem(
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
                UserPreferenceCoordinateFormatListItem(
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
            UserPreferenceCoordinateFormatControls(
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
            UserPreferenceCoordinateFormatControls(
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
            UserPreferenceCoordinateFormatControls(
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
