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
import page.ooooo.geoshare.components.RadioButtonGroup
import page.ooooo.geoshare.components.RadioButtonOption
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.ui.theme.Spacing

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

private enum class AutomationType {
    COPY_APPLE_MAPS_URI,
    COPY_COORDS_DEC,
    COPY_COORDS_NSWE_DEC,
    COPY_GEO_URI,
    COPY_GOOGLE_MAPS_URI,
    COPY_MAGIC_EARTH_URI,
    NOOP,
    OPEN_APP,
    SAVE_GPX,
    SHARE,
}

val automation = object : OptionsUserPreference<AutomationImpl>(
    default = AutomationImpl.Noop(),
    options = {
        val context = LocalContext.current
        listOf(
            AutomationImpl.Noop() to Modifier,
            AutomationImpl.CopyCoordsDec() to Modifier.testTag("geoShareUserPreferenceAutomationCopyCoordsDec"),
            AutomationImpl.CopyCoordsNorthSouthWestEastDec() to Modifier,
            AutomationImpl.CopyGeoUri() to Modifier,
            AutomationImpl.CopyGoogleMapsUri() to Modifier,
            AutomationImpl.CopyAppleMapsUri() to Modifier,
            AutomationImpl.CopyMagicEarthUri() to Modifier,
            AutomationImpl.SaveGpx() to Modifier,
            AutomationImpl.Share() to Modifier,
            *IntentTools().queryGeoUriApps(context.packageManager)
                .map { app ->
                    AutomationImpl.OpenApp(app.packageName) to
                            Modifier.testTag("geoShareUserPreferenceAutomationOpenApp_${app.packageName}")
                }
                .toTypedArray(),
        ).map { (automation, modifier) ->
            UserPreferenceOption(automation, modifier) { automation.Label() }
        }
    }
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    override fun getValue(preferences: Preferences): AutomationImpl {
        val type = preferences[typeKey]?.let(AutomationType::valueOf) ?: return default
        return when (type) {
            AutomationType.COPY_APPLE_MAPS_URI -> AutomationImpl.CopyAppleMapsUri()
            AutomationType.COPY_COORDS_DEC -> AutomationImpl.CopyCoordsDec()
            AutomationType.COPY_COORDS_NSWE_DEC -> AutomationImpl.CopyCoordsNorthSouthWestEastDec()
            AutomationType.COPY_GEO_URI -> AutomationImpl.CopyGeoUri()
            AutomationType.COPY_GOOGLE_MAPS_URI -> AutomationImpl.CopyGoogleMapsUri()
            AutomationType.COPY_MAGIC_EARTH_URI -> AutomationImpl.CopyMagicEarthUri()
            AutomationType.NOOP -> AutomationImpl.Noop()
            AutomationType.OPEN_APP -> preferences[packageNameKey]?.takeIf { it.isNotEmpty() }?.let { packageName ->
                AutomationImpl.OpenApp(packageName)
            } ?: AutomationImpl.Noop()

            AutomationType.SAVE_GPX -> AutomationImpl.SaveGpx()
            AutomationType.SHARE -> AutomationImpl.Share()
        }
    }

    override fun setValue(preferences: MutablePreferences, value: AutomationImpl) {
        val (type, packageName) = when (value) {
            is AutomationImpl.CopyAppleMapsUri -> AutomationType.COPY_APPLE_MAPS_URI to ""
            is AutomationImpl.CopyCoordsDec -> AutomationType.COPY_COORDS_DEC to ""
            is AutomationImpl.CopyCoordsNorthSouthWestEastDec -> AutomationType.COPY_COORDS_NSWE_DEC to ""
            is AutomationImpl.CopyGeoUri -> AutomationType.COPY_GEO_URI to ""
            is AutomationImpl.CopyGoogleMapsUri -> AutomationType.COPY_GOOGLE_MAPS_URI to ""
            is AutomationImpl.CopyMagicEarthUri -> AutomationType.COPY_MAGIC_EARTH_URI to ""
            is AutomationImpl.Noop -> AutomationType.NOOP to ""
            is AutomationImpl.OpenApp -> AutomationType.OPEN_APP to value.packageName
            is AutomationImpl.SaveGpx -> AutomationType.SAVE_GPX to ""
            is AutomationImpl.Share -> AutomationType.SHARE to ""
        }
        preferences[typeKey] = type.name
        preferences[packageNameKey] = packageName
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
    var automationValue: AutomationImpl = automation.loading,
    var changelogShownForVersionCodeValue: Int? = changelogShownForVersionCode.loading,
    var connectionPermissionValue: Permission = connectionPermission.loading,
    var introShownForVersionCodeValue: Int? = introShowForVersionCode.loading,
)
