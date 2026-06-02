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
            "INSERT INTO server (`name`,`description`,`urlTemplate`,`authType`,`apiKey`,`apiKeyHeader`,`challengeUrl`,`loginUrl`,`registerUrl`,`selectedGoogleMapsAddress`,`selectedGoogleMapsPlace`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Geocode Address",
                "https://geocode.googleapis.com/v4/geocode/address/{q}",
                "API_KEY",
                "",
                "X-Goog-Api-Key",
                "",
                "",
                "",
                0,
                0,
                0,
                1779859252618,
                Uuid.parse("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT INTO server (`name`,`description`,`urlTemplate`,`authType`,`apiKey`,`apiKeyHeader`,`challengeUrl`,`loginUrl`,`registerUrl`,`selectedGoogleMapsAddress`,`selectedGoogleMapsPlace`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Geocode Place",
                "https://geocode.googleapis.com/v4/geocode/places/{q}",
                "API_KEY",
                "",
                "X-Goog-Api-Key",
                "",
                "",
                "",
                0,
                0,
                0,
                1779859252618,
                Uuid.parse("c5c215a1-c453-4de9-adb3-daecbd7dc876").toByteArray(),
            )
        )
    }

    override val migrations = arrayOf(
        object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT OR REPLACE INTO server (`name`,`description`,`urlTemplate`,`authType`,`apiKey`,`apiKeyHeader`,`challengeUrl`,`loginUrl`,`registerUrl`,`selectedGoogleMapsAddress`,`selectedGoogleMapsPlace`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Google Maps",
                        "Geocode Address",
                        "https://geocode.googleapis.com/v4/geocode/address/{q}",
                        "API_KEY",
                        "",
                        "X-Goog-Api-Key",
                        "",
                        "",
                        "",
                        0,
                        0,
                        0,
                        1779859252618,
                        Uuid.parse("16b3bb06-3a3b-4853-ac06-c4bf1eb346f8").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO server (`name`,`description`,`urlTemplate`,`authType`,`apiKey`,`apiKeyHeader`,`challengeUrl`,`loginUrl`,`registerUrl`,`selectedGoogleMapsAddress`,`selectedGoogleMapsPlace`,`selectedSearch`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Google Maps",
                        "Geocode Place",
                        "https://geocode.googleapis.com/v4/geocode/places/{q}",
                        "API_KEY",
                        "",
                        "X-Goog-Api-Key",
                        "",
                        "",
                        "",
                        0,
                        0,
                        0,
                        1779859252618,
                        Uuid.parse("c5c215a1-c453-4de9-adb3-daecbd7dc876").toByteArray(),
                    )
                )
            }
        },
    )
}
