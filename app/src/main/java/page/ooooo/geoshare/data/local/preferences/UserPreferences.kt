package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.findAutomation
import page.ooooo.geoshare.lib.outputs.getAutomations
import page.ooooo.geoshare.ui.UserPreferencesGroupId
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
    fun Component(
        values: UserPreferencesValues,
        onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
    )
}

abstract class NumberUserPreference<T> : UserPreference<T> {
    abstract val key: Preferences.Key<String>
    abstract val default: T
    abstract val modifier: Modifier
    abstract val minValue: T
    abstract val maxValue: T

    override fun getValue(preferences: Preferences): T = fromString(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: T) {
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
        onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
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

    protected abstract fun fromString(value: String?): T
}

abstract class IntUserPreference : NumberUserPreference<Int>() {
    override val loading = default
    override val minValue = Int.MIN_VALUE
    override val maxValue = Int.MAX_VALUE

    override fun fromString(value: String?): Int = try {
        value?.toInt()?.coerceIn(minValue, maxValue)
    } catch (_: NumberFormatException) {
        null
    } ?: default
}

abstract class NullableIntUserPreference : NumberUserPreference<Int?>() {
    override val loading = null
    override val minValue = Int.MIN_VALUE
    override val maxValue = Int.MAX_VALUE

    override fun fromString(value: String?): Int? = try {
        value?.toInt()?.coerceIn(minValue, maxValue)
    } catch (_: NumberFormatException) {
        null
    } ?: default
}

data class UserPreferenceOption<T>(
    val value: T,
    val modifier: Modifier = Modifier,
    val label: @Composable (selected: Boolean) -> Unit,
)

abstract class OptionsUserPreference<T>(
    val default: T,
) : UserPreference<T> {
    override val loading = default

    @Composable
    abstract fun options(onNavigateToGroup: ((id: UserPreferencesGroupId) -> Unit)? = null): List<UserPreferenceOption<T>>

    @Composable
    override fun ValueLabel(values: UserPreferencesValues) {
        val value = getValue(values)
        (options().find { it.value == value } ?: options().find { it.value == default })?.also { option ->
            option.label(true)
        } ?: Text(value.toString())
    }

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
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
            options(onNavigateToGroup).map { option -> RadioButtonOption(option.value, option.modifier, option.label) }
        }
    }
}

object ConnectionPermission : OptionsUserPreference<Permission>(
    default = Permission.ASK,
) {
    private val key = stringPreferencesKey("connect_to_google_permission")

    @Composable
    override fun options(onNavigateToGroup: ((id: UserPreferencesGroupId) -> Unit)?) = listOf(
        UserPreferenceOption(
            Permission.ALWAYS,
            Modifier.testTag("geoShareUserPreferenceConnectionPermissionAlways"),
        ) {
            Text(stringResource(R.string.yes))
        },
        UserPreferenceOption(Permission.ASK) {
            Text(stringResource(R.string.user_preferences_connection_option_ask))
        },
        UserPreferenceOption(Permission.NEVER) {
            Text(stringResource(R.string.no))
        },
    )

    override fun getValue(values: UserPreferencesValues) = values.connectionPermissionValue

    override fun getValue(preferences: Preferences) = preferences[key]?.let {
        try {
            Permission.valueOf(it)
        } catch (_: IllegalArgumentException) {
            null
        }
    } ?: default

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
    default = NoopAutomation,
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    @Composable
    override fun options(onNavigateToGroup: ((id: UserPreferencesGroupId) -> Unit)?): List<UserPreferenceOption<Automation>> {
        val context = LocalContext.current
        val apps = AndroidTools.queryApps(context.packageManager)
        return buildList {
            add(NoopAutomation)
            addAll(allOutputs.getAutomations(apps))
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
                Automation.Type.OPEN_APP_GPX_ROUTE -> 8
                Automation.Type.COPY_APPLE_MAPS_URI -> 9
                Automation.Type.COPY_GOOGLE_MAPS_URI -> 10
                Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI -> 11
                Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI -> 12
                Automation.Type.COPY_MAGIC_EARTH_URI -> 13
                Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI -> 14
                Automation.Type.SAVE_GPX -> 15
                Automation.Type.SHARE -> 16
                Automation.Type.SHARE_GPX_ROUTE -> 17
            }
        }.map { automation ->
            UserPreferenceOption(
                value = automation,
                modifier = automation.testTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) { selected ->
                if (onNavigateToGroup == null || !selected || automation !is Automation.HasDelay) {
                    automation.Label()
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        automation.Label()
                        Text(
                            stringResource(R.string.user_preferences_automation_delay_sec_button),
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    MaterialTheme.shapes.extraLarge,
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable(role = Role.Button) {
                                    onNavigateToGroup(UserPreferencesGroupId.AUTOMATION_DELAY)
                                },
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }

    override fun getValue(values: UserPreferencesValues) = values.automationValue

    override fun getValue(preferences: Preferences): Automation =
        preferences[typeKey]?.let {
            try {
                Automation.Type.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }?.let { type ->
            preferences[packageNameKey]?.ifEmpty { null }.let { packageName ->
                allOutputs.findAutomation(type, packageName)
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

object AutomationDelaySec : IntUserPreference() {
    override val key = stringPreferencesKey("automation_delay")
    override val default = 5
    override val modifier = Modifier

    override fun getValue(values: UserPreferencesValues) = values.automationDelaySecValue

    @Composable
    override fun title() = stringResource(R.string.user_preferences_automation_delay_sec_title)

    @Composable
    override fun description() = stringResource(R.string.user_preferences_automation_delay_sec_description)

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onNavigateToGroup: (id: UserPreferencesGroupId) -> Unit,
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
}

object IntroShowForVersionCode : NullableIntUserPreference() {
    override val key = stringPreferencesKey("intro_shown_for_version_code")
    override val default = 0
    override val modifier = Modifier

    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCodeValue

    @Composable
    override fun title() = stringResource(R.string.user_preferences_last_run_version_code_title)

    @Composable
    override fun description() = null
}

object ChangelogShownForVersionCode : NullableIntUserPreference() {
    override val key = stringPreferencesKey("changelog_shown_for_version_code")
    override val default = BuildConfig.VERSION_CODE
    override val modifier = Modifier.testTag("geoShareUserPreferenceChangelogShownForVersionCode")

    override fun getValue(values: UserPreferencesValues) = values.changelogShownForVersionCodeValue

    @Composable
    override fun title() = stringResource(R.string.user_preferences_changelog_shown_for_version_code_title)

    @Composable
    override fun description() = null
}

data class UserPreferencesValues(
    val automationValue: Automation = AutomationUserPreference.loading,
    val automationDelaySecValue: Int = AutomationDelaySec.loading,
    val changelogShownForVersionCodeValue: Int? = ChangelogShownForVersionCode.loading,
    val connectionPermissionValue: Permission = ConnectionPermission.loading,
    val introShownForVersionCodeValue: Int? = IntroShowForVersionCode.loading,
)
