package page.ooooo.geoshare.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import page.ooooo.geoshare.data.di.ApplicationScope
import page.ooooo.geoshare.data.local.database.ApiPreset
import page.ooooo.geoshare.data.local.database.ApiPresetDao
import page.ooooo.geoshare.data.local.database.AppDatabase
import java.util.UUID
import javax.inject.Inject

interface ApiPresetRepository {
    val all: Flow<List<ApiPreset>>

    suspend fun getAll(): List<ApiPreset>

    suspend fun getByUid(uid: Int): ApiPreset?

    suspend fun getByUUID(uuid: UUID): ApiPreset?

    suspend fun getFirstEnabled(): ApiPreset?

    suspend fun insert(apiPreset: ApiPreset): Long

    suspend fun update(vararg apiPresets: ApiPreset)

    suspend fun delete(apiPreset: ApiPreset)

    suspend fun enable(uid: Int)

    suspend fun disable(uid: Int)

    suspend fun restoreInitialData()
}

class DefaultApiPresetRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val apiPresetDao: ApiPresetDao,
) : ApiPresetRepository {
    /**
     * Flow of all objects, which is shared between multiple view models, so we don't query the db multiple times.
     */
    override val all: Flow<List<ApiPreset>> = apiPresetDao.getAllFlow()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    /**
     * Used only in unit tests, because [all] is not practical, since it never finishes.
     */
    override suspend fun getAll() = apiPresetDao.getAll()

    override suspend fun getByUid(uid: Int): ApiPreset? = apiPresetDao.getByUid(uid)

    override suspend fun getByUUID(uuid: UUID): ApiPreset? = apiPresetDao.getByUUID(uuid)

    override suspend fun getFirstEnabled(): ApiPreset? = apiPresetDao.getFirstEnabled()

    override suspend fun insert(apiPreset: ApiPreset) = apiPresetDao.insert(apiPreset)

    override suspend fun update(vararg apiPresets: ApiPreset) = apiPresetDao.update(*apiPresets)

    override suspend fun enable(uid: Int) = apiPresetDao.enable(uid)

    override suspend fun disable(uid: Int) = apiPresetDao.disable(uid)

    override suspend fun delete(apiPreset: ApiPreset) = apiPresetDao.delete(apiPreset)

    override suspend fun restoreInitialData() {
        appDatabase.openHelper.writableDatabase.let { db ->
            appDatabase.runInTransaction {
                AppDatabase.restoreInitialData(db)
            }
        }
    }
}
