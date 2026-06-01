package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeUserPreferences
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataTypes
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceHiddenAppsListItem(
    index: Int,
    count: Int,
    apps: DataTypes,
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
        supportingContent = HiddenAppsPreference.getValue(values)?.let { value ->
            @Composable {
                val options = HiddenAppsPreference.getOptions(apps)
                Text(
                    (options - value).size.takeIf { it != options.size }?.let { visibleCount ->
                        pluralStringResource(
                            R.plurals.user_preferences_apps_visible_count,
                            visibleCount,
                            visibleCount,
                            options.size,
                        )
                    } ?: stringResource(R.string.user_preferences_apps_visible_all)
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
    appDetails: AppDetails,
    apps: DataTypes,
    billingAppNameResId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    values: UserPreferencesValues,
    wide: Boolean,
) {
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

        fun isChecked(option: String): Boolean =
            value?.contains(option) == false

        fun setValue(option: String, checked: Boolean) {
            val newValue = (value ?: emptySet()).let { value ->
                if (!checked) {
                    value + option
                } else {
                    value - option
                }
            }
            onValueChange { preferences ->
                HiddenAppsPreference.setValue(preferences, newValue)
            }
        }

        item {
            SegmentedList(
                values = HiddenAppsPreference.getOptions(apps).toList(),
                modifier = modifier,
                itemHeadline = { option -> appDetails[option]?.label ?: option },
                itemOnClick = { option -> setValue(option, !isChecked(option)) },
                itemEnabled = { enabled },
                itemLeadingContent = { option ->
                    appDetails[option]?.icon?.let { drawable ->
                        {
                            Image(
                                rememberDrawablePainter(drawable),
                                null,
                                Modifier.widthIn(max = 24.dp),
                            )
                        }
                    }
                },
                itemTrailingContent = { option ->
                    {
                        Switch(
                            checked = isChecked(option),
                            onCheckedChange = {
                                setValue(option, it)
                            },
                            modifier = Modifier.testTag("geoShareVisibleAppToggle_${option}"),
                            enabled = enabled,
                        )
                    }
                },
                sort = true,
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
                UserPreferenceHiddenAppsListItem(
                    index = 0,
                    count = 1,
                    apps = mapOf(
                        PackageNames.COMAPS_FDROID to emptySet(),
                        PackageNames.ORGANIC_MAPS to emptySet(),
                        PackageNames.OSMAND_PLUS to emptySet(),
                    ),
                    selected = false,
                    values = UserPreferencesValues(hiddenApps = setOf(PackageNames.ORGANIC_MAPS)),
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
                UserPreferenceHiddenAppsListItem(
                    index = 0,
                    count = 1,
                    apps = mapOf(
                        PackageNames.COMAPS_FDROID to emptySet(),
                        PackageNames.ORGANIC_MAPS to emptySet(),
                        PackageNames.OSMAND_PLUS to emptySet(),
                    ),
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
private fun ControlsPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = defaultFakeUserPreferences,
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
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = defaultFakeUserPreferences,
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
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = defaultFakeUserPreferences,
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = UserPreferencesValues(),
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = UserPreferencesValues(),
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
            val context = LocalContext.current
            @SuppressLint("LocalContextGetResourceValueCall")
            UserPreferenceHiddenAppsControls(
                billingAppNameResId = R.string.app_name_pro,
                apps = mapOf(
                    PackageNames.COMAPS_FDROID to emptySet(),
                    PackageNames.ORGANIC_MAPS to emptySet(),
                    PackageNames.OSMAND_PLUS to emptySet(),
                ),
                appDetails = mapOf(
                    PackageNames.COMAPS_FDROID to AppDetail(
                        "CoMaps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.ORGANIC_MAPS to AppDetail(
                        "Organic Maps",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                    PackageNames.OSMAND_PLUS to AppDetail(
                        "OsmAnd",
                        context.getDrawable(R.mipmap.ic_launcher_round)!!
                    ),
                ),
                values = UserPreferencesValues(),
                wide = false,
                onBack = {},
                onNavigateToBillingScreen = {},
                onValueChange = {},
            )
        }
    }
}
