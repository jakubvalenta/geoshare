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
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.local.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAGIC_EARTH_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TEST_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.formatters.MagicEarthUriFormatter
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
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
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.point.CoordinateConverter
import java.util.UUID

class OutputRepositoryTest {
    private val mockContext: Context = mock {}
    private val chinaGeometry = ChinaGeometry(mockContext)
    private val coordinateConverter = CoordinateConverter(chinaGeometry)
    private val coordinateFormatter = CoordinateFormatter(coordinateConverter)
    private val geoUriFormatter = GeoUriFormatter(coordinateConverter)
    private val googleMapsUriFormatter = GoogleMapsUriFormatter(coordinateConverter)
    private val gpxFormatter = GpxFormatter(coordinateConverter)
    private val magicEarthUriFormatter = MagicEarthUriFormatter(coordinateConverter)
    private val uriFormatter = UriFormatter(coordinateConverter)
    private val outputRepository = OutputRepository(
        coordinateFormatter = coordinateFormatter,
        geoUriFormatter = geoUriFormatter,
        googleMapsUriFormatter = googleMapsUriFormatter,
        gpxFormatter = gpxFormatter,
        magicEarthUriFormatter = magicEarthUriFormatter,
        uriFormatter = uriFormatter,
    )

    @Test
    fun getOutputsForPoint_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                CopyCoordsDecOutput(coordinateFormatter),
                CopyCoordsDegMinSecOutput(coordinateFormatter),
                CopyGeoUriOutput(geoUriFormatter),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
                CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, uriFormatter),
                ShareDisplayGeoUriOutput(geoUriFormatter),
                ShareNavigationGoogleUriOutput(googleMapsUriFormatter),
                ShareStreetViewGoogleUriOutput(googleMapsUriFormatter),
                SavePointGpxOutput(gpxFormatter),
            ),
            outputRepository.getOutputsForPoint(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPoints_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(gpxFormatter),
                SharePointsGpxOutput(gpxFormatter),
                SaveRouteGpxOutput(gpxFormatter),
                SavePointsGpxOutput(gpxFormatter),
            ),
            outputRepository.getOutputsForPoints(),
        )
    }

    @Test
    fun getOutputsForApps_returnOutputsThatSupportPassedPackageNamesAndDataTypes() {
        assertEquals(
            mapOf(
                TEST_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormatter)
                ),
                GOOGLE_MAPS_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter),
                    OpenNavigationGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME, googleMapsUriFormatter),
                    OpenStreetViewGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME, googleMapsUriFormatter),
                    OpenRouteGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormatter),
                    OpenPointsGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormatter),
                ),
                MAGIC_EARTH_PACKAGE_NAME to listOf(
                    OpenDisplayMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormatter),
                    OpenNavigationMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormatter),
                ),
                TOMTOM_PACKAGE_NAME to listOf(
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter),
                ),
                "${TEST_PACKAGE_NAME}.empty" to emptyList(),
            ),
            outputRepository.getOutputsForApps(
                mapOf(
                    TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                    GOOGLE_MAPS_PACKAGE_NAME to setOf(
                        DataType.GEO_URI,
                        DataType.GOOGLE_NAVIGATION_URI,
                        DataType.GOOGLE_STREET_VIEW_URI,
                        DataType.GPX_DATA,
                    ),
                    MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                    TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                    "${TEST_PACKAGE_NAME}.empty" to emptySet(),
                ),
                emptySet(),
            ),
        )
    }

    @Test
    fun getOutputsForApps_doesNotReturnOutputsForHiddenApps() {
        assertEquals(
            mapOf(
                TEST_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormatter)
                ),
                TOMTOM_PACKAGE_NAME to listOf(
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter),
                ),
            ),
            outputRepository.getOutputsForApps(
                mapOf(
                    TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                    GOOGLE_MAPS_PACKAGE_NAME to setOf(DataType.GOOGLE_NAVIGATION_URI),
                    MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                    TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                ),
                setOf(
                    GOOGLE_MAPS_PACKAGE_NAME,
                    MAGIC_EARTH_PACKAGE_NAME,
                ),
            ),
        )
    }

    @Test
    fun getOutputsForSharing_returnsOutputsThatSupportHardCodedDataTypes() {
        assertEquals(
            listOf(
                ShareDisplayGeoUriOutput(geoUriFormatter),
                ShareNavigationGoogleUriOutput(googleMapsUriFormatter),
                ShareStreetViewGoogleUriOutput(googleMapsUriFormatter),
                ShareRouteGpxOutput(gpxFormatter),
                SharePointsGpxOutput(gpxFormatter),
            ),
            outputRepository.getOutputsForSharing(),
        )
    }

    @Test
    fun getOutputsForLinks_returnsOutputsForAllPassedLinks() {
        assertEquals(
            mapOf(
                "Google Maps" to listOf(
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, uriFormatter),
                ),
                "Apple Maps" to listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                ),
                "Magic Earth" to listOf(
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, uriFormatter),
                ),
                "OpenStreetMap" to listOf(
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, uriFormatter),
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
                    ShareLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, uriFormatter),
                    ShareLinkUriOutput(linkInFirstGroup, uriFormatter),
                    CopyLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, uriFormatter),
                    CopyLinkUriOutput(linkInFirstGroup, uriFormatter),
                ),
                "second" to listOf(
                    ShareLinkUriOutput(linkInSecondGroup, uriFormatter),
                    CopyLinkUriOutput(linkInSecondGroup, uriFormatter),
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
                CopyGeoUriOutput(geoUriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
            ),
            outputRepository.getOutputsForPointChips(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPointsChips_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(gpxFormatter),
                SaveRouteGpxOutput(gpxFormatter),
                SavePointsGpxOutput(gpxFormatter),
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
                    CopyCoordsDecOutput(coordinateFormatter),
                    CopyCoordsDegMinSecOutput(coordinateFormatter),
                    CopyGeoUriOutput(geoUriFormatter),
                    ShareDisplayGeoUriOutput(geoUriFormatter),
                    ShareNavigationGoogleUriOutput(googleMapsUriFormatter),
                    ShareStreetViewGoogleUriOutput(googleMapsUriFormatter),
                    SavePointGpxOutput(gpxFormatter),
                ),
                listOf(
                    ShareRouteGpxOutput(gpxFormatter),
                    SharePointsGpxOutput(gpxFormatter),
                    SaveRouteGpxOutput(gpxFormatter),
                    SavePointsGpxOutput(gpxFormatter),
                ),
                listOf(
                    OpenDisplayMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormatter),
                    OpenNavigationMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormatter),
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter),
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormatter),
                ),
                listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, uriFormatter),
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, uriFormatter),
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, uriFormatter),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriFormatter),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, uriFormatter),
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
                ),
                listOf(
                    ShareRouteGpxAutomation,
                    SharePointsGpxAutomation,
                    SaveRouteGpxAutomation,
                    SavePointsGpxAutomation,
                ),
                listOf(
                    OpenDisplayMagicEarthUriAutomation(MAGIC_EARTH_PACKAGE_NAME),
                    OpenNavigationMagicEarthUriAutomation(MAGIC_EARTH_PACKAGE_NAME),
                    OpenRouteOnePointGpxAutomation(TOMTOM_PACKAGE_NAME),
                    OpenDisplayGeoUriAutomation(TEST_PACKAGE_NAME),
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
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriFormatter),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriFormatter),
                CopyCoordsDecOutput(coordinateFormatter),
                CopyCoordsDegMinSecOutput(coordinateFormatter),
                CopyGeoUriOutput(geoUriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, uriFormatter),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter),
                CopyLinkUriOutput(FakeMagicEarthNavigationLink, uriFormatter),
                CopyLinkUriOutput(FakeMagicEarthDisplayLink, uriFormatter),
                NoopOutput(),
                OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormatter),
                OpenNavigationGoogleUriOutput(TEST_PACKAGE_NAME, googleMapsUriFormatter),
                OpenStreetViewGoogleUriOutput(TEST_PACKAGE_NAME, googleMapsUriFormatter),
                OpenRouteOnePointGpxOutput(TEST_PACKAGE_NAME, gpxFormatter),
                OpenNavigationMagicEarthUriOutput(TEST_PACKAGE_NAME, magicEarthUriFormatter),
                SavePointsGpxOutput(gpxFormatter),
                ShareDisplayGeoUriOutput(geoUriFormatter),
                ShareRouteGpxOutput(gpxFormatter),
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
                OpenDisplayGeoUriAutomation(TEST_PACKAGE_NAME),
                OpenNavigationGoogleUriAutomation(TEST_PACKAGE_NAME),
                OpenStreetViewGoogleUriAutomation(TEST_PACKAGE_NAME),
                OpenRouteOnePointGpxAutomation(TEST_PACKAGE_NAME),
                OpenNavigationMagicEarthUriAutomation(TEST_PACKAGE_NAME),
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
