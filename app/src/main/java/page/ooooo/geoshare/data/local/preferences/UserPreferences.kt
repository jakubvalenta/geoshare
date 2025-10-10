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

val automation = object : OptionsUserPreference<AutomationImplementation>(
    default = AutomationImplementation.Noop(),
    options = {
        val context = LocalContext.current
        listOf(
            AutomationImplementation.Noop(),
            AutomationImplementation.CopyCoordsDec(),
            AutomationImplementation.CopyCoordsNorthSouthWestEastDec(),
            AutomationImplementation.CopyGeoUri(),
            AutomationImplementation.CopyGoogleMapsUri(),
            AutomationImplementation.CopyAppleMapsUri(),
            AutomationImplementation.CopyMagicEarthUri(),
            AutomationImplementation.SaveGpx(),
            AutomationImplementation.Share(),
            *IntentTools().queryGeoUriApps(context.packageManager)
                .map { app -> AutomationImplementation.OpenApp(app.packageName) }
                .toTypedArray(),
        ).map { automation -> UserPreferenceOption(automation) { automation.Label() } }
    }
) {
    private val typeKey = stringPreferencesKey("automation")
    private val packageNameKey = stringPreferencesKey("automation_package_name")

    override fun getValue(preferences: Preferences): AutomationImplementation {
        val type = preferences[typeKey]?.let(AutomationType::valueOf) ?: return default
        return when (type) {
            AutomationType.COPY_APPLE_MAPS_URI -> AutomationImplementation.CopyAppleMapsUri()
            AutomationType.COPY_COORDS_DEC -> AutomationImplementation.CopyCoordsDec()
            AutomationType.COPY_COORDS_NSWE_DEC -> AutomationImplementation.CopyCoordsNorthSouthWestEastDec()
            AutomationType.COPY_GEO_URI -> AutomationImplementation.CopyGeoUri()
            AutomationType.COPY_GOOGLE_MAPS_URI -> AutomationImplementation.CopyGoogleMapsUri()
            AutomationType.COPY_MAGIC_EARTH_URI -> AutomationImplementation.CopyMagicEarthUri()
            AutomationType.NOOP -> AutomationImplementation.Noop()
            AutomationType.OPEN_APP -> preferences[packageNameKey]?.takeIf { it.isNotEmpty() }?.let { packageName ->
                AutomationImplementation.OpenApp(packageName)
            } ?: AutomationImplementation.Noop()

            AutomationType.SAVE_GPX -> AutomationImplementation.SaveGpx()
            AutomationType.SHARE -> AutomationImplementation.Share()
        }
    }

    override fun setValue(preferences: MutablePreferences, value: AutomationImplementation) {
        val (type, packageName) = when (value) {
            is AutomationImplementation.CopyAppleMapsUri -> AutomationType.COPY_APPLE_MAPS_URI to ""
            is AutomationImplementation.CopyCoordsDec -> AutomationType.COPY_COORDS_DEC to ""
            is AutomationImplementation.CopyCoordsNorthSouthWestEastDec -> AutomationType.COPY_COORDS_NSWE_DEC to ""
            is AutomationImplementation.CopyGeoUri -> AutomationType.COPY_GEO_URI to ""
            is AutomationImplementation.CopyGoogleMapsUri -> AutomationType.COPY_GOOGLE_MAPS_URI to ""
            is AutomationImplementation.CopyMagicEarthUri -> AutomationType.COPY_MAGIC_EARTH_URI to ""
            is AutomationImplementation.Noop -> AutomationType.NOOP to ""
            is AutomationImplementation.OpenApp -> AutomationType.OPEN_APP to value.packageName
            is AutomationImplementation.SaveGpx -> AutomationType.SAVE_GPX to ""
            is AutomationImplementation.Share -> AutomationType.SHARE to ""
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
    var automationValue: AutomationImplementation = automation.loading,
    var connectionPermissionValue: Permission = connectionPermission.loading,
    var introShownForVersionCodeValue: Int? = introShowForVersionCode.loading,
    var changelogShownForVersionCodeValue: Int? = changelogShownForVersionCode.loading,
)
