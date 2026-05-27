package page.ooooo.geoshare.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalUuidApi::class)
@Entity
@Serializable
data class Server(
    val baseUrl: String = "",
    val authType: ServerAuthType = ServerAuthType.API_KEY,
    val apiKey: String = "",
    val apiKeyHeader: String = "",
    val selected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(index = true, defaultValue = "(RANDOMBLOB(16))")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
) {
    val name: String get() = baseUrl.removePrefix("https://")

    fun isValid(): Boolean =
        when (authType) {
            ServerAuthType.API_KEY -> baseUrl.isNotEmpty() && apiKey.isNotEmpty() && apiKeyHeader.isNotEmpty()
            ServerAuthType.ATTESTATION -> baseUrl.isNotEmpty()
        }
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM server ORDER BY createdAt ASC")
    suspend fun getAll(): List<Server>

    @Query("SELECT * FROM server ORDER BY createdAt ASC")
    fun getAllFlow(): Flow<List<Server>>

    @Query("SELECT * FROM server WHERE uid = :uid")
    suspend fun getByUid(uid: Int): Server?

    @Query("SELECT * FROM server WHERE uuid = :uuid")
    suspend fun getByUUID(uuid: UUID): Server?

    @Query("SELECT * FROM server WHERE selected = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelected(): Server?

    @Query("SELECT * FROM server WHERE selected = 1 ORDER BY createdAt ASC LIMIT 1")
    fun getSelectedFlow(): Flow<Server?>

    @Insert
    suspend fun insert(server: Server): Long

    @Update
    suspend fun update(vararg servers: Server)

    @Delete
    suspend fun delete(server: Server)

    @Query("UPDATE server SET selected = 1 WHERE uid = :uid")
    suspend fun select(uid: Int)

    @Query("UPDATE server SET selected = 0")
    suspend fun unselectAll()

    @Transaction
    suspend fun unselectAllAndSelect(uid: Int?) {
        unselectAll()
        if (uid != null) {
            select(uid)
        }
    }
}
