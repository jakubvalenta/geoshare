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
import page.ooooo.geoshare.lib.DefaultIntentTools
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.allOutputGroups
import page.ooooo.geoshare.lib.outputs.findAutomation
import page.ooooo.geoshare.lib.outputs.getAutomations
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
) : UserPreference<T> {
    override val loading = default

    @Composable
    abstract fun options(): List<UserPreferenceOption<T>>

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

object ConnectionPermission : OptionsUserPreference<Permission>(
    default = Permission.ASK,
) {
    private val key = stringPreferencesKey("connect_to_google_permission")

    @Composable
    override fun options() = listOf(
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

object AutomationUserPreference : OptionsUserPreference<Automation>(
    default = Automation.Noop,
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    @Composable
    override fun options(): List<UserPreferenceOption<Automation>> {
        val context = LocalContext.current
        val packageNames = DefaultIntentTools.queryGeoUriPackageNames(context.packageManager)
        return buildList {
            add(Automation.Noop)
            addAll(allOutputGroups.getAutomations(packageNames))
        }.sortedBy { automation ->
            when (automation.type) {
                Automation.Type.NOOP -> 0
                Automation.Type.COPY_COORDS_DEC -> 1
                Automation.Type.COPY_COORDS_NSWE_DEC -> 2
                Automation.Type.COPY_GEO_URI -> 3
                Automation.Type.OPEN_APP -> 4
                Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO -> 5
                Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW -> 6
                Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_TO -> 7
                Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_VIA -> 8
                Automation.Type.COPY_APPLE_MAPS_URI -> 9
                Automation.Type.COPY_GOOGLE_MAPS_URI -> 10
                Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI -> 11
                Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI -> 12
                Automation.Type.COPY_MAGIC_EARTH_URI -> 13
                Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI -> 14
                Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_VIA_URI -> 15
                Automation.Type.SAVE_GPX -> 16
                Automation.Type.SHARE -> 17
            }
        }.map { automation ->
            UserPreferenceOption(
                value = automation,
                modifier = automation.testTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) {
                automation.Label()
            }
        }
    }

    override fun getValue(values: UserPreferencesValues) = values.automationValue

    override fun getValue(preferences: Preferences): Automation =
        preferences[typeKey]?.let(Automation.Type::valueOf)?.let { type ->
            preferences[packageNameKey]?.ifEmpty { null }.let { packageName ->
                allOutputGroups.findAutomation(type, packageName)
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

object IntroShowForVersionCode : NullableIntUserPreference(
    key = stringPreferencesKey("intro_shown_for_version_code"),
    default = 0,
) {
    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCodeValue

    @Composable
    override fun title() = stringResource(R.string.user_preferences_last_run_version_code_title)

    @Composable
    override fun description() = null
}

object ChangelogShownForVersionCode : NullableIntUserPreference(
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
    val automationValue: Automation = AutomationUserPreference.loading,
    val changelogShownForVersionCodeValue: Int? = ChangelogShownForVersionCode.loading,
    val connectionPermissionValue: Permission = ConnectionPermission.loading,
    val introShownForVersionCodeValue: Int? = IntroShowForVersionCode.loading,
)
