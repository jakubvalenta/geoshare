package page.ooooo.geoshare.data

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeAppleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeAppleMapsNavigationLink
import page.ooooo.geoshare.data.di.FakeGoogleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeGoogleMapsNavigationLink
import page.ooooo.geoshare.data.di.FakeGoogleMapsStreetViewLink
import page.ooooo.geoshare.data.di.FakeMagicEarthDisplayLink
import page.ooooo.geoshare.data.di.FakeMagicEarthNavigationLink
import page.ooooo.geoshare.data.di.FakeOpenStreetMapDisplayLink
import page.ooooo.geoshare.data.di.FakeOpenStreetMapNavigationLink
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDegMinSecAutomation
import page.ooooo.geoshare.data.local.preferences.CopyGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.OpenStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SavePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointToContactOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.SendPointOutput
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareStreetViewGoogleUriOutput
import java.util.UUID

class OutputRepositoryTest {
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val outputRepository = OutputRepository(
        coordinateConverter = coordinateConverter,
    )

    @Test
    fun getOutputsForPoint_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                CopyCoordsDecOutput(coordinateConverter),
                CopyCoordsDegMinSecOutput(coordinateConverter),
                CopyGeoUriOutput(coordinateConverter),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
                CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter),
                ShareDisplayGeoUriOutput(coordinateConverter),
                ShareNavigationGoogleUriOutput(coordinateConverter),
                ShareStreetViewGoogleUriOutput(coordinateConverter),
                SavePointGpxOutput(coordinateConverter),
                SavePointToContactOutput(coordinateConverter),
            ),
            outputRepository.getOutputsForPoint(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPoints_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(coordinateConverter),
                SharePointsGpxOutput(coordinateConverter),
                SaveRouteGpxOutput(coordinateConverter),
                SavePointsGpxOutput(coordinateConverter),
            ),
            outputRepository.getOutputsForPoints(),
        )
    }

    @Test
    fun getOutputsForApps_returnOutputsThatSupportPassedPackageNamesAndDataTypes() {
        assertEquals(
            mapOf(
                PackageNames.TEST to listOf(
                    OpenDisplayGeoUriOutput(PackageNames.TEST, coordinateConverter)
                ),
                PackageNames.GOOGLE_MAPS to listOf(
                    OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter),
                    OpenNavigationGoogleUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter),
                    OpenStreetViewGoogleUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter),
                    OpenRouteGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter),
                    OpenPointsGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter),
                ),
                PackageNames.MAGIC_EARTH to listOf(
                    OpenDisplayMagicEarthUriOutput(PackageNames.MAGIC_EARTH, coordinateConverter),
                    OpenNavigationMagicEarthUriOutput(PackageNames.MAGIC_EARTH, coordinateConverter),
                ),
                PackageNames.SIGNAL to listOf(
                    SendPointOutput(PackageNames.SIGNAL, coordinateConverter),
                ),
                PackageNames.TOMTOM to listOf(
                    OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter),
                ),
                "${PackageNames.TEST}.empty" to emptyList(),
            ),
            outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.TEST to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(
                        DataType.GEO_URI,
                        DataType.GOOGLE_NAVIGATION_URI,
                        DataType.GOOGLE_STREET_VIEW_URI,
                        DataType.GPX_DATA,
                    ),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.SIGNAL to setOf(DataType.SEND_PLAIN_TEXT),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                    "${PackageNames.TEST}.empty" to emptySet(),
                ),
                emptySet(),
            ),
        )
    }

    @Test
    fun getOutputsForApps_doesNotReturnOutputsForHiddenApps() {
        assertEquals(
            mapOf(
                PackageNames.TEST to listOf(
                    OpenDisplayGeoUriOutput(PackageNames.TEST, coordinateConverter)
                ),
                PackageNames.TOMTOM to listOf(
                    OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter),
                ),
            ),
            outputRepository.getOutputsForApps(
                mapOf(
                    PackageNames.TEST to setOf(DataType.GEO_URI),
                    PackageNames.GOOGLE_MAPS to setOf(DataType.GOOGLE_NAVIGATION_URI),
                    PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                    PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                setOf(
                    PackageNames.GOOGLE_MAPS,
                    PackageNames.MAGIC_EARTH,
                ),
            ),
        )
    }

    @Test
    fun getOutputsForSharing_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareDisplayGeoUriOutput(coordinateConverter),
                ShareNavigationGoogleUriOutput(coordinateConverter),
                ShareStreetViewGoogleUriOutput(coordinateConverter),
                ShareRouteGpxOutput(coordinateConverter),
                SharePointsGpxOutput(coordinateConverter),
                SavePointToContactOutput(coordinateConverter),
            ),
            outputRepository.getOutputsForSharing(),
        )
    }

    @Test
    fun getOutputsForLinks_returnsOutputsForAllPassedLinks() {
        assertEquals(
            mapOf(
                "Google Maps" to listOf(
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter),
                ),
                "Apple Maps" to listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                ),
                "Magic Earth" to listOf(
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter),
                ),
                "OpenStreetMap" to listOf(
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter),
                ),
            ),
            outputRepository.getOutputsForLinks(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForLinks_returnsOutputsGroupedByLinkGroupOrName() {
        val linkInFirstGroupWithoutExplicitGroup = Link(
            group = "",
            name = "first",
            appEnabled = true,
            coordsUriTemplate = "https://www.example.com/first/{lat}/{lon}",
        )
        val linkInFirstGroup = Link(
            group = "first",
            name = "first navigation",
            appEnabled = true,
            coordsUriTemplate = "https://www.example.com/first/navigation/{lat}/{lon}",
        )
        val linkInSecondGroup = Link(
            group = "second",
            name = "second",
            appEnabled = true,
            coordsUriTemplate = "https://www.example.com/second/{lat}/{lon}",
        )
        assertEquals(
            mapOf(
                "first" to listOf(
                    ShareLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, coordinateConverter),
                    ShareLinkUriOutput(linkInFirstGroup, coordinateConverter),
                    CopyLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, coordinateConverter),
                    CopyLinkUriOutput(linkInFirstGroup, coordinateConverter),
                ),
                "second" to listOf(
                    ShareLinkUriOutput(linkInSecondGroup, coordinateConverter),
                    CopyLinkUriOutput(linkInSecondGroup, coordinateConverter),
                ),
            ),
            outputRepository.getOutputsForLinks(
                listOf(
                    linkInFirstGroupWithoutExplicitGroup,
                    linkInFirstGroup,
                    linkInSecondGroup,
                )
            ),
        )
    }

    @Test
    fun getOutputsForPointChips_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                CopyGeoUriOutput(coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
            ),
            outputRepository.getOutputsForPointChips(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPointsChips_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(coordinateConverter),
                SaveRouteGpxOutput(coordinateConverter),
                SavePointsGpxOutput(coordinateConverter),
            ),
            outputRepository.getOutputsForPointsChips(),
        )
    }

    @Test
    fun getAutomationOutput_convertsAllAutomationsToOutputs() = runTest {
        assertEquals(
            listOf(
                listOf(
                    NoopOutput(),
                ),
                listOf(
                    CopyCoordsDecOutput(coordinateConverter),
                    CopyCoordsDegMinSecOutput(coordinateConverter),
                    CopyGeoUriOutput(coordinateConverter),
                    ShareDisplayGeoUriOutput(coordinateConverter),
                    ShareNavigationGoogleUriOutput(coordinateConverter),
                    ShareStreetViewGoogleUriOutput(coordinateConverter),
                    SavePointGpxOutput(coordinateConverter),
                    SavePointToContactOutput(coordinateConverter),
                ),
                listOf(
                    ShareRouteGpxOutput(coordinateConverter),
                    SharePointsGpxOutput(coordinateConverter),
                    SaveRouteGpxOutput(coordinateConverter),
                    SavePointsGpxOutput(coordinateConverter),
                ),
                listOf(
                    OpenDisplayMagicEarthUriOutput(PackageNames.MAGIC_EARTH, coordinateConverter),
                    OpenNavigationMagicEarthUriOutput(PackageNames.MAGIC_EARTH, coordinateConverter),
                    SendPointOutput(PackageNames.SIGNAL, coordinateConverter),
                    OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter),
                    OpenDisplayGeoUriOutput(PackageNames.TEST, coordinateConverter),
                ),
                listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter),
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter),
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter),
                ),
            ),
            listOf(
                listOf(
                    NoopAutomation,
                ),
                listOf(
                    CopyCoordsDecAutomation,
                    CopyCoordsDegMinSecAutomation,
                    CopyGeoUriAutomation,
                    ShareDisplayGeoUriAutomation,
                    ShareNavigationGoogleUriAutomation,
                    ShareStreetViewGoogleUriAutomation,
                    SavePointGpxAutomation,
                    SavePointToContactAutomation,
                ),
                listOf(
                    ShareRouteGpxAutomation,
                    SharePointsGpxAutomation,
                    SaveRouteGpxAutomation,
                    SavePointsGpxAutomation,
                ),
                listOf(
                    OpenDisplayMagicEarthUriAutomation(PackageNames.MAGIC_EARTH),
                    OpenNavigationMagicEarthUriAutomation(PackageNames.MAGIC_EARTH),
                    SendPointAutomation(PackageNames.SIGNAL),
                    OpenRouteOnePointGpxAutomation(PackageNames.TOMTOM),
                    OpenDisplayGeoUriAutomation(PackageNames.TEST),
                ),
                listOf(
                    ShareLinkUriAutomation(FakeAppleMapsDisplayLink.uuid),
                    ShareLinkUriAutomation(FakeAppleMapsNavigationLink.uuid),
                    CopyLinkUriAutomation(FakeAppleMapsDisplayLink.uuid),
                    CopyLinkUriAutomation(FakeAppleMapsNavigationLink.uuid),
                    ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid),
                    ShareLinkUriAutomation(FakeGoogleMapsNavigationLink.uuid),
                    ShareLinkUriAutomation(FakeGoogleMapsStreetViewLink.uuid),
                    CopyLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid),
                    CopyLinkUriAutomation(FakeGoogleMapsNavigationLink.uuid),
                    CopyLinkUriAutomation(FakeGoogleMapsStreetViewLink.uuid),
                    ShareLinkUriAutomation(FakeMagicEarthDisplayLink.uuid),
                    ShareLinkUriAutomation(FakeMagicEarthNavigationLink.uuid),
                    CopyLinkUriAutomation(FakeMagicEarthDisplayLink.uuid),
                    CopyLinkUriAutomation(FakeMagicEarthNavigationLink.uuid),
                    ShareLinkUriAutomation(FakeOpenStreetMapDisplayLink.uuid),
                    ShareLinkUriAutomation(FakeOpenStreetMapNavigationLink.uuid),
                    CopyLinkUriAutomation(FakeOpenStreetMapDisplayLink.uuid),
                    CopyLinkUriAutomation(FakeOpenStreetMapNavigationLink.uuid),
                ),
            ).map { group ->
                group.map { automation ->
                    outputRepository.getAutomationOutput(
                        automation = automation,
                        getLinkByUUID = { defaultFakeLinks.findByUUID(it) },
                    )
                }
            },
        )
    }

    @Test
    fun getAutomationOutput_convertsAllOldAutomationsToOutputs() = runTest {
        assertEquals(
            listOf(
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter),
                CopyCoordsDecOutput(coordinateConverter),
                CopyCoordsDegMinSecOutput(coordinateConverter),
                CopyGeoUriOutput(coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter),
                CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter),
                CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter),
                NoopOutput(),
                OpenDisplayGeoUriOutput(PackageNames.TEST, coordinateConverter),
                OpenNavigationGoogleUriOutput(PackageNames.TEST, coordinateConverter),
                OpenStreetViewGoogleUriOutput(PackageNames.TEST, coordinateConverter),
                OpenRouteOnePointGpxOutput(PackageNames.TEST, coordinateConverter),
                OpenNavigationMagicEarthUriOutput(PackageNames.TEST, coordinateConverter),
                SavePointsGpxOutput(coordinateConverter),
                ShareDisplayGeoUriOutput(coordinateConverter),
                ShareRouteGpxOutput(coordinateConverter),
            ),
            listOf(
                CopyLinkUriAutomation(UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215")),
                CopyLinkUriAutomation(UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603")),
                CopyCoordsDecAutomation,
                CopyCoordsDegMinSecAutomation,
                CopyGeoUriAutomation,
                CopyLinkUriAutomation(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a")),
                CopyLinkUriAutomation(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a")),
                CopyLinkUriAutomation(UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0")),
                CopyLinkUriAutomation(UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7")),
                CopyLinkUriAutomation(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07")),
                NoopAutomation,
                OpenDisplayGeoUriAutomation(PackageNames.TEST),
                OpenNavigationGoogleUriAutomation(PackageNames.TEST),
                OpenStreetViewGoogleUriAutomation(PackageNames.TEST),
                OpenRouteOnePointGpxAutomation(PackageNames.TEST),
                OpenNavigationMagicEarthUriAutomation(PackageNames.TEST),
                SavePointsGpxAutomation,
                ShareDisplayGeoUriAutomation,
                ShareRouteGpxAutomation,
            ).map { automation ->
                outputRepository.getAutomationOutput(
                    automation = automation,
                    getLinkByUUID = {
                        when (it) {
                            UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603") -> FakeAppleMapsDisplayLink
                            UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215") -> FakeAppleMapsNavigationLink
                            UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0") -> FakeGoogleMapsDisplayLink
                            UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a") -> FakeGoogleMapsNavigationLink
                            UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a") -> FakeGoogleMapsStreetViewLink
                            UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07") -> FakeMagicEarthDisplayLink
                            UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7") -> FakeMagicEarthNavigationLink
                            else -> null
                        }
                    },
                )
            },
        )
    }
}
