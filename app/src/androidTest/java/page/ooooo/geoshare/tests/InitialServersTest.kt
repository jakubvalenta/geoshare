package page.ooooo.geoshare.tests

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import java.io.IOException

class InitialServersTest {
    private val expectedItems = InitialServersTestDataImpl.expectedItems

    @Test
    @Throws(IOException::class)
    fun restore_insertsInitialData() = runBlocking {
        // Create database
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    InitialServersImpl.restore(db)
                }
            })
            .build()

        // Initial data are inserted
        try {
            val dao = db.getServerDao()
            val actualItems = dao.getAll()
            assertEquals(expectedItems.size, actualItems.size)
            for ((expectedItem, actualItem) in expectedItems.zip(actualItems)) {
                assertEquals(
                    expectedItem.copy(createdAt = 0, uid = 0),
                    actualItem.copy(createdAt = 0, uid = 0),
                )
            }
        } finally {
            db.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrations_migrateEarliestDatabaseVersion() = runBlocking {
        val testDb = "server-migration-test"

        // Create earliest version of the database
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
        )
        val db = helper.createDatabase(testDb, 1)
        db.close()

        // Create latest version of the database
        val migratedDb = Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                AppDatabase::class.java,
                testDb,
            )
            .addMigrations(
                *InitialLinks.migrations,
                *InitialServersImpl.migrations,
            )
            .build()

        // Data have been migrated
        try {
            val dao = migratedDb.getServerDao()
            val actualItems = dao.getAll()
            assertEquals(expectedItems.size, actualItems.size)
            for ((expectedItem, actualItem) in expectedItems.zip(actualItems)) {
                assertEquals(
                    expectedItem.copy(createdAt = 0, uid = 0),
                    actualItem.copy(createdAt = 0, uid = 0),
                )
            }
        } finally {
            migratedDb.close()
        }
    }
}
