package page.ooooo.geoshare.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import page.ooooo.geoshare.data.DefaultUserPreferencesRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomaticAction
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindUserPreferencesRepository(
        userPreferencesRepository: DefaultUserPreferencesRepository,
    ): UserPreferencesRepository
}

class FakeUserPreferencesRepository @Inject constructor() :
    UserPreferencesRepository {
    override val values: Flow<UserPreferencesValues> =
        flowOf(defaultFakeUserPreferences)

    override suspend fun <T> getValue(userPreference: UserPreference<T>): T {
        throw NotImplementedError()
    }

    override suspend fun <T> setValue(
        userPreference: UserPreference<T>,
        value: T,
    ) {
        throw NotImplementedError()
    }
}

val defaultFakeUserPreferences = UserPreferencesValues(
    automaticActionValue = AutomaticAction(AutomaticAction.Type.NONE),
    changelogShownForVersionCodeValue = 22,
    connectionPermissionValue = Permission.ALWAYS,
    introShownForVersionCodeValue = 0,
)
