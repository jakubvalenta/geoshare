package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import page.ooooo.geoshare.R
import page.ooooo.geoshare.components.RadioButtonGroup
import page.ooooo.geoshare.components.RadioButtonOption
import page.ooooo.geoshare.ui.theme.Spacing

interface UserPreference<T> {
    val title: @Composable () -> String
    val description: (@Composable () -> String)?
    val key: Preferences.Key<String>
    val loading: T
    val default: T

    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun component(value: T, onValueChange: (T) -> Unit)
}

class NullableIntUserPreference(
    override val title: @Composable () -> String,
    override val description: (@Composable () -> String)?,
    override val key: Preferences.Key<String>,
    override val default: Int?,
    override val loading: Int?,
) : UserPreference<Int?> {
    override fun getValue(preferences: Preferences) =
        fromString(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: Int?) {
        preferences[key] = value.toString()
    }

    @Composable
    override fun component(value: Int?, onValueChange: (Int?) -> Unit) {
        var inputValue by remember { mutableStateOf(value.toString()) }
        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                inputValue = it
                onValueChange(fromString(it))
            },
            modifier = Modifier.padding(top = Spacing.tiny),
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
    override val key: Preferences.Key<String>,
    override val default: Permission,
    override val loading: Permission = default,
    val options: List<RadioButtonOption<Permission>>,
) : UserPreference<Permission> {
    override fun getValue(preferences: Preferences) =
        preferences[key]?.let(Permission::valueOf) ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission) {
        preferences[key] = value.name
    }

    @Composable
    override fun component(
        value: Permission,
        onValueChange: (Permission) -> Unit,
    ) {
        RadioButtonGroup<Permission>(
            options = options,
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        )
    }
}

val connectToGooglePermission = PermissionUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_connect_to_google_title)
    },
    description = @Composable {
        stringResource(
            R.string.user_preferences_connect_to_google_description,
            stringResource(R.string.app_name)
        )
    },
    key = stringPreferencesKey("connect_to_google_permission"),
    default = Permission.ASK,
    options = listOf(
        RadioButtonOption(
            Permission.ALWAYS,
            @Composable {
                stringResource(R.string.user_preferences_connect_to_google_option_always)
            }
        ),
        RadioButtonOption(
            Permission.ASK,
            @Composable {
                stringResource(R.string.user_preferences_connect_to_google_option_ask)
            }
        ),
        RadioButtonOption(
            Permission.NEVER,
            @Composable {
                stringResource(R.string.user_preferences_connect_to_google_option_never)
            }
        ),
    ),
)

val lastRunVersionCode = NullableIntUserPreference(
    title = @Composable {
        stringResource(R.string.user_preferences_last_run_version_code_title)
    },
    description = null,
    key = stringPreferencesKey("intro_shown_for_version_code"),
    loading = null,
    default = 0,
)

data class UserPreferencesValues(
    var connectToGooglePermissionValue: Permission = connectToGooglePermission.loading,
    var introShownForVersionCodeValue: Int? = lastRunVersionCode.loading,
)
