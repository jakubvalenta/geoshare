package page.ooooo.geoshare.data.local.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.DataTypes
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

interface UserPreference<T> {
    fun getValue(values: UserPreferencesValues): T
    fun getValue(preferences: Preferences, log: ILog = DefaultLog): T
    fun setValue(preferences: MutablePreferences, value: T, log: ILog = DefaultLog)
}

interface TextPreference<T> : UserPreference<T> {
    val key: Preferences.Key<String>
    val default: T

    fun serialize(value: T, log: ILog = DefaultLog): String

    fun deserialize(value: String?, log: ILog = DefaultLog): T

    fun isValid(value: String?): Boolean = true

    override fun getValue(preferences: Preferences, log: ILog): T =
        deserialize(preferences[key], log)

    override fun setValue(preferences: MutablePreferences, value: T, log: ILog) =
        preferences.set(key, serialize(value, log))
}

interface NullableIntPreference : TextPreference<Int?> {
    override fun serialize(value: Int?, log: ILog) =
        value.toString()

    override fun deserialize(value: String?, log: ILog) =
        value?.toIntOrNull() ?: default
}

interface DurationPreference : TextPreference<Duration> {
    val minSec: Int
    val maxSec: Int

    override fun serialize(value: Duration, log: ILog) =
        value.toInt(DurationUnit.SECONDS).toString()

    override fun deserialize(value: String?, log: ILog) =
        value?.toIntOrNull()?.coerceIn(minSec, maxSec)?.seconds ?: default

    override fun isValid(value: String?) =
        value?.toIntOrNull()?.let { it in minSec..maxSec } == true
}

interface OptionsPreference<T> : UserPreference<T> {
    val default: T
}

object ConnectionPermissionPreference : OptionsPreference<Permission> {
    private val key = stringPreferencesKey("connect_to_google_permission")
    override val default = Permission.ASK
    val loading = default

    override fun getValue(values: UserPreferencesValues) = values.connectionPermission

    override fun getValue(preferences: Preferences, log: ILog) = preferences[key]?.let {
        try {
            Permission.valueOf(it)
        } catch (_: IllegalArgumentException) {
            null
        }
    } ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission, log: ILog) {
        preferences[key] = value.name
    }

    fun getOptionGroups(): List<List<Permission>> = listOf(
        listOf(
            Permission.ALWAYS,
            Permission.ASK,
            Permission.NEVER,
        ),
    )
}

object CoordinateFormatPreference : OptionsPreference<CoordinateFormat> {
    private val key = stringPreferencesKey("coordinate_format")
    override val default = CoordinateFormat.DEC
    val loading = default

    override fun getValue(values: UserPreferencesValues) = values.coordinateFormat

    override fun getValue(preferences: Preferences, log: ILog) = preferences[key]?.let {
        try {
            CoordinateFormat.valueOf(it)
        } catch (_: IllegalArgumentException) {
            null
        }
    } ?: default

    override fun setValue(preferences: MutablePreferences, value: CoordinateFormat, log: ILog) {
        preferences[key] = value.name
    }

    fun getOptionGroups(): List<List<CoordinateFormat>> = listOf(
        listOf(
            CoordinateFormat.DEC,
            CoordinateFormat.DEG_MIN_SEC,
        ),
    )
}

object AutomationPreference : OptionsPreference<Automation> {
    private val key = stringPreferencesKey("automation")
    override val default = NoopAutomation
    val loading = default
    private const val TAG = "Automation"

    /**
     * Instance of [Json] for serialization.
     *
     * It's configured in such a way that it allows deserializing an old string after new properties have been added to
     * a class. So that we can update the [Automation] classes and users don't lose their preferences.
     */
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    override fun getValue(values: UserPreferencesValues) = values.automation

    override fun getValue(preferences: Preferences, log: ILog) =
        getValueOrNull(preferences, log)
            ?: @Suppress("DEPRECATION") getOldValueOrNull(preferences, log)
            // Silently ignore serialization errors, because they should never happen, because we test all automations
            // in unit tests.
            ?: default

