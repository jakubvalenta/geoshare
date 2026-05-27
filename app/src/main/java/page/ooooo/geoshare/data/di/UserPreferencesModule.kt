package page.ooooo.geoshare.data.di

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import page.ooooo.geoshare.data.DefaultUserPreferencesRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.CachedApiTokenPreference
import page.ooooo.geoshare.data.local.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.local.preferences.ChangelogShownForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.CoordinateFormatPreference
import page.ooooo.geoshare.data.local.preferences.HiddenAppsPreference
import page.ooooo.geoshare.data.local.preferences.IntroShowForVersionCodePreference
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.Log
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface UserPreferencesModule {

    @Singleton
    @Binds
    fun bindUserPreferencesRepository(
        userPreferencesRepository: DefaultUserPreferencesRepository,
    ): UserPreferencesRepository
}

class FakeUserPreferencesRepository(
    initialValues: UserPreferencesValues = defaultFakeUserPreferences,
    val log: Log = FakeLog,
) : UserPreferencesRepository {
    private val _preferences: MutableStateFlow<Preferences> = MutableStateFlow(preferencesOf())

    override val values = _preferences.map {
        UserPreferencesValues(
            automation = AutomationPreference.getValue(it),
            automationDelay = AutomationDelayPreference.getValue(it),
            cachedApiToken = CachedApiTokenPreference.getValue(it),
            cachedPurchase = CachedPurchasePreference.getValue(it),
            changelogShownForVersionCode = ChangelogShownForVersionCodePreference.getValue(it),
            connectionPermission = ConnectionPermissionPreference.getValue(it),
            coordinateFormat = CoordinateFormatPreference.getValue(it),
            hiddenApps = HiddenAppsPreference.getValue(it),
            introShownForVersionCode = IntroShowForVersionCodePreference.getValue(it),
        )
    }

    init {
        _preferences.value = _preferences.value.toMutablePreferences().apply {
            AutomationPreference.setValue(this, initialValues.automation)
            AutomationDelayPreference.setValue(this, initialValues.automationDelay)
            CachedApiTokenPreference.setValue(this, initialValues.cachedApiToken)
            CachedPurchasePreference.setValue(this, initialValues.cachedPurchase)
            ChangelogShownForVersionCodePreference.setValue(this, initialValues.changelogShownForVersionCode)
            ConnectionPermissionPreference.setValue(this, initialValues.connectionPermission)
            CoordinateFormatPreference.setValue(this, initialValues.coordinateFormat)
            HiddenAppsPreference.setValue(this, initialValues.hiddenApps)
            IntroShowForVersionCodePreference.setValue(this, initialValues.introShownForVersionCode)
        }
    }

    override suspend fun <T> getValue(userPreference: UserPreference<T>) =
        userPreference.getValue(values.first())

    override suspend fun <T> setValue(userPreference: UserPreference<T>, value: T) {
        edit { userPreference.setValue(it, value, log) }
    }

    override suspend fun edit(transform: (preferences: MutablePreferences) -> Unit) {
        _preferences.value = _preferences.value.toMutablePreferences().apply {
            transform(this)
        }
    }
}

val defaultFakeUserPreferences = UserPreferencesValues(
    automation = NoopAutomation,
    automationDelay = 5.seconds,
    cachedApiToken = null,
    cachedPurchase = null,
    changelogShownForVersionCode = 22,
    connectionPermission = Permission.ALWAYS,
    coordinateFormat = CoordinateFormat.DEC,
    hiddenApps = emptySet(),
    introShownForVersionCode = 0,
)
