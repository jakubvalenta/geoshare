package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.R
import page.ooooo.geoshare.components.RadioButtonGroup
import page.ooooo.geoshare.components.RadioButtonOption
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.ui.theme.Spacing

private val examplePosition = Position("50.123456", "-11.123456")

interface UserPreference<T> {
    val loading: T

    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun title(): String

    @Composable
    fun description(): String?

    @Composable
    fun ValueLabel(value: T)

    @Composable
    fun Component(value: T, onValueChange: (T) -> Unit)
}

abstract class NullableIntUserPreference(
    val key: Preferences.Key<String>,
    val default: Int?,
    val modifier: Modifier = Modifier,
) : UserPreference<Int?> {
    override val loading = null

    override fun getValue(preferences: Preferences): Int? = fromString(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: Int?) {
        preferences[key] = value.toString()
    }

    @Composable
    override fun ValueLabel(value: Int?) {
        Text((value ?: default).toString())
    }

    @Composable
    override fun Component(value: Int?, onValueChange: (Int?) -> Unit) {
        var inputValue by remember { mutableStateOf(value.toString()) }
        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                @Suppress("AssignedValueIsNeverRead")
                inputValue = it
                onValueChange(fromString(it))
            },
            modifier = modifier.padding(top = Spacing.tiny),
        )
    }

    private fun fromString(value: String?): Int? = try {
        value?.toInt()
    } catch (_: NumberFormatException) {
        null
    } ?: default
}

data class UserPreferenceOption<T>(
    val value: T,
    val modifier: Modifier = Modifier,
    val label: @Composable () -> Unit,
)

abstract class OptionsUserPreference<T>(
    val default: T,
    val options: (@Composable () -> List<UserPreferenceOption<T>>),
) : UserPreference<T> {
    override val loading = default

    @Composable
    override fun ValueLabel(value: T) {
        (options().find { it.value == value } ?: options().find { it.value == default })?.also { option ->
            option.label()
        } ?: Text(value.toString())
    }

    @Composable
    override fun Component(value: T, onValueChange: (T) -> Unit) {
        RadioButtonGroup(
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        ) {
            options().map { option -> RadioButtonOption(option.value, option.modifier, option.label) }
        }
    }
}

val connectionPermission = object : OptionsUserPreference<Permission>(
    default = Permission.ASK,
    options = {
        listOf(
            UserPreferenceOption(
                Permission.ALWAYS,
                Modifier.testTag("geoShareUserPreferenceConnectionPermissionAlways"),
            ) {
                Text(stringResource(R.string.user_preferences_connection_option_always))
            },
            UserPreferenceOption(Permission.ASK) {
                Text(stringResource(R.string.user_preferences_connection_option_ask))
            },
            UserPreferenceOption(Permission.NEVER) {
                Text(stringResource(R.string.user_preferences_connection_option_never))
            },
        )
    },
) {
    private val key = stringPreferencesKey("connect_to_google_permission")

    override fun getValue(preferences: Preferences) = preferences[key]?.let(Permission::valueOf) ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission) {
        preferences[key] = value.name
    }

    @Composable
    override fun title() = stringResource(R.string.user_preferences_connection_title)

    @Composable
    override fun description() =
        stringResource(R.string.user_preferences_connection_description, stringResource(R.string.app_name))
}

val automation = object : OptionsUserPreference<Automation>(
    default = Automation(Automation.Type.NOTHING),
    options = {
        val context = LocalContext.current
        val intentTools = IntentTools()
        buildList {
            add(
                UserPreferenceOption(Automation(Automation.Type.NOTHING)) {
                    Text(stringResource(R.string.user_preferences_automation_nothing))
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_COORDS_DEC)) {
                    Text(
                        stringResource(
                            R.string.user_preferences_automation_copy_coords,
                            examplePosition.toCoordsDecString()
                        )
                    )
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_COORDS_NSWE_DEC)) {
                    Text(
                        stringResource(
                            R.string.user_preferences_automation_copy_coords,
                            examplePosition.toNorthSouthWestEastDecCoordsString(),
                        )
                    )
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_GEO_URI)) {
                    Text(stringResource(R.string.conversion_succeeded_copy_geo))
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_GOOGLE_MAPS_URI)) {
                    Text(stringResource(R.string.user_preferences_automation_copy_google_maps_link))
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_APPLE_MAPS_URI)) {
                    Text(stringResource(R.string.user_preferences_automation_copy_apple_maps_link))
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.COPY_MAGIC_EARTH_URI)) {
                    Text(stringResource(R.string.user_preferences_automation_copy_magic_earth_link))
                }
            )
            for (app in intentTools.queryGeoUriApps(context.packageManager)) {
                add(
                    UserPreferenceOption(Automation(Automation.Type.OPEN_APP, app.packageName)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                rememberDrawablePainter(app.icon),
                                app.label,
                                Modifier.widthIn(max = 24.dp),
                            )
                            Text(stringResource(R.string.user_preferences_automation_open_app, app.label))
                        }
                    }
                )
            }
            add(
                UserPreferenceOption(Automation(Automation.Type.SAVE_GPX)) {
                    Text(stringResource(R.string.conversion_succeeded_save_gpx))
                }
            )
            add(
                UserPreferenceOption(Automation(Automation.Type.SHARE)) {
                    Text(stringResource(R.string.conversion_succeeded_share))
                }
            )
        }
    }
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    override fun getValue(preferences: Preferences): Automation = Automation(
        type = preferences[typeKey]?.let(Automation.Type::valueOf) ?: default.type,
        packageName = preferences[packageNameKey]?.takeIf { it.isNotEmpty() } ?: default.packageName,
    )

    override fun setValue(preferences: MutablePreferences, value: Automation) {
        preferences[typeKey] = value.type.name
        preferences[packageNameKey] = value.packageName ?: ""
    }

    @Composable
    override fun title() = stringResource(R.string.user_preferences_automation_title)

    @Composable
    override fun description() = stringResource(R.string.user_preferences_automation_description)
}

val introShowForVersionCode = object : NullableIntUserPreference(
    key = stringPreferencesKey("intro_shown_for_version_code"),
    default = 0,
) {
    @Composable
    override fun title() = stringResource(R.string.user_preferences_last_run_version_code_title)

    @Composable
    override fun description() = null
}

val changelogShownForVersionCode = object : NullableIntUserPreference(
    key = stringPreferencesKey("changelog_shown_for_version_code"),
    default = 22,
    modifier = Modifier.testTag("geoShareUserPreferenceChangelogShownForVersionCode"),
) {
    @Composable
    override fun title() = stringResource(R.string.user_preferences_changelog_shown_for_version_code_title)

    @Composable
    override fun description() = null
}

data class UserPreferencesValues(
    var automationValue: Automation = automation.loading,
    var connectionPermissionValue: Permission = connectionPermission.loading,
    var introShownForVersionCodeValue: Int? = introShowForVersionCode.loading,
    var changelogShownForVersionCodeValue: Int? = changelogShownForVersionCode.loading,
)
