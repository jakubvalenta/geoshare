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
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalUuidApi::class)
@Entity
@Serializable
data class Server(
    val name: String = "",
    val urlTemplate: String = "",
    val authType: ServerAuthType = ServerAuthType.API_KEY,
    val apiKey: String = "",
    val apiKeyHeader: String = "",
    val challengeUrl: String = "",
    val loginUrl: String = "",
    val registerUrl: String = "",
    val selectedGoogleMaps: Boolean = false,
    // TODO Add selectedGoogleMapsPlace?
    val selectedSearch: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(index = true, defaultValue = "(RANDOMBLOB(16))")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
) {
    fun getUrl(query: String, uriQuote: UriQuote = DefaultUriQuote): String =
        urlTemplate.replace("{q}", uriQuote.encode(query))

    fun isValid(): Boolean =
        when (authType) {
            ServerAuthType.API_KEY -> urlTemplate.isNotEmpty() && apiKey.isNotEmpty() && apiKeyHeader.isNotEmpty()
            ServerAuthType.ATTESTATION -> urlTemplate.isNotEmpty()
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

    @Query("SELECT * FROM server WHERE selectedGoogleMaps = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelectedGoogleMaps(): Server?

    @Query("SELECT * FROM server WHERE selectedSearch = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelectedSearch(): Server?

    @Insert
    suspend fun insert(server: Server): Long

    @Update
    suspend fun update(vararg servers: Server)

    @Delete
    suspend fun delete(server: Server)

    @Query("UPDATE server SET selectedGoogleMaps = 1 WHERE uid = :uid")
    suspend fun selectGoogleMaps(uid: Int)

    @Query("UPDATE server SET selectedGoogleMaps = 0")
    suspend fun unselectAllGoogleMaps()

    @Transaction
    suspend fun unselectAllGoogleMapsAndSelect(uid: Int?) {
        unselectAllGoogleMaps()
        if (uid != null) {
            selectGoogleMaps(uid)
        }
    }

    @Query("UPDATE server SET selectedSearch = 1 WHERE uid = :uid")
    suspend fun selectSearch(uid: Int)

    @Query("UPDATE server SET selectedSearch = 0")
    suspend fun unselectAllSearch()

    @Transaction
    suspend fun unselectAllSearchAndSelect(uid: Int?) {
        unselectAllSearch()
        if (uid != null) {
            selectSearch(uid)
        }
    }
}
