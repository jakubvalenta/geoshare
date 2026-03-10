package page.ooooo.geoshare

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.data.local.database.AppDatabase
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create the earliest version of the database.
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                """
                INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES
                (
                    'test-group',
                    'test-name',
                    'GCJ02',
                    'NAVIGATION',
                    1,
                    0,
                    1,
                    'https://example.com/?ll={lat}%2C{lon}',
                    'https://example.com/?q={name}',
                    1772395295367,
                    'c9808988-c999-4885-b405-51b4a75d3f5f'
                )
                """.trimIndent()
            )
            close()
        }

        // Open latest version of the database. Room validates the schema once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDb,
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
            .apply {
                openHelper.writableDatabase.close()
            }
    }
}
