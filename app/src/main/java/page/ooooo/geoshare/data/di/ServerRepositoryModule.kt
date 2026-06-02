package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.data.DefaultServerRepository
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerDao
import page.ooooo.geoshare.data.local.database.AppDatabase
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServerRepositoryModule {

    @Singleton
    @Provides
    fun provideServerRepository(
        appDatabase: AppDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        serverDao: ServerDao,
    ): ServerRepository =
        DefaultServerRepository(appDatabase, applicationScope, serverDao)
}

@OptIn(ExperimentalCoroutinesApi::class)
class FakeServerRepository(
    initialServers: List<Server> = defaultFakeServers,
) : ServerRepository {
    private val _fakeServers: MutableStateFlow<List<Server>> = MutableStateFlow(initialServers)

    override val all = _fakeServers.mapLatest { it.sortedBy { item -> item.createdAt }.reversed() }

    override val selectedGoogleMapsAddress =
        _fakeServers.mapLatest { it.firstOrNull { item -> item.selectedGoogleMapsAddress } }

    override val selectedGoogleMapsPlace =
        _fakeServers.mapLatest { it.firstOrNull { item -> item.selectedGoogleMapsPlace } }

    override val selectedSearch =
        _fakeServers.mapLatest { it.firstOrNull { item -> item.selectedSearch } }

    override suspend fun getAll() = _fakeServers.value

    override suspend fun getByUid(uid: Int) =
        _fakeServers.value.firstOrNull { it.uid == uid }

    override suspend fun getByUUID(uuid: UUID) =
        _fakeServers.value.firstOrNull { it.uuid == uuid }

    override suspend fun getSelectedGoogleMapsAddress() =
        _fakeServers.value.firstOrNull { it.selectedGoogleMapsAddress }

    override suspend fun getSelectedGoogleMapsPlace() =
        _fakeServers.value.firstOrNull { it.selectedGoogleMapsPlace }

    override suspend fun getSelectedSearch() =
        _fakeServers.value.firstOrNull { it.selectedSearch }

    override suspend fun insert(server: Server) =
        (_fakeServers.value + listOf(server)).also {
            _fakeServers.value = it
        }.size.toLong()

    override suspend fun update(vararg servers: Server) {
        for (server in servers) {
            _fakeServers.value = _fakeServers.value.map {
                if (it.uid == server.uid) {
                    server
                } else {
                    it
                }
            }
        }
    }

    override suspend fun delete(server: Server) {
        _fakeServers.value = _fakeServers.value.filterNot { it.uid == server.uid }
    }

    override suspend fun unselectAllGoogleMapsAddressAndSelect(uid: Int?) {
        _fakeServers.value = _fakeServers.value.map {
            it.copy(selectedGoogleMapsAddress = it.uid == uid)
        }
    }

    override suspend fun unselectAllGoogleMapsPlaceAndSelect(uid: Int?) {
        _fakeServers.value = _fakeServers.value.map {
            it.copy(selectedGoogleMapsPlace = it.uid == uid)
        }
    }

    override suspend fun unselectAllSearchAndSelect(uid: Int?) {
        _fakeServers.value = _fakeServers.value.map {
            it.copy(selectedSearch = it.uid == uid)
        }
    }

    override suspend fun restoreInitialData() {
        _fakeServers.value = defaultFakeServers
    }
}

val FakeGeoShareGoogleMapsAddressServer = Server(
    name = "GeoShare Proxy",
    description = "Google Maps Geocode Address backend",
    urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/address/{q}",
    authType = ServerAuthType.ATTESTATION,
    challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
    loginUrl = "https://api.geoshare-app.net/v1/auth/login",
    registerUrl = "https://api.geoshare-app.net/v1/auth/register",
)
val FakeGeoShareGoogleMapsPlaceServer = Server(
    name = "GeoShare Proxy",
    description = "Google Maps Geocode Place backend",
    urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/places/{q}",
    authType = ServerAuthType.ATTESTATION,
    challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
    loginUrl = "https://api.geoshare-app.net/v1/auth/login",
    registerUrl = "https://api.geoshare-app.net/v1/auth/register",
)
val FakeGoogleMapsAddressServer = Server(
    name = "Google Maps",
    description = "Geocode Address",
    urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
    authType = ServerAuthType.API_KEY,
    apiKeyHeader = "X-Goog-Api-Key",
)

/**
 * Items for testing purposes. The actual items that the table is populated with are in
 * [page.ooooo.geoshare.data.local.database.InitialServers].
 */
val defaultFakeServers = listOf(
    FakeGeoShareGoogleMapsAddressServer,
    FakeGoogleMapsAddressServer,
)
