package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.ui.HiddenAppDetail
import page.ooooo.geoshare.ui.HiddenAppsSize
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceHiddenAppsListItem(
    index: Int,
    count: Int,
    hiddenAppsSize: StateFlow<HiddenAppsSize>,
    selected: Boolean,
    values: UserPreferencesValues,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val hiddenAppsSize by hiddenAppsSize.collectAsStateWithLifecycle()

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        supportingContent = HiddenAppsPreference.getValue(values)?.let { value ->
            {
                Text(
                    if (hiddenAppsSize.visible == 0) {
                        stringResource(R.string.user_preferences_apps_visible_all)
                    } else {
                        pluralStringResource(
                            R.plurals.user_preferences_apps_visible_count,
                            hiddenAppsSize.visible,
                            hiddenAppsSize.visible,
                            hiddenAppsSize.total,
                        )
                    }
                )
            }
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
    billingAppNameResId: Int,
    hiddenAppsDetails: StateFlow<List<HiddenAppDetail>>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
    val hiddenAppsDetails by hiddenAppsDetails.collectAsStateWithLifecycle()

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

        fun isChecked(packageName: String): Boolean =
            value?.contains(packageName) == false

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

        item {
            val spacing = LocalSpacing.current

            SegmentedList(
                values = hiddenAppsDetails,
                modifier = modifier.padding(horizontal = spacing.windowPadding),
                itemHeadline = { detail -> detail.appLabel.orEmpty() },
                itemOnClick = { detail -> setValue(detail.packageName, !isChecked(detail.packageName)) },
                itemEnabled = { enabled },
                itemLeadingContent = { detail ->
                    {
                        IconFromDescriptor(
                            detail.icon?.let { DrawableIconDescriptor(it) } ?: PlaceholderIconDescriptor,
                            contentDescription = null,
                        )
                    }
                },
                itemTrailingContent = { detail ->
                    {
                        Switch(
                            checked = isChecked(detail.packageName),
                            onCheckedChange = {
                                setValue(detail.packageName, it)
                            },
                            modifier = Modifier.testTag("geoShareVisibleAppToggle_${detail}"),
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
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
                    selected = false,
                    values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
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
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
                    selected = false,
                    values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
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
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
                    selected = false,
                    values = UserPreferencesValues(
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    ),
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
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
                    selected = false,
                    values = UserPreferencesValues(
                        hiddenApps = setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    ),
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
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
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
private fun DarkNoneListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceHiddenAppsListItem(
                    index = 0,
                    count = 1,
                    activities = fakeActivities.filter {
                        it.packageName in setOf(
                            PackageNames.COMAPS_FDROID,
                            PackageNames.ORGANIC_MAPS,
                            PackageNames.OSMAND_PLUS,
                        )
                    },
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
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
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
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
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
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
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
private fun LoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletHiddenAppsLoadingPreview() {
    AppTheme {
        Surface {
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                activities = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.ORGANIC_MAPS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetails = fakeAppDetails(),
                values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}
