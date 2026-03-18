package page.ooooo.geoshare.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import page.ooooo.geoshare.data.di.ApplicationScope
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkDao
import java.util.UUID
import javax.inject.Inject

interface LinkRepository {
    val all: Flow<List<Link>>

    suspend fun getAll(): List<Link>

    suspend fun getByUid(uid: Int): Link?

    suspend fun getByUUID(uuid: UUID): Link?

    suspend fun insert(link: Link): Long

    suspend fun update(vararg links: Link)

    suspend fun delete(link: Link)

    suspend fun enable(uid: Int)

    suspend fun disable(uid: Int)

    suspend fun disableGroup(group: String?)

    suspend fun restoreInitialData()
}

class DefaultLinkRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val linkDao: LinkDao,
) : LinkRepository {
    /**
     * Flow of links that is shared between multiple view models, so we don't query the db multiple times.
     */
    override val all: Flow<List<Link>> = linkDao.getAllFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    /**
     * Used only in unit tests, because [all] is not practical, since it never finishes.
     */
    override suspend fun getAll() = linkDao.getAll()

    override suspend fun getByUid(uid: Int): Link? = linkDao.getByUid(uid)

    override suspend fun getByUUID(uuid: UUID): Link? = linkDao.getByUUID(uuid)

    override suspend fun insert(link: Link) = linkDao.insert(link)

    override suspend fun update(vararg links: Link) = linkDao.update(*links)

    override suspend fun enable(uid: Int) = linkDao.enable(uid)

    override suspend fun disable(uid: Int) = linkDao.disable(uid)

    override suspend fun delete(link: Link) = linkDao.delete(link)

    override suspend fun disableGroup(group: String?) = linkDao.disableGroup(group)

    override suspend fun restoreInitialData() {
        appDatabase.openHelper.writableDatabase.let { db ->
            appDatabase.runInTransaction {
                AppDatabase.restoreInitialData(db)
            }
        }
    }
}
