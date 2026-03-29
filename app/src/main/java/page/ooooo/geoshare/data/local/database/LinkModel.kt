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
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.firstGraphemeOrNull
import page.ooooo.geoshare.lib.point.Srs
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.components.CharacterIconDescriptor
import page.ooooo.geoshare.ui.components.IconDescriptor
import page.ooooo.geoshare.ui.components.ResourceIconDescriptor
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalUuidApi::class)
@Entity
@Serializable
data class Link(
    val group: String = "",
    val name: String = "",
    val srs: Srs = Srs.WGS84,
    val type: LinkType = LinkType.DISPLAY,
    val appEnabled: Boolean = true,
    val chipEnabled: Boolean = false,
    val sheetEnabled: Boolean = false,
    val coordsUriTemplate: String = "",
    val nameUriTemplate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(index = true, defaultValue = "(RANDOMBLOB(16))")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
) {
    val groupOrName: String
        get() = group.ifEmpty { name }

    val enabled: Boolean
        get() = appEnabled || sheetEnabled || chipEnabled

    fun formatUriString(point: Point, uriQuote: UriQuote = DefaultUriQuote): String? =
        point.formatUriString(coordsUriTemplate, nameUriTemplate, srs, uriQuote = uriQuote)

    val menuIcon: IconDescriptor
        get() = when (type) {
            LinkType.DISPLAY -> ResourceIconDescriptor(R.drawable.location_on_24px)
            LinkType.NAVIGATION -> ResourceIconDescriptor(R.drawable.navigation_24px)
            LinkType.STREET_VIEW -> ResourceIconDescriptor(R.drawable.streetview_24px)
        }

    val icon: IconDescriptor
        get() = CharacterIconDescriptor(name.firstGraphemeOrNull())
}

@Dao
interface LinkDao {
    @Query("SELECT * FROM link ORDER BY name ASC")
    suspend fun getAll(): List<Link>

    @Query("SELECT * FROM link ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Link>>

    @Query("SELECT * FROM link WHERE uid = :uid")
    suspend fun getByUid(uid: Int): Link?

    @Query("SELECT * FROM link WHERE uuid = :uuid")
    suspend fun getByUUID(uuid: UUID): Link?

    @Insert
    suspend fun insert(link: Link): Long

    @Update
    suspend fun update(vararg entries: Link)

    @Delete
    suspend fun delete(link: Link)

    @Query("UPDATE link SET appEnabled = 1 WHERE uid = :uid")
    suspend fun enable(uid: Int)

    @Query("UPDATE link SET appEnabled = 0, chipEnabled = 0, sheetEnabled = 0 WHERE uid = :uid")
    suspend fun disable(uid: Int)

    @Query("UPDATE link SET appEnabled = 0, chipEnabled = 0, sheetEnabled = 0 WHERE `group` = :group")
    suspend fun disableGroup(group: String?)
}
