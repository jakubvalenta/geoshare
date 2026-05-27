package page.ooooo.geoshare.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

interface InitialData {
    fun restore(db: SupportSQLiteDatabase)
    val migrations: Array<out Migration>
}
