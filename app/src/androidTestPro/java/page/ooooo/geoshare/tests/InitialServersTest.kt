package page.ooooo.geoshare.tests

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import page.ooooo.geoshare.data.local.database.InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID
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
        val expectedItems = buildList {
            add(
                Server(
                    name = "GeoShare Proxy (GM Address)",
                    urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/address/{q}",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                    loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                    registerUrl = "https://api.geoshare-app.net/v1/auth/register",
                    selectedGoogleMapsAddress = true,
                    selectedSearch = true,
                    uuid = UUID.fromString("640f61e6-2bb4-41d3-9b4a-65e656564d03"),
                )
            )
            add(
                Server(
                    name = "GeoShare Proxy (GM Place)",
                    urlTemplate = "https://api.geoshare-app.net/v1/google-maps/geocode/places/{q}",
                    authType = ServerAuthType.ATTESTATION,
                    challengeUrl = "https://api.geoshare-app.net/v1/auth/challenge",
                    loginUrl = "https://api.geoshare-app.net/v1/auth/login",
                    registerUrl = "https://api.geoshare-app.net/v1/auth/register",
                    selectedGoogleMapsPlace = true,
                    uuid = UUID.fromString("e6f6ace9-0f52-42bd-86c4-f42cdebea60c"),
                )
            )
            add(
                Server(
                    name = "Google Maps Geocode Address",
                    urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
                    authType = ServerAuthType.API_KEY,
                    apiKeyHeader = "X-Goog-Api-Key",
                    uuid = UUID.fromString(GOOGLE_MAPS_GEOCODE_ADDRESS_UUID),
                )
            )
            add(
                Server(
                    name = "Google Maps Geocode Place",
                    urlTemplate = "https://geocode.googleapis.com/v4/geocode/places/{q}",
                    authType = ServerAuthType.API_KEY,
                    apiKeyHeader = "X-Goog-Api-Key",
                    uuid = UUID.fromString("c5c215a1-c453-4de9-adb3-daecbd7dc876"),
                )
            )
            if (BuildConfig.DEBUG) {
                add(
                    Server(
                        name = "Local GeoShare Proxy (GM Address)",
                        urlTemplate = "http://127.0.0.1:8080/v1/google-maps/geocode/address/{q}",
                        authType = ServerAuthType.ATTESTATION,
                        challengeUrl = "http://127.0.0.1:8080/v1/auth/challenge",
                        loginUrl = "http://127.0.0.1:8080/v1/auth/login",
                        registerUrl = "http://127.0.0.1:8080/v1/auth/register",
                        uuid = UUID.fromString("274f5f6e-8e44-49ed-aa60-16ac05f9b37f"),
                    )
                )
                add(
                    Server(
                        name = "Local GeoShare Proxy (GM Place)",
                        urlTemplate = "http://127.0.0.1:8080/v1/google-maps/geocode/places/{q}",
                        authType = ServerAuthType.ATTESTATION,
                        challengeUrl = "http://127.0.0.1:8080/v1/auth/challenge",
                        loginUrl = "http://127.0.0.1:8080/v1/auth/login",
                        registerUrl = "http://127.0.0.1:8080/v1/auth/register",
                        uuid = UUID.fromString("6655c0d2-0f0d-4490-a8b2-53a76e08294c"),
                    ),
                )
            }
        }.sortedBy { it.name }
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
