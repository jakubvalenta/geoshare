package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import page.ooooo.geoshare.data.ApiPresetRepository
import page.ooooo.geoshare.data.DefaultApiPresetRepository
import page.ooooo.geoshare.data.local.database.ApiAuthType
import page.ooooo.geoshare.data.local.database.ApiPreset
import page.ooooo.geoshare.data.local.database.ApiPresetDao
import page.ooooo.geoshare.data.local.database.AppDatabase
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiPresetRepositoryModule {

    @Singleton
    @Provides
    fun provideApiPresetRepository(
        appDatabase: AppDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        apiPresetDao: ApiPresetDao,
    ): ApiPresetRepository =
        DefaultApiPresetRepository(appDatabase, applicationScope, apiPresetDao)
}

@OptIn(ExperimentalCoroutinesApi::class)
class FakeApiPresetRepository(
    initialFakeApiPresets: List<ApiPreset> = defaultFakeApiPresets,
) : ApiPresetRepository {
    private val _fakeApiPresets: MutableStateFlow<List<ApiPreset>> = MutableStateFlow(initialFakeApiPresets)

    override val all = _fakeApiPresets.mapLatest { it.sortedBy { item -> item.createdAt }.reversed() }

    override val selected = _fakeApiPresets.mapLatest { it.firstOrNull { item -> item.enabled } }

    override suspend fun getAll() = _fakeApiPresets.value

    override suspend fun getByUid(uid: Int) =
        _fakeApiPresets.value.firstOrNull { it.uid == uid }

    override suspend fun getByUUID(uuid: UUID) =
        _fakeApiPresets.value.firstOrNull { it.uuid == uuid }

    override suspend fun getSelected() =
        _fakeApiPresets.value.firstOrNull { it.enabled }

    override suspend fun insert(apiPreset: ApiPreset) =
        (_fakeApiPresets.value + listOf(apiPreset)).also {
            _fakeApiPresets.value = it
        }.size.toLong()

    override suspend fun update(vararg apiPresets: ApiPreset) {
        for (apiPreset in apiPresets) {
            _fakeApiPresets.value = _fakeApiPresets.value.map {
                if (it.uid == apiPreset.uid) {
                    apiPreset
                } else {
                    it
                }
            }
        }
    }

    override suspend fun delete(apiPreset: ApiPreset) {
        _fakeApiPresets.value = _fakeApiPresets.value.filterNot { it.uid == apiPreset.uid }
    }

    override suspend fun select(uid: Int?) {
        _fakeApiPresets.value = _fakeApiPresets.value.map {
            it.copy(enabled = it.uid == uid)
        }
    }

    override suspend fun restoreInitialData() {
        _fakeApiPresets.value = defaultFakeApiPresets
    }
}

val FakeGeoShareApiPreset = ApiPreset(
    baseUrl = "https://api.geoshare-app.net",
    authType = ApiAuthType.ATTESTATION,
)
val FakeGoogleMapsApiPreset = ApiPreset(
    baseUrl = "https://geocode.googleapis.com",
    authType = ApiAuthType.API_KEY,
    apiKeyHeader = "X-Goog-Api-Key",
)

/**
 * Objects for testing purposes. The actual objects that the table is populated with are in
 * [page.ooooo.geoshare.data.local.database.AppDatabase.Companion.restoreInitialData].
 */
val defaultFakeApiPresets = listOf(
    FakeGeoShareApiPreset,
    FakeGoogleMapsApiPreset,
)
