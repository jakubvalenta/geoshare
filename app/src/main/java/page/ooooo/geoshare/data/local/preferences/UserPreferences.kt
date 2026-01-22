package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.billing.FeatureStatus
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.findAutomation
import page.ooooo.geoshare.lib.outputs.getAutomations
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.RadioButtonGroup
import page.ooooo.geoshare.ui.components.RadioButtonOption
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

interface UserPreference<T> {
    val loading: T

    fun getValue(values: UserPreferencesValues): T
    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun title(): String

    @Composable
    fun Description(enabled: Boolean = true) {
    }

    @Composable
    fun suffix(): String? = null

    @Composable
    fun ValueLabel(values: UserPreferencesValues, featureStatus: FeatureStatus)

    @Composable
    fun Component(
        values: UserPreferencesValues,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
        enabled: Boolean = true,
        featureStatus: FeatureStatus,
    )
}

abstract class SingleKeyPreference<T>() : UserPreference<T> {
    abstract val key: Preferences.Key<String>
    abstract val default: T

    open val modifier: Modifier = Modifier

    protected abstract fun serialize(value: T): String

    protected abstract fun deserialize(value: String?): T

    override fun getValue(preferences: Preferences): T = deserialize(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: T) = preferences.set(key, serialize(value))

    @Composable
    override fun ValueLabel(values: UserPreferencesValues, featureStatus: FeatureStatus) {
        val value = getValue(values)
        Text(serialize(value ?: default))
    }

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
        enabled: Boolean,
        featureStatus: FeatureStatus,
    ) {
        val value = getValue(values)
        val spacing = LocalSpacing.current
        val (inputValue, setInputValue) = remember { mutableStateOf(serialize(value)) }
        val error = getError(inputValue)

        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                setInputValue(it)
                onValueChange { preferences -> setValue(preferences, deserialize(it)) }
            },
            modifier = modifier.padding(top = spacing.tinyAdaptive),
            enabled = enabled,
            suffix = suffix()?.let { text ->
                {
                    Text(
                        text,
                        color = if (error == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            },
            trailingIcon = {
                IconButton({
                    setInputValue(serialize(default))
                    onValueChange { preferences -> setValue(preferences, default) }
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        stringResource(R.string.reset),
                    )
                }
            },
            supportingText = error?.let { error ->
                {
                    Text(error)
                }
            },
            isError = error != null,
            singleLine = true,
        )
    }

    @Composable
    open fun getError(value: String?): String? = null
}

abstract class NullableIntPreference : SingleKeyPreference<Int?>() {
    override val loading = null

    override fun serialize(value: Int?) = value.toString()

    override fun deserialize(value: String?) = value?.toIntOrNull() ?: default
}

abstract class DurationPreference : SingleKeyPreference<Duration>() {
    open val minValue = Int.MIN_VALUE
    open val maxValue = Int.MAX_VALUE

    override fun serialize(value: Duration) = value.toInt(DurationUnit.SECONDS).toString()

    override fun deserialize(value: String?) =
        value?.toIntOrNull()?.coerceIn(minValue, maxValue)?.seconds ?: default

    @Composable
    override fun suffix() = stringResource(R.string.seconds_unit)

    @Composable
    override fun ValueLabel(values: UserPreferencesValues, featureStatus: FeatureStatus) {
        if (values.automation is Automation.HasDelay) {
            val seconds = getValue(values).toInt(DurationUnit.SECONDS)
            Text(pluralStringResource(R.plurals.seconds, seconds, seconds))
        }
    }

    @Composable
    override fun getError(value: String?) = if (value?.toIntOrNull()?.let { it in minValue..maxValue } == true) {
        null
    } else {
        stringResource(R.string.user_preferences_number_error_range, minValue, maxValue)
    }
}

data class PreferenceOption<T>(
    val value: T,
    val modifier: Modifier = Modifier,
    val label: @Composable () -> Unit,
)

abstract class OptionsPreference<T> : UserPreference<T> {
    abstract val default: T

    @Composable
    abstract fun options(): List<PreferenceOption<T>>

    @Composable
    override fun ValueLabel(values: UserPreferencesValues, featureStatus: FeatureStatus) {
        val value = getValue(values)
        val option = if (featureStatus == FeatureStatus.NOT_AVAILABLE) {
            options().find { it.value == default }
        } else {
            options().find { it.value == value }
                ?: options().find { it.value == default }
        }
        if (option != null) {
            option.label()
        } else {
            Text(value.toString())
        }
    }

    @Composable
    override fun Component(
        values: UserPreferencesValues,
        onValueChange: (transform: (MutablePreferences) -> Unit) -> Unit,
        enabled: Boolean,
        featureStatus: FeatureStatus,
    ) {
        val value = if (featureStatus == FeatureStatus.AVAILABLE) {
            getValue(values)
        } else {
            default
        }
        val spacing = LocalSpacing.current
        RadioButtonGroup(
            selectedValue = value,
            onSelect = {
                onValueChange { preferences ->
                    setValue(preferences, it)
                }
            },
            modifier = Modifier.padding(top = spacing.tinyAdaptive),
            enabled = enabled,
        ) {
            options().map { option -> RadioButtonOption(option.value, option.modifier, option.label) }
        }
    }
}

