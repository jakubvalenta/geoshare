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
    val title: @Composable () -> String
    val description: (@Composable () -> String)?
    val loading: T

    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun Component(value: T, onValueChange: (T) -> Unit)
}

class NullableIntUserPreference(
    override val title: @Composable () -> String,
    override val description: (@Composable () -> String)?,
    val key: Preferences.Key<String>,
    val default: Int?,
    override val loading: Int?,
    val modifier: Modifier = Modifier,
) : UserPreference<Int?> {
    override fun getValue(preferences: Preferences): Int? = fromString(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: Int?) {
        preferences[key] = value.toString()
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

class PermissionUserPreference(
    override val title: @Composable () -> String,
    override val description: (@Composable () -> String)?,
    val key: Preferences.Key<String>,
    val default: Permission,
    override val loading: Permission = default,
    val options: (@Composable () -> List<RadioButtonOption<Permission>>),
) : UserPreference<Permission> {
    override fun getValue(preferences: Preferences) = preferences[key]?.let(Permission::valueOf) ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission) {
        preferences[key] = value.name
    }

    @Composable
    override fun Component(value: Permission, onValueChange: (Permission) -> Unit) {
        RadioButtonGroup(
            options = options(),
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        )
    }
}

data class AutomaticAction(
    val type: AutomaticActionType,
    val packageName: String? = null,
)

class AutomaticActionUserPreference(
    override val title: @Composable () -> String,
    override val description: (@Composable () -> String)?,
    val typeKey: Preferences.Key<String>,
    val packageNameKey: Preferences.Key<String>,
    val default: AutomaticAction,
    override val loading: AutomaticAction = default,
    val options: (@Composable () -> List<RadioButtonOption<AutomaticAction>>),
) : UserPreference<AutomaticAction> {
    override fun getValue(preferences: Preferences): AutomaticAction = AutomaticAction(
        type = preferences[typeKey]?.let(AutomaticActionType::valueOf) ?: default.type,
        packageName = preferences[packageNameKey]?.takeIf { it.isNotEmpty() } ?: default.packageName,
    )

    override fun setValue(preferences: MutablePreferences, value: AutomaticAction) {
        preferences[typeKey] = value.type.name
        preferences[packageNameKey] = value.packageName ?: ""
    }

    @Composable
    override fun Component(value: AutomaticAction, onValueChange: (AutomaticAction) -> Unit) {
        RadioButtonGroup(
            options = options(),
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        )
    }
}

val connectionPermission = PermissionUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_connection_title)
    },
    description = @Composable {
        stringResource(R.string.user_preferences_connection_description, stringResource(R.string.app_name))
    },
    key = stringPreferencesKey("connect_to_google_permission"),
    default = Permission.ASK,
) {
    listOf(
        RadioButtonOption(
            Permission.ALWAYS,
            stringResource(R.string.user_preferences_connection_option_always),
            modifier = Modifier.testTag("geoShareUserPreferenceConnectionPermissionAlways"),
        ),
        RadioButtonOption(Permission.ASK, stringResource(R.string.user_preferences_connection_option_ask)),
        RadioButtonOption(Permission.NEVER, stringResource(R.string.user_preferences_connection_option_never)),
    )
}

val automaticAction = AutomaticActionUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_automatic_action_title)
    },
    description = @Composable {
        stringResource(R.string.user_preferences_automatic_action_description)
    },
    typeKey = stringPreferencesKey("automatic_action"),
    packageNameKey = stringPreferencesKey("automatic_action_package_name"),
    default = AutomaticAction(AutomaticActionType.NONE),
) {
    val context = LocalContext.current
    val intentTools = IntentTools()
    buildList {
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.NONE),
                "Nothing", // TODO
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_COORDS_DEC),
                "Copy coordinates in format %s".format(examplePosition.toCoordsDecString()) // TOOD
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_COORDS_NSWE_DEC),
                "Copy coordinates in format %s".format(examplePosition.toNorthSouthWestEastDecCoordsString()), // TODO
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_GEO_URI),
                stringResource(R.string.conversion_succeeded_copy_geo),
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_GOOGLE_MAPS_URI),
                "Copy Google Maps link", // TODO
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_APPLE_MAPS_URI),
                "Copy Apple Maps link", // TODO
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.COPY_MAGIC_EARTH_URI),
                "Copy Magic Earth link", // TODO
            )
        )
        for (app in intentTools.queryGeoUriApps(context.packageManager)) {
            add(
                RadioButtonOption(
                    AutomaticAction(AutomaticActionType.OPEN_APP, app.packageName),
                    "Open %s".format(app.label), // TODO
                )
            )
        }
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.SAVE_GPX),
                stringResource(R.string.conversion_succeeded_save_gpx),
            )
        )
        add(
            RadioButtonOption(
                AutomaticAction(AutomaticActionType.SHARE),
                stringResource(R.string.conversion_succeeded_share),
            )
        )
    }
}

val lastRunVersionCode = NullableIntUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_last_run_version_code_title)
    },
    description = null,
    key = stringPreferencesKey("intro_shown_for_version_code"),
    loading = null,
    default = 0,
)

val lastInputVersionCode = NullableIntUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_changelog_shown_for_version_code_title)
    },
    description = null,
    key = stringPreferencesKey("changelog_shown_for_version_code"),
    loading = null,
    default = 22,
    modifier = Modifier.testTag("geoShareUserPreferenceLastInputVersionCode"),
)

data class UserPreferencesValues(
    var automaticActionValue: AutomaticAction = automaticAction.loading,
    var connectionPermissionValue: Permission = connectionPermission.loading,
    var introShownForVersionCodeValue: Int? = lastRunVersionCode.loading,
    var lastInputVersionCodeValue: Int? = lastInputVersionCode.loading,
)
