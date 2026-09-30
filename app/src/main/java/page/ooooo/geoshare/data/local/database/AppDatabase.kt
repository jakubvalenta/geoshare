package page.ooooo.geoshare.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Server::class, Link::class],
    version = 11,
    autoMigrations = [
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 9, to = 10),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLinkDao(): LinkDao
    abstract fun getServerDao(): ServerDao
}
