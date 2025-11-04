package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.outputs.Outputs
import page.ooooo.geoshare.ui.components.RadioButtonGroup
import page.ooooo.geoshare.ui.components.RadioButtonOption
import page.ooooo.geoshare.ui.theme.LocalSpacing

interface UserPreference<T> {
    val loading: T

    fun getValue(values: UserPreferencesValues): T
    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun title(): String

    @Composable
    fun description(): String?

    @Composable
    fun ValueLabel(values: UserPreferencesValues)

    @Composable
    fun Component(values: UserPreferencesValues, onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit)
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
    override fun ValueLabel(values: UserPreferencesValues) {
        val value = getValue(values)
        Text((value ?: default).toString())
    }

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
    ) {
        val value = getValue(values)
        val spacing = LocalSpacing.current
        var inputValue by remember { mutableStateOf(value.toString()) }
        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                @Suppress("AssignedValueIsNeverRead")
                inputValue = it
                onValueChange { preferences ->
                    setValue(preferences, fromString(it))
                }
            },
            modifier = modifier.padding(top = spacing.tiny),
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
    override fun ValueLabel(values: UserPreferencesValues) {
        val value = getValue(values)
        (options().find { it.value == value } ?: options().find { it.value == default })?.also { option ->
            option.label()
        } ?: Text(value.toString())
    }

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
    ) {
        val value = getValue(values)
        val spacing = LocalSpacing.current
        RadioButtonGroup(
            selectedValue = value,
            onSelect = {
                onValueChange { preferences ->
                    setValue(preferences, it)
                }
            },
            modifier = Modifier.padding(top = spacing.tiny),
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

    override fun getValue(values: UserPreferencesValues) = values.connectionPermissionValue

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
    default = Automation.Noop,
    options = {
        val context = LocalContext.current
        buildList {
            add(Automation.Noop)
            addAll(Outputs.getAutomations(context))
        }.map { automation ->
            UserPreferenceOption(
                value = automation,
                modifier = automation.testTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) {
                automation.Label()
            }
        }
    }
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    override fun getValue(values: UserPreferencesValues) = values.automationValue

    override fun getValue(preferences: Preferences): Automation =
        preferences[typeKey]?.let(Automation.Type::valueOf)?.let { type ->
            preferences[packageNameKey]?.ifEmpty { null }.let { packageName ->
                Outputs.findAutomation(type, packageName)
            }
        } ?: default

    override fun setValue(preferences: MutablePreferences, value: Automation) {
        preferences[typeKey] = value.type.name
        preferences[packageNameKey] = value.packageName
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
    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCodeValue

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
    override fun getValue(values: UserPreferencesValues) = values.changelogShownForVersionCodeValue

    @Composable
    override fun title() = stringResource(R.string.user_preferences_changelog_shown_for_version_code_title)

    @Composable
    override fun description() = null
}

data class UserPreferencesValues(
    val automationValue: Automation = automation.loading,
    val changelogShownForVersionCodeValue: Int? = changelogShownForVersionCode.loading,
    val connectionPermissionValue: Permission = connectionPermission.loading,
    val introShownForVersionCodeValue: Int? = introShowForVersionCode.loading,
)
