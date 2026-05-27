package page.ooooo.geoshare.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalUuidApi::class)
@Entity
@Serializable
data class ApiPreset(
    val baseUrl: String = "",
    val authType: ApiAuthType = ApiAuthType.API_KEY,
    val apiKey: String = "",
    val apiKeyHeader: String = "",
    val enabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(index = true, defaultValue = "(RANDOMBLOB(16))")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
)

@Dao
interface ApiPresetDao {
    @Query("SELECT * FROM apiPreset ORDER BY createdAt ASC")
    suspend fun getAll(): List<ApiPreset>

    @Query("SELECT * FROM apiPreset ORDER BY createdAt ASC")
    fun getAllFlow(): Flow<List<ApiPreset>>

    @Query("SELECT * FROM apiPreset WHERE uid = :uid")
    suspend fun getByUid(uid: Int): ApiPreset?

    @Query("SELECT * FROM apiPreset WHERE uuid = :uuid")
    suspend fun getByUUID(uuid: UUID): ApiPreset?

    @Insert
    suspend fun insert(apiPreset: ApiPreset): Long

    @Update
    suspend fun update(vararg apiPresets: ApiPreset)

    @Delete
    suspend fun delete(apiPreset: ApiPreset)

    @Query("UPDATE apiPreset SET enabled = 1 WHERE uid = :uid")
    suspend fun enable(uid: Int)

    @Query("UPDATE apiPreset SET enabled = 0 WHERE uid = :uid")
    suspend fun disable(uid: Int)
}
