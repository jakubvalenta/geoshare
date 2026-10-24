package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.outputs.DrawableIconDescriptor
import page.ooooo.geoshare.data.outputs.PlaceholderIconDescriptor
import page.ooooo.geoshare.data.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.HiddenAppsOptionsState
import page.ooooo.geoshare.ui.SelectedHiddenAppsState
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun UserPreferenceHiddenAppsListItem(
    state: StateFlow<SelectedHiddenAppsState>,
    index: Int,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val state by state.collectAsStateWithLifecycle()

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        supportingContent = {
            Text(
                if (state.totalCount == state.visibleCount) {
                    stringResource(R.string.user_preferences_apps_visible_all)
                } else {
                    pluralStringResource(
                        R.plurals.user_preferences_apps_visible_count,
                        state.visibleCount,
                        state.visibleCount,
                        state.totalCount,
                    )
                }
            )
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.user_preferences_apps_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun UserPreferenceHiddenAppsControls(
    state: StateFlow<HiddenAppsOptionsState>,
    billingAppNameResId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    val state by state.collectAsStateWithLifecycle()

    UserPreferenceControls(
        titleResId = R.string.user_preferences_apps_title,
        description = {
            stringResource(R.string.user_preferences_apps_description)
        },
        billingAppNameResId = billingAppNameResId,
        wide = wide,
        onBack = onBack,
        onNavigateToBillingScreen = onNavigateToBillingScreen,
    ) {
        val value = HiddenAppsPreference.getValue(values)
        val enabled = value != null

        fun isChecked(packageName: String): Boolean = value?.contains(packageName) == false

        fun setValue(packageName: String, checked: Boolean) {
            val newValue = if (!checked) {
                value.orEmpty() + packageName
            } else {
                value.orEmpty() - packageName
            }
            onValueChange { preferences ->
                HiddenAppsPreference.setValue(preferences, newValue)
            }
        }

        item(key = "hidden_apps_list", contentType = "segmented_list") {
            val spacing = LocalSpacing.current

            SegmentedList(
                values = state.hiddenApps,
                modifier = modifier.padding(horizontal = spacing.windowPadding),
                itemHeadline = { packageName -> state.appDetails[packageName]?.label.orEmpty() },
                itemOnClick = { packageName -> setValue(packageName, !isChecked(packageName)) },
                itemEnabled = { enabled },
                itemLeadingContent = { packageName ->
                    {
                        IconFromDescriptor(
                            state.appDetails[packageName]?.icon?.let { DrawableIconDescriptor(it) }
                                ?: PlaceholderIconDescriptor,
                            contentDescription = null,
                            placeholderContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    }
                },
                itemTrailingContent = { packageName ->
                    {
                        Switch(
                            checked = isChecked(packageName),
                            onCheckedChange = {
                                setValue(packageName, it)
                            },
                            modifier = Modifier.testTag("geoShareVisibleAppToggle_${packageName}"),
                            enabled = enabled,
                        )
                    }
                },
                sort = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 1)),
                    index = 0,
                    count = 1,
                    selected = false,
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
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 1)),
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
private fun AllListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 3)),
                    index = 0,
                    count = 1,
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkAllListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 3)),
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
private fun NoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 0)),
                    index = 0,
                    count = 1,
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    state = MutableStateFlow(SelectedHiddenAppsState(totalCount = 3, visibleCount = 0)),
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
            val context = LocalContext.current
            UserPreferenceHiddenAppsControls(
                state = MutableStateFlow(
                    HiddenAppsOptionsState(
                        appDetails = getFakeAppDetails(context),
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        ),
                    )
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            UserPreferenceHiddenAppsControls(
                state = MutableStateFlow(
                    HiddenAppsOptionsState(
                        appDetails = getFakeAppDetails(context),
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        ),
                    )
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = true,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            UserPreferenceHiddenAppsControls(
                state = MutableStateFlow(
                    HiddenAppsOptionsState(
                        appDetails = getFakeAppDetails(context),
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        ),
                    )
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingControlsPreview() {
    AppTheme {
        Surface {
            UserPreferenceHiddenAppsControls(
                state = MutableStateFlow(
                    HiddenAppsOptionsState(
                        appDetails = emptyMap(),
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        ),
                    )
                ),
                billingAppNameResId = R.string.app_name_pro,
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}
