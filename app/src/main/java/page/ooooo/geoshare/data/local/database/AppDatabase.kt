package page.ooooo.geoshare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Database(
    entities = [Link::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLinkDao(): LinkDao

    @OptIn(ExperimentalUuidApi::class)
    companion object {
        /**
         * Delete all links and populate the table with initial links.
         *
         * When you change these links, you must:
         *
         * 1. Increase the version of [AppDatabase].
         * 2. Add new migration similar to [MIGRATION_1_2].
         * 3. Update InitialLinksTest.
         *
         * Optionally, you can add the new links to [page.ooooo.geoshare.data.di.defaultFakeLinks], which is used for
         * testing.
         *
         * Notice that we use a standard map display link for Google Street View search link, because Google Street
         * View doesn't support search.
         *
         * See https://developers.google.com/maps/documentation/urls/get-started
         *
         * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
         *
         * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
         */
        fun restoreInitialData(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM Link")
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "OpenStreetMap",
                    "OpenStreetMap",
                    "WGS84",
                    "DISPLAY",
                    1,
                    1,
                    1,
                    "https://www.openstreetmap.org/#map={z}/{lat}/{lon}",
                    "https://www.openstreetmap.org/search?query={q}",
                    1772395295367L,
                    Uuid.parse("a771fd79-291e-4e55-9952-601f87b05bfe").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "OpenStreetMap",
                    "OpenStreetMap navigation",
                    "WGS84",
                    "NAVIGATION",
                    1,
                    0,
                    1,
                    "https://www.openstreetmap.org/directions?to={lat}%2C{lon}",
                    "https://www.openstreetmap.org/directions?to={q}",
                    1772395295367L,
                    Uuid.parse("dad7a723-eeb1-4f60-af5d-7813b3cc1926").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Google Maps",
                    "Google Maps",
                    "GCJ02",
                    "DISPLAY",
                    1,
                    1,
                    1,
                    "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    "https://www.google.com/maps/search/?api=1&query={q}",
                    1772395295367L,
                    Uuid.parse("7bd96da4-beba-4a30-9dbd-b437a49a1dc0").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Google Maps",
                    "Google Maps navigation",
                    "GCJ02",
                    "NAVIGATION",
                    1,
                    0,
                    1,
                    "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                    "https://www.google.com/maps/dir/?api=1&destination={q}",
                    1772395295367L,
                    Uuid.parse("64b0b360-24ec-4113-9056-314223c6e19a").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Google Maps",
                    "Google Street View",
                    "GCJ02",
                    "STREET_VIEW",
                    1,
                    0,
                    0,
                    "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
                    "https://www.google.com/maps/search/?api=1&query={q}",
                    1772395295367L,
                    Uuid.parse("9d7cd113-ce01-4b8b-82fe-856956b8b20a").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Apple Maps",
                    "Apple Maps",
                    "WGS84",
                    "DISPLAY",
                    1,
                    0,
                    1,
                    "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                    "https://maps.apple.com/?q={q}",
                    1772395295367L,
                    Uuid.parse("ce900ea1-2c5d-4641-82f3-a5429a68d603").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Apple Maps",
                    "Apple Maps navigation",
                    "WGS84",
                    "NAVIGATION",
                    1,
                    0,
                    1,
                    "https://maps.apple.com/?daddr={lat}%2C{lon}",
                    "https://maps.apple.com/?daddr={q}",
                    1772395295367L,
                    Uuid.parse("a5092c63-cf5c-4225-9059-e888ae12e215").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Magic Earth",
                    "Magic Earth",
                    "WGS84",
                    "DISPLAY",
                    0,
                    0,
                    1,
                    "magicearth://?show_on_map&lat={lat}&lon={lon}&name={name}",
                    "magicearth://?open_search&q={q}",
                    1772395295367L,
                    Uuid.parse("b109970a-aef8-4482-9879-52e128fd0e07").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "Magic Earth",
                    "Magic Earth navigation",
                    "WGS84",
                    "NAVIGATION",
                    0,
                    0,
                    1,
                    "magicearth://?get_directions&lat={lat}&lon={lon}",
                    "magicearth://?get_directions&q={q}",
                    1772395295367L,
                    Uuid.parse("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "GasBuddy",
                    "WGS84",
                    "DISPLAY",
                    0,
                    0,
                    0,
                    "https://www.gasbuddy.com/gaspricemap?lat={lat}&lng={lon}&z={z}",
                    "",
                    1772579164207L,
                    Uuid.parse("fd89f6f0-694e-4d96-b604-ed15e2530a2d").toByteArray(),
                )
            )
            @Suppress("SpellCheckingInspection")
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "KartaView",
                    "WGS84",
                    "STREET_VIEW",
                    0,
                    0,
                    0,
                    "https://kartaview.org/map/@{lat}%2C{lon},{z}z",
                    "",
                    1772579164207L,
                    Uuid.parse("7e09855d-d29b-4c18-944f-7fa440db3528").toByteArray(),
                )
            )
            @Suppress("SpellCheckingInspection")
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "Mapilio",
                    "WGS84",
                    "STREET_VIEW",
                    0,
                    0,
                    0,
                    "https://mapilio.com/app?lat={lat}&lng={lon}&zoom={z}",
                    "",
                    1772579164207L,
                    Uuid.parse("c206d165-b2db-4030-a415-203e92cacb66").toByteArray(),
                )
            )
            @Suppress("SpellCheckingInspection")
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "Panoramax",
                    "WGS84",
                    "STREET_VIEW",
                    0,
                    0,
                    0,
                    "https://panoramax.openstreetmap.fr/#background=streets&focus=map&map={z}/{lat}/{lon}&speed=250",
                    "",
                    1772579164207L,
                    Uuid.parse("c80ccfa6-6d22-4290-b8f5-82119f87a570").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "PeakVisor",
                    "WGS84",
                    "DISPLAY",
                    0,
                    0,
                    0,
                    "https://peakvisor.com/embed?lat={lat}&lng={lon}&alt=3000&yaw=160",
                    "",
                    1772579164207L,
                    Uuid.parse("fe5de45b-ba0f-4d87-8ee9-979f1a701978").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "Refuges",
                    "WGS84",
                    "DISPLAY",
                    0,
                    0,
                    0,
                    "https://www.refuges.info/nav?map={z}/{lon}/{lat}",
                    "",
                    1772579164207L,
                    Uuid.parse("1aad2356-d900-45e5-91a0-d7ee2092641d").toByteArray(),
                )
            )
            db.execSQL(
                "INSERT INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    "",
                    "uMap",
                    "WGS84",
                    "DISPLAY",
                    0,
                    0,
                    0,
                    "https://umap.openstreetmap.fr/en/map/test-mapy_626288#{z}/{lat}/{lon}",
                    "",
                    1772579164207L,
                    Uuid.parse("94e1350a-3599-43b3-858b-59750a6f8680").toByteArray(),
                )
            )
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "GasBuddy",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://www.gasbuddy.com/gaspricemap?lat={lat}&lng={lon}&z={z}",
                        "",
                        1772579164207L,
                        Uuid.parse("fd89f6f0-694e-4d96-b604-ed15e2530a2d").toByteArray(),
                    )
                )
                @Suppress("SpellCheckingInspection")
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "KartaView",
                        "WGS84",
                        "STREET_VIEW",
                        0,
                        0,
                        0,
                        "https://kartaview.org/map/@{lat}%2C{lon},{z}z",
                        "",
                        1772579164207L,
                        Uuid.parse("7e09855d-d29b-4c18-944f-7fa440db3528").toByteArray(),
                    )
                )
                @Suppress("SpellCheckingInspection")
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "Mapilio",
                        "WGS84",
                        "STREET_VIEW",
                        0,
                        0,
                        0,
                        "https://mapilio.com/app?lat={lat}&lng={lon}&zoom={z}",
                        "",
                        1772579164207L,
                        Uuid.parse("c206d165-b2db-4030-a415-203e92cacb66").toByteArray(),
                    )
                )
                @Suppress("SpellCheckingInspection")
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "Panoramax",
                        "WGS84",
                        "STREET_VIEW",
                        0,
                        0,
                        0,
                        "https://panoramax.openstreetmap.fr/#background=streets&focus=map&map={z}/{lat}/{lon}&speed=250",
                        "",
                        1772579164207L,
                        Uuid.parse("c80ccfa6-6d22-4290-b8f5-82119f87a570").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "PeakVisor",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://peakvisor.com/embed?lat={lat}&lng={lon}&alt=3000&yaw=160",
                        "",
                        1772579164207L,
                        Uuid.parse("fe5de45b-ba0f-4d87-8ee9-979f1a701978").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "Refuges",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://www.refuges.info/nav?map={z}/{lon}/{lat}",
                        "",
                        1772579164207L,
                        Uuid.parse("1aad2356-d900-45e5-91a0-d7ee2092641d").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO Link(`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "uMap",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://umap.openstreetmap.fr/en/map/test-mapy_626288#{z}/{lat}/{lon}",
                        "",
                        1772579164207L,
                        Uuid.parse("94e1350a-3599-43b3-858b-59750a6f8680").toByteArray(),
                    )
                )
            }
        }
    }
}
