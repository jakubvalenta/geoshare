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

    override val selected = _fakeServers.mapLatest { it.firstOrNull { item -> item.selected } }

    override suspend fun getAll() = _fakeServers.value

    override suspend fun getByUid(uid: Int) =
        _fakeServers.value.firstOrNull { it.uid == uid }

    override suspend fun getByUUID(uuid: UUID) =
        _fakeServers.value.firstOrNull { it.uuid == uuid }

    override suspend fun getSelected() =
        _fakeServers.value.firstOrNull { it.selected }

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

    override suspend fun unselectAllAndSelect(uid: Int?) {
        _fakeServers.value = _fakeServers.value.map {
            it.copy(selected = it.uid == uid)
        }
    }

    override suspend fun restoreInitialData() {
        _fakeServers.value = defaultFakeServers
    }
}

val FakeGeoShareServer = Server(
    baseUrl = "https://api.geoshare-app.net",
    authType = ServerAuthType.ATTESTATION,
)
val FakeGoogleMapsServer = Server(
    baseUrl = "https://geocode.googleapis.com",
    authType = ServerAuthType.API_KEY,
    apiKeyHeader = "X-Goog-Api-Key",
)

/**
 * Items for testing purposes. The actual items that the table is populated with are in
 * [page.ooooo.geoshare.data.local.database.AppDatabase.Companion.restoreInitialData].
 */
val defaultFakeServers = listOf(
    FakeGeoShareServer,
    FakeGoogleMapsServer,
)
