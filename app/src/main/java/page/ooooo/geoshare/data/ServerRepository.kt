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
    val selected: Flow<Server?>

    suspend fun getAll(): List<Server>

    suspend fun getByUid(uid: Int): Server?

    suspend fun getByUUID(uuid: UUID): Server?

    suspend fun getSelected(): Server?

    suspend fun insert(server: Server): Long

    suspend fun update(vararg servers: Server)

    suspend fun delete(server: Server)

    suspend fun unselectAllAndSelect(uid: Int?)

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

    override val selected: Flow<Server?> = serverDao.getSelectedFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    /**
     * Used only in unit tests, because [all] is not practical, since it never finishes.
     */
    override suspend fun getAll() = serverDao.getAll()

    override suspend fun getByUid(uid: Int): Server? = serverDao.getByUid(uid)

    override suspend fun getByUUID(uuid: UUID): Server? = serverDao.getByUUID(uuid)

    override suspend fun getSelected(): Server? = serverDao.getSelected()

    override suspend fun insert(server: Server) = serverDao.insert(server)

    override suspend fun update(vararg servers: Server) = serverDao.update(*servers)

    override suspend fun delete(server: Server) = serverDao.delete(server)

    override suspend fun unselectAllAndSelect(uid: Int?) = serverDao.unselectAllAndSelect(uid)

    override suspend fun restoreInitialData() {
        appDatabase.openHelper.writableDatabase.let { db ->
            appDatabase.runInTransaction {
                InitialServersImpl.restore(db)
            }
        }
    }
}
