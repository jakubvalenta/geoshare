package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
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

class AllOutputsTest {

    @Test
    fun getOutputsForPoint_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                CopyCoordsDecOutput,
                CopyCoordsDegMinSecOutput,
                CopyGeoUriOutput,
                CopyLinkUriOutput(FakeAppleMapsDisplayLink),
                CopyLinkUriOutput(FakeAppleMapsNavigationLink),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink),
                CopyLinkUriOutput(FakeOpenStreetMapNavigationLink),
                ShareDisplayGeoUriOutput,
                ShareNavigationGoogleUriOutput,
                ShareStreetViewGoogleUriOutput,
                SavePointGpxOutput,
            ),
            getOutputsForPoint(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPoints_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput,
                SharePointsGpxOutput,
                SaveRouteGpxOutput,
                SavePointsGpxOutput,
            ),
            getOutputsForPoints(),
        )
    }

    @Test
    fun getOutputsForApps_returnOutputsThatSupportPassedPackageNamesAndDataTypes() {
        assertEquals(
            mapOf(
                TEST_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME)
                ),
                GOOGLE_MAPS_PACKAGE_NAME to listOf(
                    OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME),
                    OpenNavigationGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME),
                    OpenStreetViewGoogleUriOutput(GOOGLE_MAPS_PACKAGE_NAME),
                    OpenRouteGpxOutput(GOOGLE_MAPS_PACKAGE_NAME),
                    OpenPointsGpxOutput(GOOGLE_MAPS_PACKAGE_NAME),
                ),
                MAGIC_EARTH_PACKAGE_NAME to listOf(
                    OpenDisplayMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME),
                    OpenNavigationMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME),
                ),
                TOMTOM_PACKAGE_NAME to listOf(
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME),
                ),
                "$TEST_PACKAGE_NAME.empty" to emptyList(),
            ),
            getOutputsForApps(
                apps = mapOf(
                    TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                    GOOGLE_MAPS_PACKAGE_NAME to setOf(
                        DataType.GEO_URI,
                        DataType.GOOGLE_NAVIGATION_URI,
                        DataType.GOOGLE_STREET_VIEW_URI,
                        DataType.GPX_DATA,
                    ),
                    MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                    TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                    "$TEST_PACKAGE_NAME.empty" to emptySet(),
                ),
            ),
        )
    }

    @Test
    fun getOutputsForSharing_returnsOutputsThatSupportHardCodedDataTypes() {
        assertEquals(
            listOf(
                ShareDisplayGeoUriOutput,
                ShareNavigationGoogleUriOutput,
                ShareStreetViewGoogleUriOutput,
                ShareRouteGpxOutput,
                SharePointsGpxOutput,
            ),
            getOutputsForSharing(),
        )
    }

    @Test
    fun getOutputsForLinks_returnsOutputsForAllPassedLinks() {
        assertEquals(
            mapOf(
                "Google Maps" to listOf(
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink),
                ),
                "Apple Maps" to listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink),
                ),
                "Magic Earth" to listOf(
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink),
                ),
                "OpenStreetMap" to listOf(
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink),
                ),
            ),
            getOutputsForLinks(defaultFakeLinks),
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
                    ShareLinkUriOutput(linkInFirstGroupWithoutExplicitGroup),
                    ShareLinkUriOutput(linkInFirstGroup),
                    CopyLinkUriOutput(linkInFirstGroupWithoutExplicitGroup),
                    CopyLinkUriOutput(linkInFirstGroup),
                ),
                "second" to listOf(
                    ShareLinkUriOutput(linkInSecondGroup),
                    CopyLinkUriOutput(linkInSecondGroup),
                ),
            ),
            getOutputsForLinks(
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
                CopyGeoUriOutput,
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink),
            ),
            getOutputsForPointChips(defaultFakeLinks),
        )
    }

    @Test
    fun getOutputsForPointsChips_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput,
                SaveRouteGpxOutput,
                SavePointsGpxOutput,
            ),
            getOutputsForPointsChips(),
        )
    }
}
