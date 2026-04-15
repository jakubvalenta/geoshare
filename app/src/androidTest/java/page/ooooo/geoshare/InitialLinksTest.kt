package page.ooooo.geoshare

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkDao
import page.ooooo.geoshare.data.local.database.LinkType
import page.ooooo.geoshare.lib.geo.Srs
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class InitialLinksTest {
    private lateinit var linkDao: LinkDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    AppDatabase.restoreInitialData(db)
                }
            })
            .build()
        linkDao = db.getLinkDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun initialLinksAreInserted() = runBlocking {
        val expectedLinks = listOf(
            Link(
                group = "OpenStreetMap",
                name = "OpenStreetMap",
                chipEnabled = true,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.openstreetmap.org/#map={z}/{lat}/{lon}",
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
            Link(
                group = "Google Maps",
                name = "Google Maps",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                chipEnabled = true,
                sheetEnabled = true,
                coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uuid = UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0"),
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
                name = "Google Street View",
                srs = Srs.GCJ02_MAINLAND_CHINA,
                type = LinkType.STREET_VIEW,
                coordsUriTemplate = "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
                nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                uuid = UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a"),
            ),
            Link(
                group = "Apple Maps",
                name = "Apple Maps",
                sheetEnabled = true,
                coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                nameUriTemplate = "https://maps.apple.com/?q={q}",
                uuid = UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603"),
            ),
            Link(
                group = "Apple Maps",
                name = "Apple Maps navigation",
                type = LinkType.NAVIGATION,
                sheetEnabled = true,
                coordsUriTemplate = "https://maps.apple.com/?daddr={lat}%2C{lon}",
                nameUriTemplate = "https://maps.apple.com/?daddr={q}",
                uuid = UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215"),
            ),
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
            Link(
                name = "GasBuddy",
                appEnabled = false,
                coordsUriTemplate = "https://www.gasbuddy.com/gaspricemap?lat={lat}&lng={lon}&z={z}",
                uuid = UUID.fromString("fd89f6f0-694e-4d96-b604-ed15e2530a2d"),
            ),
            @Suppress("SpellCheckingInspection")
            Link(
                name = "KartaView",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://kartaview.org/map/@{lat}%2C{lon},{z}z",
                uuid = UUID.fromString("7e09855d-d29b-4c18-944f-7fa440db3528"),
            ),
            @Suppress("SpellCheckingInspection")
            Link(
                name = "Mapilio",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://mapilio.com/app?lat={lat}&lng={lon}&zoom={z}",
                uuid = UUID.fromString("c206d165-b2db-4030-a415-203e92cacb66"),
            ),
            @Suppress("SpellCheckingInspection")
            Link(
                name = "Panoramax",
                type = LinkType.STREET_VIEW,
                appEnabled = false,
                coordsUriTemplate = "https://panoramax.openstreetmap.fr/#background=streets&focus=map&map={z}/{lat}/{lon}&speed=250",
                uuid = UUID.fromString("c80ccfa6-6d22-4290-b8f5-82119f87a570"),
            ),
            Link(
                name = "PeakVisor",
                appEnabled = false,
                coordsUriTemplate = "https://peakvisor.com/embed?lat={lat}&lng={lon}&alt=3000&yaw=160",
                uuid = UUID.fromString("fe5de45b-ba0f-4d87-8ee9-979f1a701978"),
            ),
            Link(
                name = "Refuges",
                appEnabled = false,
                coordsUriTemplate = "https://www.refuges.info/nav?map={z}/{lon}/{lat}",
                uuid = UUID.fromString("1aad2356-d900-45e5-91a0-d7ee2092641d"),
            ),
            Link(
                name = "uMap",
                appEnabled = false,
                coordsUriTemplate = "https://umap.openstreetmap.fr/en/map/test-mapy_626288#{z}/{lat}/{lon}",
                uuid = UUID.fromString("94e1350a-3599-43b3-858b-59750a6f8680"),
            ),
        ).sortedBy { it.name }
        val actualLinks = linkDao.getAll()
        assertEquals(expectedLinks.size, actualLinks.size)
        for ((expectedLink, actualLink) in expectedLinks.zip(actualLinks)) {
            assertEquals(
                expectedLink.copy(createdAt = 0L, uid = 0),
                actualLink.copy(createdAt = 0L, uid = 0),
            )
        }
    }
}
