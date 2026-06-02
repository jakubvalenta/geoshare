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
                name = "GeoShare Proxy",
                description = "Google Maps Geocode Address backend",
                urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/address/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                registerUrl = "https://api.geoshare-app.net/v1/auth/register",
                selectedGoogleMapsAddress = true,
                selectedSearch = true,
                uuid = UUID.fromString("640f61e6-2bb4-41d3-9b4a-65e656564d03"),
            ),
            Server(
                name = "GeoShare Proxy",
                description = "Google Maps Geocode Place backend",
                urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/places/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                registerUrl = "https://api.geoshare-app.net/v1/auth/register",
                selectedGoogleMapsPlace = true,
                uuid = UUID.fromString("e6f6ace9-0f52-42bd-86c4-f42cdebea60c"),
            ),
            Server(
                name = "Google Maps",
                description = "Geocode Address",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8"),
            ),
            Server(
                name = "Google Maps",
                description = "Geocode Place",
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
