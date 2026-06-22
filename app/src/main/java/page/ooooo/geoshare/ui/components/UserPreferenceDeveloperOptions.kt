package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceDeveloperOptionsListItem(
    index: Int,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_developer_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceDeveloperOptionsControls(
    billingAppNameResId: Int,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: (transform: (preferences: MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    UserPreferenceControls(
        titleResId = R.string.user_preferences_developer_title,
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        item {
            ParagraphText(
                stringResource(R.string.user_preferences_changelog_shown_for_version_code_title),
                Modifier
                    .padding(horizontal = LocalSpacing.current.windowPadding)
                    .padding(bottom = LocalSpacing.current.smallAdaptive),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        userPreferenceTextControl(
            userPreference = ChangelogShownForVersionCodePreference,
            values = values,
            onValueChange = onValueChange,
            modifier = Modifier.testTag("geoShareUserPreferenceChangelogShownForVersionCode"),
        )
        item {
            ParagraphText(
                stringResource(R.string.user_preferences_last_run_version_code_title),
                Modifier
                    .padding(horizontal = LocalSpacing.current.windowPadding)
                    .padding(
                        top = LocalSpacing.current.mediumAdaptive,
                        bottom = LocalSpacing.current.smallAdaptive,
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        userPreferenceTextControl(
            userPreference = IntroShowForVersionCodePreference,
            values = values,
            onValueChange = onValueChange,
        )
        item {
            ParagraphText(
                stringResource(R.string.user_preferences_billing_cached_purchase),
                Modifier
                    .padding(horizontal = LocalSpacing.current.windowPadding)
                    .padding(
                        top = LocalSpacing.current.mediumAdaptive,
                        bottom = LocalSpacing.current.smallAdaptive,
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        userPreferenceTextControl(
            userPreference = CachedPurchasePreference,
            values = values,
            onValueChange = onValueChange,
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
                UserPreferenceDeveloperOptionsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
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
                UserPreferenceDeveloperOptionsListItem(
                    index = 0,
                    count = 1,
                    selected = false,
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
            UserPreferenceDeveloperOptionsControls(
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
            UserPreferenceDeveloperOptionsControls(
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
            UserPreferenceDeveloperOptionsControls(
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
