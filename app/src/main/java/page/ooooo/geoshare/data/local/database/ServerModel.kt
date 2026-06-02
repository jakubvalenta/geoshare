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
    val description: String = "",
    val urlTemplate: String = "",
    val authType: ServerAuthType = ServerAuthType.API_KEY,
    val apiKey: String = "",
    val apiKeyHeader: String = "",
    val challengeUrl: String = "",
    val loginUrl: String = "",
    val registerUrl: String = "",
    val selectedGoogleMapsAddress: Boolean = false,
    val selectedGoogleMapsPlace: Boolean = false,
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

    @Query("SELECT * FROM server WHERE selectedGoogleMapsAddress = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelectedGoogleMapsAddress(): Server?

    @Query("SELECT * FROM server WHERE selectedGoogleMapsAddress = 1 ORDER BY createdAt ASC LIMIT 1")
    fun getSelectedGoogleMapsAddressFlow(): Flow<Server?>

    @Query("SELECT * FROM server WHERE selectedGoogleMapsPlace = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelectedGoogleMapsPlace(): Server?

    @Query("SELECT * FROM server WHERE selectedGoogleMapsPlace = 1 ORDER BY createdAt ASC LIMIT 1")
    fun getSelectedGoogleMapsPlaceFlow(): Flow<Server?>

    @Query("SELECT * FROM server WHERE selectedSearch = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getSelectedSearch(): Server?

    @Query("SELECT * FROM server WHERE selectedSearch = 1 ORDER BY createdAt ASC LIMIT 1")
    fun getSelectedSearchFlow(): Flow<Server?>

    @Insert
    suspend fun insert(server: Server): Long

    @Update
    suspend fun update(vararg servers: Server)

    @Delete
    suspend fun delete(server: Server)

    @Query("UPDATE server SET selectedGoogleMapsAddress = 1 WHERE uid = :uid")
    suspend fun selectGoogleMapsAddress(uid: Int)

    @Query("UPDATE server SET selectedGoogleMapsAddress = 0")
    suspend fun unselectAllGoogleMapsAddress()

    @Transaction
    suspend fun unselectAllGoogleMapsAddressAndSelect(uid: Int?) {
        unselectAllGoogleMapsAddress()
        if (uid != null) {
            selectGoogleMapsAddress(uid)
        }
    }

    @Query("UPDATE server SET selectedGoogleMapsPlace = 1 WHERE uid = :uid")
    suspend fun selectGoogleMapsPlace(uid: Int)

    @Query("UPDATE server SET selectedGoogleMapsPlace = 0")
    suspend fun unselectAllGoogleMapsPlace()

    @Transaction
    suspend fun unselectAllGoogleMapsPlaceAndSelect(uid: Int?) {
        unselectAllGoogleMapsPlace()
        if (uid != null) {
            selectGoogleMapsPlace(uid)
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
