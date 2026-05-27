package page.ooooo.geoshare

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.database.ServerDao
import java.io.IOException
import java.util.UUID

class InitialServersTest {
    private lateinit var serverDao: ServerDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    AppDatabase.restoreInitialServers(db)
                }
            })
            .build()
        serverDao = db.getServerDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun initialLinksAreInserted() = runBlocking {
        val expectedItems = listOf(
            Server(
                baseUrl = "https://api.geoshare-app.net",
                authType = ServerAuthType.ATTESTATION,
                uuid = UUID.fromString("640f61e6-2bb4-41d3-9b4a-65e656564d03"),
            ),
            Server(
                baseUrl = "https://geocode.googleapis.com",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8"),
            ),
        ).sortedBy { it.name }
        val actualItems = serverDao.getAll()
        assertEquals(expectedItems.size, actualItems.size)
        for ((expectedItem, actualItem) in expectedItems.zip(actualItems)) {
            assertEquals(
                expectedItem.copy(createdAt = 0L, uid = 0),
                actualItem.copy(createdAt = 0L, uid = 0),
            )
        }
    }
}