    /**
     * Get [Automation] from [preferences] by deserializing a JSON stored in a single string key.
     */
    private fun getValueOrNull(preferences: Preferences, log: ILog): Automation? {
        val serializedString = preferences[key] ?: return NoopAutomation
        return try {
            json.decodeFromString<Automation>(serializedString)
        } catch (tr: IllegalArgumentException) {
            log.e(TAG, "Deserialization error", tr)
            null
        }
    }

    /**
     * Get [Automation] from [preferences] by reading automation type and package name from two different string keys.
     *
     * This is an old way of storing automation in preferences, which we keep for backward compatibility. So that users
     * of old app versions don't lose their automation preference after upgrading the app.
     */
    @Deprecated("Replaced with getValueOrNull")
    private fun getOldValueOrNull(preferences: Preferences, log: ILog): Automation? {
        val type = preferences[key] ?: return NoopAutomation
        val oldPackageNameKey = stringPreferencesKey("automation_package_name")
        val packageName = preferences[oldPackageNameKey]
        val serializedString = json.encodeToString(
            @Suppress("DEPRECATION")
            OldData(type = type, packageName = packageName)
        )
        return try {
            json.decodeFromString<Automation>(serializedString)
        } catch (tr: IllegalArgumentException) {
            log.e(TAG, "Deserialization error", tr)
            null
        }
    }

    @Deprecated("Replaced with getValueOrNull")
    @Serializable
    private data class OldData(val type: String?, val packageName: String?)

    override fun setValue(preferences: MutablePreferences, value: Automation, log: ILog) {
        val serializedString = try {
            json.encodeToString(value)
        } catch (tr: SerializationException) {
            // Silently ignore serialization errors, because they should never happen, because we test all automations
            // in unit tests.
            log.e(TAG, "Serialization error", tr)
            return
        }
        preferences[key] = serializedString
    }

    fun getOptionGroups(
        apps: DataTypes,
        appDetails: AppDetails,
        hiddenApps: Set<String>?,
        links: List<Link>,
    ): List<List<Automation>> = listOfNotNull(
        listOf(
            NoopAutomation,
        ),
        listOf(
            CopyCoordsDecAutomation,
            CopyCoordsDegMinSecAutomation,
            CopyGeoUriAutomation,
            ShareDisplayGeoUriAutomation,
            ShareNavigationGoogleUriAutomation,
            ShareStreetViewGoogleUriAutomation,
            SavePointGpxAutomation,
        ),
        listOf(
            ShareRouteGpxAutomation,
            SharePointsGpxAutomation,
            SaveRouteGpxAutomation,
            SavePointsGpxAutomation,
        ),
        apps
            .filterKeys { hiddenApps?.contains(it) != true }
            .toSortedMap(compareBy(nullsLast()) { packageName -> appDetails[packageName]?.label })
            .flatMap { (packageName, dataTypes) ->
                buildList {
                    if (DataType.GEO_URI in dataTypes) {
                        add(OpenDisplayGeoUriAutomation(packageName))
                    }
                    if (DataType.MAGIC_EARTH_URI in dataTypes) {
                        add(OpenDisplayMagicEarthUriAutomation(packageName))
                        add(OpenNavigationMagicEarthUriAutomation(packageName))
                    }
                    if (DataType.GOOGLE_NAVIGATION_URI in dataTypes) {
                        add(OpenNavigationGoogleUriAutomation(packageName))
                    }
                    if (DataType.GOOGLE_STREET_VIEW_URI in dataTypes) {
                        add(OpenStreetViewGoogleUriAutomation(packageName))
                    }
                    if (DataType.GPX_DATA in dataTypes) {
                        add(OpenRouteGpxAutomation(packageName))
                        add(OpenPointsGpxAutomation(packageName))
                    }
                    if (DataType.GPX_ONE_POINT_DATA in dataTypes) {
                        add(OpenRouteOnePointGpxAutomation(packageName))
                    }
                }
            }
            .takeIf { it.isNotEmpty() },
        links
            .groupBy { it.groupOrName }
            .toSortedMap()
            .flatMap { (_, links) ->
                listOf(
                    *links.map { ShareLinkUriAutomation(it.uuid) }.toTypedArray(),
                    *links.map { CopyLinkUriAutomation(it.uuid) }.toTypedArray(),
                )
            }
            .takeIf { it.isNotEmpty() },
    )
}

