package page.ooooo.geoshare.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import page.ooooo.geoshare.data.local.preferences.*
import java.io.IOException
import javax.inject.Inject

interface UserPreferencesRepository {
    val values: Flow<UserPreferencesValues>

    suspend fun <T> getValue(userPreference: UserPreference<T>): T

    suspend fun <T> setValue(userPreference: UserPreference<T>, value: T)

    suspend fun edit(transform: (preferences: MutablePreferences) -> Unit)
}

class DefaultUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    override val values: Flow<UserPreferencesValues> =
        dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            UserPreferencesValues(
                automationValue = AutomationUserPreference.getValue(it),
                changelogShownForVersionCodeValue = ChangelogShownForVersionCode.getValue(it),
                connectionPermissionValue = ConnectionPermission.getValue(it),
                introShownForVersionCodeValue = IntroShowForVersionCode.getValue(it),
            )
        }

    override suspend fun <T> getValue(userPreference: UserPreference<T>): T =
        userPreference.getValue(dataStore.data.first())

    override suspend fun <T> setValue(userPreference: UserPreference<T>, value: T) {
        dataStore.edit { userPreference.setValue(it, value) }
    }

    override suspend fun edit(transform: (preferences: MutablePreferences) -> Unit) {
        dataStore.edit(transform)
    }
}
