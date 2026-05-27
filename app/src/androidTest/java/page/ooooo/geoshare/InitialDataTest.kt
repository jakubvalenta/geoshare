package page.ooooo.geoshare

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import page.ooooo.geoshare.data.local.database.AppDatabase
import java.io.IOException

interface InitialDataTest {
    var db: AppDatabase

    fun restore(db: SupportSQLiteDatabase)

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    restore(db)
                }
            })
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
