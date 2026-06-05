package page.ooooo.geoshare

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import java.util.UUID

class InitialServersTest : InitialDataTest {
    override lateinit var db: AppDatabase

    override fun restore(db: SupportSQLiteDatabase) = InitialServersImpl.restore(db)

    @Test
    @Throws(Exception::class)
    fun initialLinksAreInserted() = runBlocking {
        val serverDao = db.getServerDao()
        val expectedItems = listOf(
            Server(
                name = "Google Maps Geocode Address",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8"),
            ),
            Server(
                name = "Google Maps Geocode Place",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/places/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString("c5c215a1-c453-4de9-adb3-daecbd7dc876"),
            ),
        ).sortedBy { it.name }
        val actualItems = serverDao.getAll()
        Assert.assertEquals(expectedItems.size, actualItems.size)
        for ((expectedItem, actualItem) in expectedItems.zip(actualItems)) {
            Assert.assertEquals(
                expectedItem.copy(createdAt = 0L, uid = 0),
                actualItem.copy(createdAt = 0L, uid = 0),
            )
        }
    }
}
