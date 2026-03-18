package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import page.ooooo.geoshare.data.DefaultLinkRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkDao
import page.ooooo.geoshare.lib.point.Srs
import page.ooooo.geoshare.data.local.database.LinkType
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LinkModule {

    @Singleton
    @Provides
    fun provideLinkRepository(
        appDatabase: AppDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        linkDao: LinkDao,
    ): LinkRepository =
        DefaultLinkRepository(appDatabase, applicationScope, linkDao)
}

@OptIn(ExperimentalCoroutinesApi::class)
class FakeLinkRepository(
    initialFakeLinks: List<Link> = defaultFakeLinks,
) : LinkRepository {
    private val _fakeLinks: MutableStateFlow<List<Link>> = MutableStateFlow(initialFakeLinks)

    override val all = _fakeLinks.mapLatest { it.sortedBy { entry -> entry.createdAt }.reversed() }

    override suspend fun getAll() = _fakeLinks.value

    override suspend fun getByUid(uid: Int) =
        _fakeLinks.value.firstOrNull { it.uid == uid }

    override suspend fun getByUUID(uuid: UUID) =
        _fakeLinks.value.firstOrNull { it.uuid == uuid }

    override suspend fun insert(link: Link) =
        (_fakeLinks.value + listOf(link)).also {
            _fakeLinks.value = it
        }.size.toLong()

    override suspend fun update(vararg links: Link) {
        for (link in links) {
            _fakeLinks.value = _fakeLinks.value.map {
                if (it.uid == link.uid) {
                    link
                } else {
                    it
                }
            }
        }
    }

    override suspend fun delete(link: Link) {
        _fakeLinks.value = _fakeLinks.value.filterNot { it.uid == link.uid }
    }

    override suspend fun enable(uid: Int) {
        _fakeLinks.value = _fakeLinks.value.map {
            if (it.uid == uid) {
                it.copy(appEnabled = true)
            } else {
                it
            }
        }
    }

    override suspend fun disable(uid: Int) {
        _fakeLinks.value = _fakeLinks.value.map {
            if (it.uid == uid) {
                it.copy(appEnabled = false, chipEnabled = false, sheetEnabled = false)
            } else {
                it
            }
        }
    }

    override suspend fun disableGroup(group: String?) {
        _fakeLinks.value = _fakeLinks.value.map {
            if (it.group == group) {
                it.copy(appEnabled = false, chipEnabled = false, sheetEnabled = false)
            } else {
                it
            }
        }
    }

    override suspend fun restoreInitialData() {
        _fakeLinks.value = defaultFakeLinks
    }
}

val FakeOpenStreetMapDisplayLink = Link(
    group = "OpenStreetMap",
    name = "OpenStreetMap",
    appEnabled = true,
    chipEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://www.openstreetmap.org/#map={z}/{lat}/{lon}",
    nameUriTemplate = "https://www.openstreetmap.org/search?query={q}",
)
val FakeOpenStreetMapNavigationLink = Link(
    group = "OpenStreetMap",
    name = "OpenStreetMap navigation",
    type = LinkType.NAVIGATION,
    appEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://www.openstreetmap.org/directions?to={lat}%2C{lon}",
    nameUriTemplate = "https://www.openstreetmap.org/directions?to={q}",
)
val FakeGoogleMapsDisplayLink = Link(
    group = "Google Maps",
    name = "Google Maps",
    srs = Srs.GCJ02,
    appEnabled = true,
    chipEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
)
val FakeGoogleMapsNavigationLink = Link(
    group = "Google Maps",
    name = "Google Maps navigation",
    srs = Srs.GCJ02,
    type = LinkType.NAVIGATION,
    appEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
    nameUriTemplate = "https://www.google.com/maps/dir/?api=1&destination={q}",
)
val FakeGoogleMapsStreetViewLink = Link(
    group = "Google Maps",
    name = "Google Street View",
    srs = Srs.GCJ02,
    type = LinkType.STREET_VIEW,
    appEnabled = true,
    coordsUriTemplate = "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}", // Use display map URL, because Street View doesn't support search
)
val FakeAppleMapsDisplayLink = Link(
    group = "Apple Maps",
    name = "Apple Maps",
    appEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
    nameUriTemplate = "https://maps.apple.com/?q={q}",
)
val FakeAppleMapsNavigationLink = Link(
    group = "Apple Maps",
    name = "Apple Maps navigation",
    type = LinkType.NAVIGATION,
    appEnabled = true,
    sheetEnabled = true,
    coordsUriTemplate = "https://maps.apple.com/?daddr={lat}%2C{lon}",
    nameUriTemplate = "https://maps.apple.com/?daddr={q}",
)
val FakeMagicEarthDisplayLink = Link(
    group = "Magic Earth",
    name = "Magic Earth",
    appEnabled = true,
    coordsUriTemplate = "magicearth://?show_on_map&lat={lat}&lon={lon}&name={name}",
    nameUriTemplate = "magicearth://?open_search&q={q}",
)
val FakeMagicEarthNavigationLink = Link(
    group = "Magic Earth",
    name = "Magic Earth navigation",
    type = LinkType.NAVIGATION,
    appEnabled = true,
    coordsUriTemplate = "magicearth://?get_directions&lat={lat}&lon={lon}",
    nameUriTemplate = "magicearth://?get_directions&q={q}",
)

/**
 * Links for testing purposes. The actual links that the table is populated with are in
 * [page.ooooo.geoshare.data.local.database.AppDatabase.Companion.restoreInitialData].
 */
val defaultFakeLinks = listOf(
    FakeOpenStreetMapDisplayLink,
    FakeOpenStreetMapNavigationLink,
    FakeGoogleMapsDisplayLink,
    FakeGoogleMapsNavigationLink,
    FakeGoogleMapsStreetViewLink,
    FakeAppleMapsDisplayLink,
    FakeAppleMapsNavigationLink,
    FakeMagicEarthDisplayLink,
    FakeMagicEarthNavigationLink,
)