object ConnectionPermissionPreference : OptionsPreference<Permission>() {
    override val default = Permission.ASK
    override val loading = default

    private val key = stringPreferencesKey("connect_to_google_permission")

    @Composable
    override fun options() = listOf(
        PreferenceOption(
            Permission.ALWAYS,
            Modifier.testTag("geoShareUserPreferenceConnectionPermissionAlways"),
        ) {
            Text(stringResource(R.string.yes))
        },
        PreferenceOption(Permission.ASK) {
            Text(stringResource(R.string.user_preferences_connection_option_ask))
        },
        PreferenceOption(Permission.NEVER) {
            Text(stringResource(R.string.no))
        },
    )

    override fun getValue(values: UserPreferencesValues) = values.connectionPermission

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
    override fun Description(enabled: Boolean) {
        ParagraphHtml(
            stringResource(R.string.user_preferences_connection_description, stringResource(R.string.app_name)),
            if (enabled) Modifier else Modifier.alpha(0.7f),
        )
    }
}

object AutomationPreference : OptionsPreference<Automation>() {
    override val default = NoopAutomation
    override val loading = default

    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    @Composable
    override fun options(): List<PreferenceOption<Automation>> {
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
                Automation.Type.COPY_APPLE_MAPS_NAVIGATE_TO_URI -> 10
                Automation.Type.COPY_GOOGLE_MAPS_URI -> 11
                Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI -> 12
                Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI -> 13
                Automation.Type.COPY_MAGIC_EARTH_URI -> 14
                Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI -> 15
                Automation.Type.SAVE_GPX -> 16
                Automation.Type.SHARE -> 17
                Automation.Type.SHARE_GPX_ROUTE -> 18
            }
        }.map { automation ->
            PreferenceOption(
                value = automation,
                modifier = automation.testTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) {
                automation.Label()
            }
        }
    }

    override fun getValue(values: UserPreferencesValues) = values.automation

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
    override fun Description(enabled: Boolean) {
        ParagraphHtml(
            stringResource(R.string.user_preferences_automation_description),
            if (enabled) Modifier else Modifier.alpha(0.7f),
        )
    }
}

object AutomationDelayPreference : DurationPreference() {
    override val key = stringPreferencesKey("automation_delay")
    override val default = 5.seconds
    override val loading = default
    override val modifier = Modifier
    override val minValue = 0
    override val maxValue = 60

    override fun getValue(values: UserPreferencesValues) = values.automationDelay

    @Composable
    override fun title() = stringResource(R.string.user_preferences_automation_delay_sec_title)

    @Composable
    override fun Description(enabled: Boolean) {
        ParagraphHtml(
            stringResource(R.string.user_preferences_automation_delay_sec_description),
            if (enabled) Modifier else Modifier.alpha(0.7f),
        )
    }
}

object BillingCachedProductIdPreference : SingleKeyPreference<String?>() {
    override val key = stringPreferencesKey("billing_product_id")
    override val default = ""
    override val loading = ""

    override fun serialize(value: String?) = value.orEmpty()

    override fun deserialize(value: String?) = value?.ifEmpty { null }

    override fun getValue(values: UserPreferencesValues) = values.billingCachedProductId

    @Composable
    override fun title() = stringResource(R.string.user_preferences_billing_cached_product_id)
}

object IntroShowForVersionCodePreference : NullableIntPreference() {
    override val key = stringPreferencesKey("intro_shown_for_version_code")
    override val default = 0
    override val modifier = Modifier

    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCode

    @Composable
    override fun title() = stringResource(R.string.user_preferences_last_run_version_code_title)
}

object ChangelogShownForVersionCodePreference : NullableIntPreference() {
    override val key = stringPreferencesKey("changelog_shown_for_version_code")
    override val default = BuildConfig.VERSION_CODE
    override val modifier = Modifier.testTag("geoShareUserPreferenceChangelogShownForVersionCode")

    override fun getValue(values: UserPreferencesValues) = values.changelogShownForVersionCode

    @Composable
    override fun title() = stringResource(R.string.user_preferences_changelog_shown_for_version_code_title)
}

data class UserPreferencesValues(
    val automation: Automation = AutomationPreference.loading,
    val automationDelay: Duration = AutomationDelayPreference.loading,
    val billingCachedProductId: String? = BillingCachedProductIdPreference.loading,
    val changelogShownForVersionCode: Int? = ChangelogShownForVersionCodePreference.loading,
    val connectionPermission: Permission = ConnectionPermissionPreference.loading,
    val introShownForVersionCode: Int? = IntroShowForVersionCodePreference.loading,
)
