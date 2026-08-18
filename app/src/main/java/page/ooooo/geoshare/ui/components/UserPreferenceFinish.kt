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
import page.ooooo.geoshare.data.local.preferences.Finish
import page.ooooo.geoshare.data.local.preferences.FinishPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceFinishListItem(
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
        supportingContent = {
            FinishPreferenceValue(
                value = FinishPreference.getValue(values),
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_finish_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceFinishControls(
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_finish_title,
        description = {
            stringResource(R.string.user_preferences_finish_description)
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        userPreferenceOptionsControl(
            userPreference = FinishPreference,
            values = values,
            onValueChange = onValueChange,
            optionGroups = FinishPreference.getOptionGroups(),
            itemTestTag = { option -> "geoShareUserPreferenceFinish_${option}" },
        ) { option, _ ->
            FinishPreferenceValue(option)
        }
    }
}

@Composable
private fun FinishPreferenceValue(value: Finish) {
    Text(
        when (value) {
            Finish.AFTER_OPENING_APP ->
                stringResource(R.string.user_preferences_finish_after_opening_app)

            Finish.ALWAYS ->
                stringResource(R.string.user_preferences_finish_always)

            Finish.NEVER ->
                stringResource(R.string.user_preferences_finish_never)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceFinishListItem(
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
                UserPreferenceFinishListItem(
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
            UserPreferenceFinishControls(
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
            UserPreferenceFinishControls(
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
            UserPreferenceFinishControls(
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
