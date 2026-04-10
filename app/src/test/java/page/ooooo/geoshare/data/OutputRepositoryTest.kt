package page.ooooo.geoshare.data

import org.junit.Assert
import org.junit.Test
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
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.MAGIC_EARTH_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TEST_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
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

class OutputRepositoryTest {
    @Test
    fun getOutputsForPoint_returnsOutputsThatDoNotRequirePackageName() {
        Assert.assertEquals(
            listOf(
                CopyCoordsDecOutput(coordsFormat),
                CopyCoordsDegMinSecOutput(coordsFormat),
                CopyGeoUriOutput(geoUriFormat),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriStringFormat),
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriStringFormat),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriStringFormat),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriStringFormat),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriStringFormat),
                CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, uriStringFormat),
                ShareDisplayGeoUriOutput(geoUriFormat),
                ShareNavigationGoogleUriOutput(googleMapsUriFormat),
                ShareStreetViewGoogleUriOutput(googleMapsUriFormat),
                SavePointGpxOutput(gpxFormat),
            ),
            outputRepository.getOutputsForPoint(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPoints_returnsOutputsThatDoNotRequirePackageName() {
        Assert.assertEquals(
            listOf(
                ShareRouteGpxOutput(gpxFormat),
                SharePointsGpxOutput(gpxFormat),
                SaveRouteGpxOutput(gpxFormat),
                SavePointsGpxOutput(gpxFormat),
            ),
            outputRepository.getOutputsForPoints(),
        )
    }

    @Test
    fun getOutputsForApps_returnOutputsThatSupportPassedPackageNamesAndDataTypes() {
        Assert.assertEquals(
            mapOf(
                TEST_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormat)
                ),
                GOOGLE_MAPS_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormat),
                    OpenNavigationGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME, googleMapsUriFormat),
                    OpenStreetViewGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME, googleMapsUriFormat),
                    OpenRouteGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormat),
                    OpenPointsGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormat),
                ),
                MAGIC_EARTH_PACKAGE_NAME to listOf(
                    OpenDisplayMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormat),
                    OpenNavigationMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME, magicEarthUriFormat),
                ),
                TOMTOM_PACKAGE_NAME to listOf(
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormat),
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
        Assert.assertEquals(
            mapOf(
                TEST_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME, geoUriFormat)
                ),
                TOMTOM_PACKAGE_NAME to listOf(
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormat),
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
        Assert.assertEquals(
            listOf(
                ShareDisplayGeoUriOutput(geoUriFormat),
                ShareNavigationGoogleUriOutput(googleMapsUriFormat),
                ShareStreetViewGoogleUriOutput(googleMapsUriFormat),
                ShareRouteGpxOutput(gpxFormat),
                SharePointsGpxOutput(gpxFormat),
            ),
            outputRepository.getOutputsForSharing(),
        )
    }

    @Test
    fun getOutputsForLinks_returnsOutputsForAllPassedLinks() {
        Assert.assertEquals(
            mapOf(
                "Google Maps" to listOf(
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, uriStringFormat),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, uriStringFormat),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, uriStringFormat),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriStringFormat),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, uriStringFormat),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, uriStringFormat),
                ),
                "Apple Maps" to listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, uriStringFormat),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, uriStringFormat),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, uriStringFormat),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, uriStringFormat),
                ),
                "Magic Earth" to listOf(
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, uriStringFormat),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, uriStringFormat),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, uriStringFormat),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, uriStringFormat),
                ),
                "OpenStreetMap" to listOf(
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, uriStringFormat),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, uriStringFormat),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriStringFormat),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, uriStringFormat),
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
        Assert.assertEquals(
            mapOf(
                "first" to listOf(
                    ShareLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, uriStringFormat),
                    ShareLinkUriOutput(linkInFirstGroup, uriStringFormat),
                    CopyLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, uriStringFormat),
                    CopyLinkUriOutput(linkInFirstGroup, uriStringFormat),
                ),
                "second" to listOf(
                    ShareLinkUriOutput(linkInSecondGroup, uriStringFormat),
                    CopyLinkUriOutput(linkInSecondGroup, uriStringFormat),
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
        Assert.assertEquals(
            listOf(
                CopyGeoUriOutput(geoUriFormat),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, uriStringFormat),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, uriStringFormat),
            ),
            outputRepository.getOutputsForPointChips(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPointsChips_returnsHardCodedOutputs() {
        Assert.assertEquals(
            listOf(
                ShareRouteGpxOutput(gpxFormat),
                SaveRouteGpxOutput(gpxFormat),
                SavePointsGpxOutput(gpxFormat),
            ),
            outputRepository.getOutputsForPointsChips(),
        )
    }
}
