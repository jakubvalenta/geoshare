package page.ooooo.geoshare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Server::class, Link::class],
    version = 7,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLinkDao(): LinkDao
    abstract fun getServerDao(): ServerDao
}