object AutomationDelayPreference : DurationPreference {
    override val key = stringPreferencesKey("automation_delay")
    override val default = 5.seconds
    val loading = default

    override val minSec = 0
    override val maxSec = 60

    override fun getValue(values: UserPreferencesValues) = values.automationDelay
}

object CachedPurchasePreference : TextPreference<CachedPurchase?> {
    override val key = stringPreferencesKey("cached_purchase")
    override val default = null
    val loading = default

    override fun serialize(value: CachedPurchase?, log: ILog) =
        try {
            Json.encodeToString(value)
        } catch (tr: SerializationException) {
            // Silently ignore serialization errors, because the value should always serialize
            log.e(TAG, "Serialization error", tr)
            ""
        }

    override fun deserialize(value: String?, log: ILog) =
        value?.let {
            try {
                Json.decodeFromString<CachedPurchase?>(it)
            } catch (tr: IllegalArgumentException) {
                log.e(TAG, "Deserialization error", tr)
                null
            }
        }

    override fun getValue(values: UserPreferencesValues) = values.cachedPurchase

    private const val TAG = "BillingCachedProductPreference"
}

/**
 * A set of strings stored as a JSON array.
 */
interface SetPreference : TextPreference<Set<String>?> {
    override val key: Preferences.Key<String>
    override val default: Set<String>?

    override fun serialize(value: Set<String>?, log: ILog) =
        try {
            Json.encodeToString(value)
        } catch (tr: SerializationException) {
            // Silently ignore serialization errors, because the value should always serialize
            log.e(TAG, "Serialization error", tr)
            ""
        }

    override fun deserialize(value: String?, log: ILog) =
        value?.let {
            try {
                Json.decodeFromString<Set<String>?>(it)
            } catch (tr: IllegalArgumentException) {
                log.e(TAG, "Deserialization error", tr)
                null
            }
        }

    companion object {
        private const val TAG = "SetPreference"
    }
}

object HiddenAppsPreference : SetPreference {
    override val key = stringPreferencesKey("hidden_apps")
    override val default: Set<String> = emptySet()
    val loading = null

    override fun getValue(values: UserPreferencesValues) = values.hiddenApps

    fun getOptions(apps: DataTypes): Set<String> = apps.keys
}

object IntroShowForVersionCodePreference : NullableIntPreference {
    override val key = stringPreferencesKey("intro_shown_for_version_code")
    override val default = 0
    val loading = null

    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCode
}

object ChangelogShownForVersionCodePreference : NullableIntPreference {
    override val key = stringPreferencesKey("changelog_shown_for_version_code")
    override val default = BuildConfig.VERSION_CODE
    val loading = null

    override fun getValue(values: UserPreferencesValues) = values.changelogShownForVersionCode
}

data class UserPreferencesValues(
    val automation: Automation = AutomationPreference.loading,
    val automationDelay: Duration = AutomationDelayPreference.loading,
    val cachedPurchase: CachedPurchase? = CachedPurchasePreference.loading,
    val changelogShownForVersionCode: Int? = ChangelogShownForVersionCodePreference.loading,
    val connectionPermission: Permission = ConnectionPermissionPreference.loading,
    val coordinateFormat: CoordinateFormat = CoordinateFormatPreference.loading,
    val hiddenApps: Set<String>? = HiddenAppsPreference.loading,
    val introShownForVersionCode: Int? = IntroShowForVersionCodePreference.loading,
)
