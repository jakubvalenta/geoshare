package page.ooooo.geoshare.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.uuid.Uuid

object InitialLinks : InitialData {
    /**
     * Delete all links and populate the table with initial ones.
     *
     * When you change these links, you must:
     *
     * 1. Increase the version of [AppDatabase].
     * 2. Add new migration to [migrations].
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
    override fun restore(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM link")
        // Apple Maps
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772395295367,
                Uuid.parse(APPLE_MAPS_DISPLAY_UUID).toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772395295367,
                Uuid.parse(APPLE_MAPS_NAVIGATION_UUID).toByteArray(),
            )
        )
        // Cartes.app
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Cartes.app",
                "Cartes.app",
                "WGS84",
                "DISPLAY",
                0,
                0,
                0,
                "https://cartes.app/?perspective=non&clic={lat}%7C{lon}#{z}/{lat}/{lon}",
                "https://cartes.app/?q={q}",
                1790865562966,
                Uuid.parse("4d7b8012-7f0e-478a-9858-634dc4009a58").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Cartes.app",
                "Cartes.app navigation",
                "WGS84",
                "NAVIGATION",
                0,
                0,
                0,
                "https://cartes.app/?perspective=non&clic={lat}%7C{lon}&allez=-%3EPoint+sur+la+carte%7C%7C{lon}%7C{lat}#{z}/{lat}/{lon}",
                "",
                1790865562966,
                Uuid.parse("06c9c666-0f65-4a42-b571-de5c1b6b0f13").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Cartes.app",
                "Cartes.app search",
                "WGS84",
                "SEARCH",
                0,
                0,
                0,
                "",
                "https://cartes.app/?q={q}",
                1790865562966,
                Uuid.parse("54ac6017-4988-40a5-86fe-63aac862fb20").toByteArray(),
            )
        )
        // GasBuddy
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("fd89f6f0-694e-4d96-b604-ed15e2530a2d").toByteArray(),
            )
        )
        // Géoportail
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "",
                "Géoportail",
                "WGS84",
                "DISPLAY",
                0,
                0,
                0,
                "https://cartes.gouv.fr/explorer-les-cartes?c={lon},{lat}&z={z}",
                "",
                1778680284986,
                Uuid.parse("b0f1715a-6645-4ae6-a4ec-36d6e5f08c34").toByteArray(),
            )
        )
        // Google Maps
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Google Maps",
                "GCJ02_MAINLAND_CHINA",
                "DISPLAY",
                1,
                1,
                1,
                "https://maps.google.com/?q={lat}%2C{lon}",
                "https://maps.google.com/?q={q}",
                1772395295367,
                Uuid.parse(GOOGLE_MAPS_DISPLAY_UUID).toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Google Maps navigation",
                "GCJ02_MAINLAND_CHINA",
                "NAVIGATION",
                1,
                0,
                1,
                "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                "https://www.google.com/maps/dir/?api=1&destination={q}",
                1772395295367,
                Uuid.parse("64b0b360-24ec-4113-9056-314223c6e19a").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Google Maps search",
                "GCJ02_MAINLAND_CHINA",
                "SEARCH",
                1,
                0,
                1,
                "",
                "https://www.google.com/maps/search/?api=1&query={q}",
                1786537384232,
                Uuid.parse(GOOGLE_MAPS_SEARCH_UUID).toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Google Street View",
                "GCJ02_MAINLAND_CHINA",
                "STREET_VIEW",
                1,
                0,
                0,
                "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
                "",
                1772395295367,
                Uuid.parse(GOOGLE_MAPS_STREET_VIEW_UUID).toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Google Maps",
                "Google Maps Plus Code",
                "GCJ02_MAINLAND_CHINA",
                "DISPLAY",
                0,
                0,
                1,
                "https://www.google.com/maps/place/{plus_code}",
                "",
                1776849143380,
                Uuid.parse("5c891351-3f66-4aa2-83c4-f13c5a67fdef").toByteArray(),
            )
        )
        // Kagi Maps
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Kagi Maps",
                "Kagi Maps",
                "WGS84",
                "DISPLAY",
                0,
                0,
                0,
                "https://kagi.com/maps/info?ll={lat},{lon}&z={z}",
                "https://kagi.com/maps/info?q={q}",
                1790865562966,
                Uuid.parse("7633131d-8485-46ce-8be8-8b05f0928fd8").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Kagi Maps",
                "Kagi Maps navigation",
                "WGS84",
                "NAVIGATION",
                0,
                0,
                0,
                "https://kagi.com/maps/directions?q=|{lat},{lon}",
                "https://kagi.com/maps/directions?q=|{q}",
                1790865562966,
                Uuid.parse("0f327575-c2c9-41e1-9bba-fb4af5e195a7").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Kagi Maps",
                "Kagi Maps search",
                "WGS84",
                "SEARCH",
                0,
                0,
                0,
                "",
                "https://kagi.com/maps/search?q={q}",
                1790865562966,
                Uuid.parse(KAGI_MAPS_SEARCH_UUID).toByteArray(),
            )
        )
        // KartaView
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("7e09855d-d29b-4c18-944f-7fa440db3528").toByteArray(),
            )
        )
        // Magic Earth
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772395295367,
                Uuid.parse("b109970a-aef8-4482-9879-52e128fd0e07").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772395295367,
                Uuid.parse("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7").toByteArray(),
            )
        )
        // Mapilio
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("c206d165-b2db-4030-a415-203e92cacb66").toByteArray(),
            )
        )
        // OpenStreetMap
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "OpenStreetMap",
                "OpenStreetMap",
                "WGS84",
                "DISPLAY",
                1,
                1,
                1,
                "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map={z}/{lat}/{lon}",
                "https://www.openstreetmap.org/search?query={q}",
                1772395295367,
                Uuid.parse("a771fd79-291e-4e55-9952-601f87b05bfe").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772395295367,
                Uuid.parse("dad7a723-eeb1-4f60-af5d-7813b3cc1926").toByteArray(),
            )
        )
        // Panoramax
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("c80ccfa6-6d22-4290-b8f5-82119f87a570").toByteArray(),
            )
        )
        // PeakVisor
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("fe5de45b-ba0f-4d87-8ee9-979f1a701978").toByteArray(),
            )
        )
        // Refuges
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("1aad2356-d900-45e5-91a0-d7ee2092641d").toByteArray(),
            )
        )
        // uMap
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                1772579164207,
                Uuid.parse("94e1350a-3599-43b3-858b-59750a6f8680").toByteArray(),
            )
        )
        // Yahoo! Maps Japan
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Yahoo! Maps Japan",
                "Yahoo! Maps Japan",
                "WGS84",
                "DISPLAY",
                0,
                0,
                0,
                "https://map.yahoo.co.jp/place?lat={lat}&lon={lon}&zoom={z}",
                "https://map.yahoo.co.jp/search?q={q}",
                1790947732269,
                Uuid.parse("5274e6d4-c675-4d44-a36c-b5d36e717624").toByteArray(),
            )
        )
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "Yahoo! Maps Japan",
                "Yahoo! Maps Japan search",
                "WGS84",
                "SEARCH",
                0,
                0,
                0,
                "",
                "https://map.yahoo.co.jp/search?q={q}",
                1790947732269,
                Uuid.parse("7db7be61-657c-47e7-9b10-68b40cd914b4").toByteArray(),
            )
        )
        // Zoom Earth
        db.execSQL(
            "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any>(
                "",
                "Zoom Earth",
                "WGS84",
                "DISPLAY",
                0,
                0,
                0,
                "https://zoom.earth/maps/satellite/#view={lat},{lon},{z}",
                "",
                1776348228136,
                Uuid.parse("a0b5b493-31a7-410d-9fe2-285974738ff1").toByteArray(),
            )
        )
    }

    override val migrations = arrayOf(
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert GasBuddy
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("fd89f6f0-694e-4d96-b604-ed15e2530a2d").toByteArray(),
                    )
                )
                // Insert KartaView
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("7e09855d-d29b-4c18-944f-7fa440db3528").toByteArray(),
                    )
                )
                // Insert Mapilio
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("c206d165-b2db-4030-a415-203e92cacb66").toByteArray(),
                    )
                )
                // Insert Panoramax
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("c80ccfa6-6d22-4290-b8f5-82119f87a570").toByteArray(),
                    )
                )
                // Insert PeakVisor
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("fe5de45b-ba0f-4d87-8ee9-979f1a701978").toByteArray(),
                    )
                )
                // Insert Refuges
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("1aad2356-d900-45e5-91a0-d7ee2092641d").toByteArray(),
                    )
                )
                // Insert uMap
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                        1772579164207,
                        Uuid.parse("94e1350a-3599-43b3-858b-59750a6f8680").toByteArray(),
                    )
                )
            }
        },
        object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update Google Maps to fix SRS
                db.execSQL(
                    "UPDATE link SET srs = ? WHERE uuid = ? AND srs = ?",
                    arrayOf<Any>(
                        "GCJ02_MAINLAND_CHINA",
                        Uuid.parse(GOOGLE_MAPS_DISPLAY_UUID).toByteArray(),
                        "GCJ02",
                    )
                )
                db.execSQL(
                    "UPDATE link SET srs = ? WHERE uuid = ? AND srs = ?",
                    arrayOf<Any>(
                        "GCJ02_MAINLAND_CHINA",
                        Uuid.parse("64b0b360-24ec-4113-9056-314223c6e19a").toByteArray(),
                        "GCJ02",
                    )
                )
                db.execSQL(
                    "UPDATE link SET srs = ? WHERE uuid = ? AND srs = ?",
                    arrayOf<Any>(
                        "GCJ02_MAINLAND_CHINA",
                        Uuid.parse("9d7cd113-ce01-4b8b-82fe-856956b8b20a").toByteArray(),
                        "GCJ02",
                    )
                )
            }
        },
        object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update OpenStreetMap display URL to show marker
                db.execSQL(
                    "UPDATE link SET coordsUriTemplate = ? WHERE uuid = ? AND coordsUriTemplate = ?",
                    arrayOf<Any>(
                        "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map={z}/{lat}/{lon}",
                        Uuid.parse("a771fd79-291e-4e55-9952-601f87b05bfe").toByteArray(),
                        "https://www.openstreetmap.org/#map={z}/{lat}/{lon}",
                    )
                )
                // Insert Zoom Earth
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "Zoom Earth",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://zoom.earth/maps/satellite/#view={lat},{lon},{z}",
                        "",
                        1776348228136,
                        Uuid.parse("a0b5b493-31a7-410d-9fe2-285974738ff1").toByteArray(),
                    )
                )
            }
        },
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert Google Maps Plus Code
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Google Maps",
                        "Google Maps Plus Code",
                        "GCJ02_MAINLAND_CHINA",
                        "DISPLAY",
                        0,
                        0,
                        1,
                        "https://www.google.com/maps/place/{plus_code}",
                        "",
                        1776849143380,
                        Uuid.parse("5c891351-3f66-4aa2-83c4-f13c5a67fdef").toByteArray(),
                    )
                )
            }
        },
        object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert Géoportail
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "",
                        "Géoportail",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://cartes.gouv.fr/explorer-les-cartes?c={lon},{lat}&z={z}",
                        "",
                        1778680284986,
                        Uuid.parse("b0f1715a-6645-4ae6-a4ec-36d6e5f08c34").toByteArray(),
                    )
                )
            }
        },
        object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert Google Maps search
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Google Maps",
                        "Google Maps search",
                        "GCJ02_MAINLAND_CHINA",
                        "SEARCH",
                        1,
                        0,
                        1,
                        "https://www.google.com/maps/search/?api=1&query={q}",
                        "https://www.google.com/maps/search/?api=1&query={q}",
                        1786537384232,
                        Uuid.parse("bdd9982d-8441-41b6-81a5-abce959a09b3").toByteArray(),
                    )
                )
            }
        },
        object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert Cartes.app
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Cartes.app",
                        "Cartes.app",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://cartes.app/?perspective=non&clic={lat}%7C{lon}#{z}/{lat}/{lon}",
                        "https://cartes.app/?q={q}",
                        1790865562966,
                        Uuid.parse("4d7b8012-7f0e-478a-9858-634dc4009a58").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Cartes.app",
                        "Cartes.app navigation",
                        "WGS84",
                        "NAVIGATION",
                        0,
                        0,
                        0,
                        "https://cartes.app/?perspective=non&clic={lat}%7C{lon}&allez=-%3EPoint+sur+la+carte%7C%7C{lon}%7C{lat}#{z}/{lat}/{lon}",
                        "",
                        1790865562966,
                        Uuid.parse("06c9c666-0f65-4a42-b571-de5c1b6b0f13").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Cartes.app",
                        "Cartes.app search",
                        "WGS84",
                        "SEARCH",
                        0,
                        0,
                        0,
                        "https://cartes.app/?q={lat},{lon}",
                        "https://cartes.app/?q={q}",
                        1790865562966,
                        Uuid.parse("54ac6017-4988-40a5-86fe-63aac862fb20").toByteArray(),
                    )
                )
                // Insert Kagi Maps
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Kagi Maps",
                        "Kagi Maps",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://kagi.com/maps/info?ll={lat},{lon}&z={z}",
                        "https://kagi.com/maps/info?q={q}",
                        1790865562966,
                        Uuid.parse("7633131d-8485-46ce-8be8-8b05f0928fd8").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Kagi Maps",
                        "Kagi Maps navigation",
                        "WGS84",
                        "NAVIGATION",
                        0,
                        0,
                        0,
                        "https://kagi.com/maps/directions?q=|{lat},{lon}",
                        "https://kagi.com/maps/directions?q=|{q}",
                        1790865562966,
                        Uuid.parse("0f327575-c2c9-41e1-9bba-fb4af5e195a7").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Kagi Maps",
                        "Kagi Maps search",
                        "WGS84",
                        "SEARCH",
                        0,
                        0,
                        0,
                        "https://kagi.com/maps/search?q={q}",
                        "https://kagi.com/maps/search?q={q}",
                        1790865562966,
                        Uuid.parse("782ec7d5-8962-4a24-9ecb-1fc61418d60e").toByteArray(),
                    )
                )
            }
        },
        object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Insert Yahoo! Maps Japan
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Yahoo! Maps Japan",
                        "Yahoo! Maps Japan",
                        "WGS84",
                        "DISPLAY",
                        0,
                        0,
                        0,
                        "https://map.yahoo.co.jp/place?lat={lat}&lon={lon}&zoom={z}",
                        "https://map.yahoo.co.jp/search?q={q}",
                        1790947732269,
                        Uuid.parse("5274e6d4-c675-4d44-a36c-b5d36e717624").toByteArray(),
                    )
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO link (`group`,`name`,`srs`,`type`,`appEnabled`,`chipEnabled`,`sheetEnabled`,`coordsUriTemplate`,`nameUriTemplate`,`createdAt`,`uuid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        "Yahoo! Maps Japan",
                        "Yahoo! Maps Japan search",
                        "WGS84",
                        "SEARCH",
                        0,
                        0,
                        0,
                        "",
                        "https://map.yahoo.co.jp/search?q={q}",
                        1790947732269,
                        Uuid.parse("7db7be61-657c-47e7-9b10-68b40cd914b4").toByteArray(),
                    )
                )
            }
        },
        object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update Cartes.app search URL -- remove coordinate template
                db.execSQL(
                    "UPDATE link SET coordsUriTemplate = ? WHERE uuid = ? AND coordsUriTemplate = ?",
                    arrayOf<Any>(
                        "",
                        Uuid.parse(CARTES_APP_SEARCH_UUID).toByteArray(),
                        "https://cartes.app/?q={lat},{lon}",
                    )
                )
                // Update Google Maps display -- make templates compatible with Telegram
                db.execSQL(
                    "UPDATE link SET coordsUriTemplate = ?, nameUriTemplate = ? WHERE uuid = ? AND coordsUriTemplate = ? AND nameUriTemplate = ?",
                    arrayOf<Any>(
                        "https://maps.google.com/?q={lat}%2C{lon}",
                        "https://maps.google.com/?q={q}",
                        Uuid.parse(GOOGLE_MAPS_DISPLAY_UUID).toByteArray(),
                        "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                        "https://www.google.com/maps/search/?api=1&query={q}",
                    )
                )
                // Update Google Maps search -- clear coordinate template
                db.execSQL(
                    "UPDATE link SET coordsUriTemplate = ? WHERE uuid = ? AND coordsUriTemplate = ?",
                    arrayOf<Any>(
                        "",
                        Uuid.parse(GOOGLE_MAPS_SEARCH_UUID).toByteArray(),
                        "https://www.google.com/maps/search/?api=1&query={q}",
                    )
                )
                // Update Google Maps Street View -- clear name template
                db.execSQL(
                    "UPDATE link SET nameUriTemplate = ? WHERE uuid = ? AND nameUriTemplate = ?",
                    arrayOf<Any>(
                        "",
                        Uuid.parse(GOOGLE_MAPS_STREET_VIEW_UUID).toByteArray(),
                        "https://www.google.com/maps/search/?api=1&query={q}",
                    )
                )
                // Update Kagi Maps search URL -- clear coordinate template
                db.execSQL(
                    "UPDATE link SET coordsUriTemplate = ? WHERE uuid = ? AND coordsUriTemplate = ?",
                    arrayOf<Any>(
                        "",
                        Uuid.parse(KAGI_MAPS_SEARCH_UUID).toByteArray(),
                        "https://kagi.com/maps/search?q={q}",
                    )
                )
            }
        },
    )

    const val APPLE_MAPS_DISPLAY_UUID = "ce900ea1-2c5d-4641-82f3-a5429a68d603"
    const val APPLE_MAPS_NAVIGATION_UUID = "a5092c63-cf5c-4225-9059-e888ae12e215"
    const val CARTES_APP_SEARCH_UUID = "54ac6017-4988-40a5-86fe-63aac862fb20"
    const val GOOGLE_MAPS_DISPLAY_UUID = "7bd96da4-beba-4a30-9dbd-b437a49a1dc0"
    const val GOOGLE_MAPS_SEARCH_UUID = "bdd9982d-8441-41b6-81a5-abce959a09b3"
    const val GOOGLE_MAPS_STREET_VIEW_UUID = "9d7cd113-ce01-4b8b-82fe-856956b8b20a"
    const val KAGI_MAPS_SEARCH_UUID = "782ec7d5-8962-4a24-9ecb-1fc61418d60e"
}
