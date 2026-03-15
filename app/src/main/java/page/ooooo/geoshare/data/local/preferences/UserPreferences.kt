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

interface TextUserPreference<T> : UserPreference<T> {
    val key: Preferences.Key<String>
    val default: T

    fun serialize(value: T): String

    fun deserialize(value: String?): T

    fun isValid(value: String?): Boolean = true

    override fun getValue(preferences: Preferences, log: ILog): T = deserialize(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: T, log: ILog) = preferences.set(key, serialize(value))
}

interface NullableIntPreference : TextUserPreference<Int?> {
    override fun serialize(value: Int?) = value.toString()

    override fun deserialize(value: String?) = value?.toIntOrNull() ?: default
}

interface DurationPreference : TextUserPreference<Duration> {
    val minSec: Int
    val maxSec: Int

    override fun serialize(value: Duration) = value.toInt(DurationUnit.SECONDS).toString()

    override fun deserialize(value: String?) = value?.toIntOrNull()?.coerceIn(minSec, maxSec)?.seconds ?: default

    override fun isValid(value: String?) = value?.toIntOrNull()?.let { it in minSec..maxSec } == true
}

interface OptionsPreference<T> : UserPreference<T> {
    val default: T
}

object ConnectionPermissionPreference : OptionsPreference<Permission> {
    override val default = Permission.ASK
    val loading = default

    private val key = stringPreferencesKey("connect_to_google_permission")

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
    override val default = CoordinateFormat.DEC
    val loading = default

    private val key = stringPreferencesKey("coordinate_format")

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
    private const val TAG = "Automation"

    override val default = NoopAutomation
    val loading = default

    private val key = stringPreferencesKey("automation")

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

    fun getOptionGroups(apps: DataTypes, appDetails: AppDetails, links: List<Link>): List<List<Automation>> = listOf(
        listOf(
            NoopAutomation,
        ),
        listOf(
            CopyCoordsDecAutomation,
            CopyCoordsDegMinSecAutomation,
            CopyGeoUriAutomation,
            ShareDisplayUriAutomation,
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
            },
        links
            .groupBy { it.groupOrName }
            .toSortedMap()
            .flatMap { (_, links) ->
                listOf(
                    *links.map { ShareLinkUriAutomation(it.uuid) }.toTypedArray(),
                    *links.map { CopyLinkUriAutomation(it.uuid) }.toTypedArray(),
                )
            },
    )
}

object AutomationDelayPreference : DurationPreference {
    override val default = 5.seconds
    val loading = default

    override val key = stringPreferencesKey("automation_delay")
    override val minSec = 0
    override val maxSec = 60

    override fun getValue(values: UserPreferencesValues) = values.automationDelay
}

object BillingCachedProductIdPreference : TextUserPreference<String?> {
    override val default = ""
    val loading = default

    override val key = stringPreferencesKey("billing_product_id")

    override fun serialize(value: String?) = value.orEmpty()

    override fun deserialize(value: String?) = value?.ifEmpty { null }

    override fun getValue(values: UserPreferencesValues) = values.billingCachedProductId
}

object IntroShowForVersionCodePreference : NullableIntPreference {
    val loading = null

    override val default = 0
    override val key = stringPreferencesKey("intro_shown_for_version_code")

    override fun getValue(values: UserPreferencesValues) = values.introShownForVersionCode
}

object ChangelogShownForVersionCodePreference : NullableIntPreference {
    val loading = null

    override val key = stringPreferencesKey("changelog_shown_for_version_code")
    override val default = BuildConfig.VERSION_CODE

    override fun getValue(values: UserPreferencesValues) = values.changelogShownForVersionCode
}

data class UserPreferencesValues(
    val automation: Automation = AutomationPreference.loading,
    val automationDelay: Duration = AutomationDelayPreference.loading,
    val billingCachedProductId: String? = BillingCachedProductIdPreference.loading,
    val changelogShownForVersionCode: Int? = ChangelogShownForVersionCodePreference.loading,
    val connectionPermission: Permission = ConnectionPermissionPreference.loading,
    val coordinateFormat: CoordinateFormat = CoordinateFormatPreference.loading,
    val introShownForVersionCode: Int? = IntroShowForVersionCodePreference.loading,
)
