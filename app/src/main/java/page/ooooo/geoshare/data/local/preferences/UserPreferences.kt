package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
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
    fun valueLabel(value: T): String

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
    override fun valueLabel(value: Int?) = (value ?: default).toString()

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
    val label: String,
    val modifier: Modifier = Modifier,
)

abstract class OptionsUserPreference<T>(
    val default: T,
    val options: (@Composable () -> List<UserPreferenceOption<T>>),
) : UserPreference<T> {
    override val loading = default

    @Composable
    override fun valueLabel(value: T) =
        (options().find { it.value == value } ?: options().find { it.value == default })?.label ?: value.toString()

    @Composable
    override fun Component(value: T, onValueChange: (T) -> Unit) {
        RadioButtonGroup(
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        ) {
            options().map { option -> RadioButtonOption(option.value, option.label, option.modifier) }
        }
    }
}

val connectionPermission = object : OptionsUserPreference<Permission>(
    default = Permission.ASK,
    options = {
        listOf(
            UserPreferenceOption(
                Permission.ALWAYS,
                stringResource(R.string.user_preferences_connection_option_always),
                Modifier.testTag("geoShareUserPreferenceConnectionPermissionAlways"),
            ),
            UserPreferenceOption(Permission.ASK, stringResource(R.string.user_preferences_connection_option_ask)),
            UserPreferenceOption(Permission.NEVER, stringResource(R.string.user_preferences_connection_option_never)),
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

val automaticAction = object : OptionsUserPreference<AutomaticAction>(
    default = AutomaticAction(AutomaticAction.Type.NONE),
    options = {
        val context = LocalContext.current
        val intentTools = IntentTools()
        buildList {
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.NONE),
                    stringResource(R.string.user_preferences_automatic_action_none),
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_COORDS_DEC),
                    stringResource(
                        R.string.user_preferences_automatic_action_copy_coords,
                        examplePosition.toCoordsDecString(),
                    )
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_COORDS_NSWE_DEC),
                    stringResource(
                        R.string.user_preferences_automatic_action_copy_coords,
                        examplePosition.toNorthSouthWestEastDecCoordsString(),
                    )
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_GEO_URI),
                    stringResource(R.string.conversion_succeeded_copy_geo),
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_GOOGLE_MAPS_URI),
                    stringResource(R.string.user_preferences_automatic_action_copy_google_maps_link),
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_APPLE_MAPS_URI),
                    stringResource(R.string.user_preferences_automatic_action_copy_apple_maps_link),
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.COPY_MAGIC_EARTH_URI),
                    stringResource(R.string.user_preferences_automatic_action_copy_magic_earth_link),
                )
            )
            for (app in intentTools.queryGeoUriApps(context.packageManager)) {
                add(
                    UserPreferenceOption(
                        AutomaticAction(AutomaticAction.Type.OPEN_APP, app.packageName),
                        stringResource(R.string.user_preferences_automatic_action_open_app, app.packageName)
                    )
                )
            }
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.SAVE_GPX),
                    stringResource(R.string.conversion_succeeded_save_gpx),
                )
            )
            add(
                UserPreferenceOption(
                    AutomaticAction(AutomaticAction.Type.SHARE),
                    stringResource(R.string.conversion_succeeded_share),
                )
            )
        }
    }
) {
    private val typeKey = stringPreferencesKey("automatic_action")
    private val packageNameKey = stringPreferencesKey("automatic_action_package_name")

    override fun getValue(preferences: Preferences): AutomaticAction = AutomaticAction(
        type = preferences[typeKey]?.let(AutomaticAction.Type::valueOf) ?: default.type,
        packageName = preferences[packageNameKey]?.takeIf { it.isNotEmpty() } ?: default.packageName,
    )

    override fun setValue(preferences: MutablePreferences, value: AutomaticAction) {
        preferences[typeKey] = value.type.name
        preferences[packageNameKey] = value.packageName ?: ""
    }

    @Composable
    override fun title() = stringResource(R.string.user_preferences_automatic_action_title)

    @Composable
    override fun description() = stringResource(R.string.user_preferences_automatic_action_description)
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
    var automaticActionValue: AutomaticAction = automaticAction.loading,
    var connectionPermissionValue: Permission = connectionPermission.loading,
    var introShownForVersionCodeValue: Int? = introShowForVersionCode.loading,
    var changelogShownForVersionCodeValue: Int? = changelogShownForVersionCode.loading,
)
