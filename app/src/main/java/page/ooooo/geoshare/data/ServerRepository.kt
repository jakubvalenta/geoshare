package page.ooooo.geoshare.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import page.ooooo.geoshare.data.di.ApplicationScope
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerDao
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import java.util.UUID
import javax.inject.Inject

interface ServerRepository {
    val all: Flow<List<Server>>
    val selectedGoogleMaps: Flow<Server?>
    val selectedSearch: Flow<Server?>

    suspend fun getAll(): List<Server>

    suspend fun getByUid(uid: Int): Server?

    suspend fun getByUUID(uuid: UUID): Server?

    suspend fun getSelectedGoogleMaps(): Server?

    suspend fun getSelectedSearch(): Server?

    suspend fun insert(server: Server): Long

    suspend fun update(vararg servers: Server)

    suspend fun delete(server: Server)

    suspend fun unselectAllGoogleMapsAndSelect(uid: Int?)

    suspend fun unselectAllSearchAndSelect(uid: Int?)

    suspend fun restoreInitialData()
}

class DefaultServerRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val serverDao: ServerDao,
) : ServerRepository {
    /**
     * Flow of all objects, which is shared between multiple view models, so we don't query the db multiple times.
     */
    override val all: Flow<List<Server>> = serverDao.getAllFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    override val selectedGoogleMaps: Flow<Server?> = serverDao.getSelectedGoogleMapsFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    override val selectedSearch: Flow<Server?> = serverDao.getSelectedSearchFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    /**
     * Used only in unit tests, because [all] is not practical, since it never finishes.
     */
    override suspend fun getAll() = serverDao.getAll()

    override suspend fun getByUid(uid: Int): Server? = serverDao.getByUid(uid)

    override suspend fun getByUUID(uuid: UUID): Server? = serverDao.getByUUID(uuid)

    override suspend fun getSelectedGoogleMaps(): Server? = serverDao.getSelectedGoogleMaps()

    override suspend fun getSelectedSearch(): Server? = serverDao.getSelectedSearch()

    override suspend fun insert(server: Server) = serverDao.insert(server)

    override suspend fun update(vararg servers: Server) = serverDao.update(*servers)

    override suspend fun delete(server: Server) = serverDao.delete(server)

    override suspend fun unselectAllGoogleMapsAndSelect(uid: Int?) = serverDao.unselectAllGoogleMapsAndSelect(uid)

    override suspend fun unselectAllSearchAndSelect(uid: Int?) = serverDao.unselectAllSearchAndSelect(uid)

    override suspend fun restoreInitialData() {
        appDatabase.openHelper.writableDatabase.let { db ->
            appDatabase.runInTransaction {
                InitialServersImpl.restore(db)
            }
        }
    }
}
