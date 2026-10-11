package page.ooooo.geoshare.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

interface InitialData {
    fun restore(db: SupportSQLiteDatabase)
    val migrations: Array<out Migration>
}
