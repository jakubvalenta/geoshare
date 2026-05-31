package page.ooooo.geoshare.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object InitialServersImpl : InitialServers {
    /**
     * Delete all servers and populate the table with initial ones.
     *
     * When you change these servers, you must:
     *
     * 1. Increase the version of [AppDatabase].
     * 2. Add new migration to [migrations].
     * 3. Update InitialServersImpl in both the free and pro variant.
     * 4. Update InitialServersImplTest in both the free and pro variant.
     *
     * Optionally, you can add the new servers to [page.ooooo.geoshare.data.di.defaultFakeServers], which is used for
     * testing.
     */
    override fun restore(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM server")
        db.execSQL(
            "INSERT INTO server (`baseUrl`,`authType`,`apiKey`,`apiKeyHeader`,`selectedGoogleMaps`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "https://api.geoshare-app.net",
                "ATTESTATION",
                "",
                "",
                1,
                1,
                1779859233816,
                Uuid.parse("640f61e6-2bb4-41d3-9b4a-65e656564d03").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT INTO server (`baseUrl`,`authType`,`apiKey`,`apiKeyHeader`,`selectedGoogleMaps`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "https://geocode.googleapis.com",
                "API_KEY",
                "",
                "X-Goog-Api-Key",
                0,
                0,
                1779859252618,
                Uuid.parse("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8").toByteArray(),
            )
        )
    }

    override val migrations = arrayOf(
        object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT OR REPLACE INTO server (`baseUrl`,`authType`,`apiKey`,`apiKeyHeader`,`selectedGoogleMaps`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "https://api.geoshare-app.net",
                        "ATTESTATION",
                        "",
                        "",
                        1,
                        1,
                        1779859233816,
                        Uuid.parse("640f61e6-2bb4-41d3-9b4a-65e656564d03").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO server (`baseUrl`,`authType`,`apiKey`,`apiKeyHeader`,`selectedGoogleMaps`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "https://geocode.googleapis.com",
                        "API_KEY",
                        "",
                        "X-Goog-Api-Key",
                        0,
                        0,
                        1779859252618,
                        Uuid.parse("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8").toByteArray(),
                    )
                )
            }
        }
    )
}
