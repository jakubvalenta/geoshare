package page.ooooo.geoshare.tests

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkType
import page.ooooo.geoshare.lib.geo.Srs
import java.util.UUID

class InitialLinksTest : InitialDataTest {
    override lateinit var db: AppDatabase

    override fun restore(db: SupportSQLiteDatabase) = InitialLinks.restore(db)

    @Test
    @Throws(Exception::class)
    fun initialLinksAreInserted() = runBlocking {
        val linkDao = db.getLinkDao()
        val expectedItems = listOf(
            // Apple Maps
            Link(
                group = "Apple Maps",
                name = "Apple Maps",
                sheetEnabled = true,
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                nameUriTemplate = "https://maps.apple.com/?q={q}",
                uuid = UUID.fromString(InitialLinks.APPLE_MAPS_DISPLAY_UUID),
            ),
            Link(
                group = "Apple Maps",
                name = "Apple Maps navigation",
                type = LinkType.NAVIGATION,
                sheetEnabled = true,
                coordsUriTemplate = "https://maps.apple.com/?daddr={lat}%2C{lon}",
                nameUriTemplate = "https://maps.apple.com/?daddr={q}",
                uuid = UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID),
            ),
            // Cartes.app
            Link(
                group = "Cartes.app",
                name = "Cartes.app",
                appEnabled = false,
                coordsUriTemplate = "https://cartes.app/?perspective=non&clic={lat}%7C{lon}#{z}/{lat}/{lon}",
                nameUriTemplate = "https://cartes.app/?q={q}",
                uuid = UUID.fromString("4d7b8012-7f0e-478a-9858-634dc4009a58"),
            ),
            Link(
                group = "Cartes.app",
                name = "Cartes.app navigation",
                type = LinkType.NAVIGATION,
                appEnabled = false,
                coordsUriTemplate = "https://cartes.app/?perspective=non&clic={lat}%7C{lon}&allez=-%3EPoint+sur+la+carte%7C%7C{lon}%7C{lat}#{z}/{lat}/{lon}",
                uuid = UUID.fromString("06c9c666-0f65-4a42-b571-de5c1b6b0f13"),
            ),
            Link(
                group = "Cartes.app",
                name = "Cartes.app search",
                type = LinkType.SEARCH,
                appEnabled = false,
                coordsUriTemplate = "https://cartes.app/?q={lat},{lon}",
                nameUriTemplate = "https://cartes.app/?q={q}",
                uuid = UUID.fromString("54ac6017-4988-40a5-86fe-63aac862fb20"),
            ),
            // GasBuddy
            Link(
                name = "GasBuddy",
                appEnabled = false,
                coordsUriTemplate = "https://www.gasbuddy.com/gaspricemap?lat={lat}&lng={lon}&z={z}",
                uuid = UUID.fromString("fd89f6f0-694e-4d96-b604-ed15e2530a2d"),
            ),
            // Géoportail
            Link(
                name = "Géoportail",
                appEnabled = false,
                coordsUriTemplate = "https://cartes.gouv.fr/explorer-les-cartes?c={lon},{lat}&z={z}",
                uuid = UUID.fromString("b0f1715a-6645-4ae6-a4ec-36d6e5f08c34"),
            ),
            // Google Maps
            Link(
                group = "Google Maps",
                name = "Google Maps",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                chipEnabled = true,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uuid = UUID.fromString(InitialLinks.GOOGLE_MAPS_DISPLAY_UUID),
            ),
            Link(
                group = "Google Maps",
                name = "Google Maps navigation",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                type = LinkType.NAVIGATION,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/dir/?api=1&destination={q}",
                uuid = UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a"),
            ),
            Link(
                group = "Google Maps",
                name = "Google Maps search",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                type = LinkType.SEARCH,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uuid = UUID.fromString("bdd9982d-8441-41b6-81a5-abce959a09b3"),
            ),
            Link(
                group = "Google Maps",
                name = "Google Street View",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                type = LinkType.STREET_VIEW,
                coordsUriTemplate = "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uuid = UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a"),
            ),
            Link(
                group = "Google Maps",
                name = "Google Maps Plus Code",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                appEnabled = false,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.google.com/maps/place/{plus_code}",
                uuid = UUID.fromString("5c891351-3f66-4aa2-83c4-f13c5a67fdef"),
            ),
            // Kagi Maps
            Link(
                group = "Kagi Maps",
                name = "Kagi Maps",
                appEnabled = false,
                coordsUriTemplate = "https://kagi.com/maps/info?ll={lat},{lon}&z={z}",
                nameUriTemplate = "https://kagi.com/maps/info?q={q}",
                uuid = UUID.fromString("7633131d-8485-46ce-8be8-8b05f0928fd8"),
            ),
            Link(
                group = "Kagi Maps",
                name = "Kagi Maps navigation",
                type = LinkType.NAVIGATION,
                appEnabled = false,
                coordsUriTemplate = "https://kagi.com/maps/directions?q=|{lat},{lon}",
                nameUriTemplate = "https://kagi.com/maps/directions?q=|{q}",
                uuid = UUID.fromString("0f327575-c2c9-41e1-9bba-fb4af5e195a7"),
            ),
            Link(
                group = "Kagi Maps",
                name = "Kagi Maps search",
                type = LinkType.SEARCH,
                appEnabled = false,
                coordsUriTemplate = "https://kagi.com/maps/search?q={q}",
                nameUriTemplate = "https://kagi.com/maps/search?q={q}",
                uuid = UUID.fromString("782ec7d5-8962-4a24-9ecb-1fc61418d60e"),
            ),
            // KartaView
            Link(
                name = "KartaView",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://kartaview.org/map/@{lat}%2C{lon},{z}z",
                uuid = UUID.fromString("7e09855d-d29b-4c18-944f-7fa440db3528"),
            ),
            // Magic Earth
            Link(
                group = "Magic Earth",
                name = "Magic Earth",
                appEnabled = false,
                sheetEnabled = true,
                coordsUriTemplate = "magicearth://?show_on_map&lat={lat}&lon={lon}&name={name}",
                nameUriTemplate = "magicearth://?open_search&q={q}",
                uuid = UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07"),
            ),
            Link(
                group = "Magic Earth",
                name = "Magic Earth navigation",
                appEnabled = false,
                sheetEnabled = true,
                type = LinkType.NAVIGATION,
                coordsUriTemplate = "magicearth://?get_directions&lat={lat}&lon={lon}",
                nameUriTemplate = "magicearth://?get_directions&q={q}",
                uuid = UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7"),
            ),
            // Mapilio
            Link(
                name = "Mapilio",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://mapilio.com/app?lat={lat}&lng={lon}&zoom={z}",
                uuid = UUID.fromString("c206d165-b2db-4030-a415-203e92cacb66"),
            ),
            // OpenStreetMap
            Link(
                group = "OpenStreetMap",
                name = "OpenStreetMap",
                chipEnabled = true,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map={z}/{lat}/{lon}",
                nameUriTemplate = "https://www.openstreetmap.org/search?query={q}",
                uuid = UUID.fromString("a771fd79-291e-4e55-9952-601f87b05bfe"),
            ),
            Link(
                group = "OpenStreetMap",
                name = "OpenStreetMap navigation",
                srs = Srs.WGS84,
                type = LinkType.NAVIGATION,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.openstreetmap.org/directions?to={lat}%2C{lon}",
                nameUriTemplate = "https://www.openstreetmap.org/directions?to={q}",
                uuid = UUID.fromString("dad7a723-eeb1-4f60-af5d-7813b3cc1926"),
            ),
            // Panoramax
            Link(
                name = "Panoramax",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://panoramax.openstreetmap.fr/#background=streets&focus=map&map={z}/{lat}/{lon}&speed=250",
                uuid = UUID.fromString("c80ccfa6-6d22-4290-b8f5-82119f87a570"),
            ),
            // PeakVisor
            Link(
                name = "PeakVisor",
                appEnabled = false,
                coordsUriTemplate = "https://peakvisor.com/embed?lat={lat}&lng={lon}&alt=3000&yaw=160",
                uuid = UUID.fromString("fe5de45b-ba0f-4d87-8ee9-979f1a701978"),
            ),
            // Refuges
            Link(
                name = "Refuges",
                appEnabled = false,
                coordsUriTemplate = "https://www.refuges.info/nav?map={z}/{lon}/{lat}",
                uuid = UUID.fromString("1aad2356-d900-45e5-91a0-d7ee2092641d"),
            ),
            // uMap
            Link(
                name = "uMap",
                appEnabled = false,
                coordsUriTemplate = "https://umap.openstreetmap.fr/en/map/test-mapy_626288#{z}/{lat}/{lon}",
                uuid = UUID.fromString("94e1350a-3599-43b3-858b-59750a6f8680"),
            ),
            // Zoom Earth
            Link(
                name = "Zoom Earth",
                appEnabled = false,
                coordsUriTemplate = "https://zoom.earth/maps/satellite/#view={lat},{lon},{z}",
                uuid = UUID.fromString("a0b5b493-31a7-410d-9fe2-285974738ff1"),
            ),
        ).sortedBy { it.name }
        val actualItems = linkDao.getAll()
        assertEquals(expectedItems.size, actualItems.size)
        for ((expectedItem, actualItem) in expectedItems.zip(actualItems)) {
            assertEquals(
                expectedItem.copy(createdAt = 0, uid = 0),
                actualItem.copy(createdAt = 0, uid = 0),
            )
        }
    }
}
