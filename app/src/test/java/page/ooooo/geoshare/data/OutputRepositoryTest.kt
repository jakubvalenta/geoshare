package page.ooooo.geoshare.data

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.FileType
import page.ooooo.geoshare.data.android.MimeType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.TextActivity
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.database.InitialLinks
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.database.findByUUID
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
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.log.FakeLog
import page.ooooo.geoshare.data.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.data.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.data.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.data.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.data.outputs.CopyNameOutput
import page.ooooo.geoshare.data.outputs.CopyStringOutput
import page.ooooo.geoshare.data.outputs.NoopOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayCartesIGNUrlOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.data.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.data.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.data.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.data.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.data.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.data.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.data.outputs.OpenUnknownUriOutput
import page.ooooo.geoshare.data.outputs.SavePointGpxOutput
import page.ooooo.geoshare.data.outputs.SavePointToContactOutput
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.data.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.data.outputs.SendPointOutput
import page.ooooo.geoshare.data.outputs.SendStringOutput
import page.ooooo.geoshare.data.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.data.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.data.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.data.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.data.outputs.ShareStreetViewGoogleUriOutput
import page.ooooo.geoshare.data.preferences.ActivityAutomation
import page.ooooo.geoshare.data.preferences.BasicAutomation
import page.ooooo.geoshare.data.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.preferences.CopyCoordsDegMinSecAutomation
import page.ooooo.geoshare.data.preferences.CopyGeoUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkUriAutomation
import page.ooooo.geoshare.data.preferences.CopyNameAutomation
import page.ooooo.geoshare.data.preferences.LinkAutomation
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayCartesIGNUrlAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.OpenNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.OpenNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.preferences.OpenStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.SavePointGpxAutomation
import page.ooooo.geoshare.data.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.preferences.SendPointAutomation
import page.ooooo.geoshare.data.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.uri.FakeUriQuote
import java.util.UUID

class OutputRepositoryTest {
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val log = FakeLog
    private val uriQuote = FakeUriQuote
    private val outputRepository = OutputRepository(
        coordinateConverter = coordinateConverter,
        log = log,
        uriQuote = uriQuote,
    )

    @Test
    fun getPointCopyOutputs_returnsOutputsThatDoNotRequirePackageName() {
        assertEquals(
            listOf(
                CopyCoordsDecOutput(coordinateConverter, uriQuote),
                CopyCoordsDegMinSecOutput(coordinateConverter, uriQuote),
                CopyNameOutput(uriQuote),
                CopyGeoUriOutput(coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter, uriQuote),
            ),
            outputRepository.getPointCopyOutputs(defaultFakeLinks),
        )
    }

    @Test
    fun getPointOpenOutputs_returnsFirstOutputForEachPassedActivity() {
        assertEquals(
            listOf(
                AppOutputs(
                    packageName = PackageNames.TEST,
                    defaultOutput = OpenDisplayGeoUriOutput(
                        UriActivity(PackageNames.TEST, UriScheme.GEO), coordinateConverter, uriQuote
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.GOOGLE_MAPS,
                    defaultOutput = OpenDisplayGeoUriOutput(
                        UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter, uriQuote
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.CARTES_IGN,
                    defaultOutput = OpenDisplayCartesIGNUrlOutput(
                        UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN), coordinateConverter, uriQuote
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.MAGIC_EARTH,
                    defaultOutput = OpenDisplayMagicEarthUriOutput(
                        UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH), coordinateConverter, uriQuote
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.SIGNAL,
                    defaultOutput = SendPointOutput(
                        TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN), coordinateConverter, uriQuote
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.TOMTOM,
                    defaultOutput = OpenRouteOnePointGpxOutput(
                        FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT), coordinateConverter, log
                    ),
                    allOutputs = emptyList(),
                ),
            ),
            outputRepository.getPointOpenOutputs(
                listOf(
                    // TODO Test filtering first activity that is for a single point
                    UriActivity(PackageNames.TEST, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_STREET_VIEW),
                    FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX),
                    UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN),
                    UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                    TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN),
                    FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                ),
                emptySet(), // TODO Test hidden apps
            ),
        )
    }

    @Test
    fun pointSaveOutputs_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                SavePointGpxOutput(coordinateConverter),
                SavePointToContactOutput(coordinateConverter),
            ),
            outputRepository.pointSaveOutputs,
        )
    }

    @Test
    fun pointsSaveOutputs_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                SaveRouteGpxOutput(coordinateConverter),
                SavePointsGpxOutput(coordinateConverter),
            ),
            outputRepository.pointsSaveOutputs,
        )
    }

    @Test
    fun pointsShareOutputs_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(coordinateConverter),
                SharePointsGpxOutput(coordinateConverter),
            ),
            outputRepository.pointsShareOutputs,
        )
    }

    @Test
    fun getAppsOutputs_returnOutputsThatSupportPassedActivities() {
        assertEquals(
            AppsOutputs(
                mapAppsOutputs = listOf(
                    AppOutputs(
                        packageName = PackageNames.TEST,
                        defaultOutput = OpenDisplayGeoUriOutput(
                            UriActivity(PackageNames.TEST, UriScheme.GEO), coordinateConverter, uriQuote
                        ),
                        allOutputs = listOf(
                            OpenDisplayGeoUriOutput(
                                UriActivity(PackageNames.TEST, UriScheme.GEO), coordinateConverter, uriQuote
                            ),
                        )
                    ),
                    AppOutputs(
                        packageName = PackageNames.GOOGLE_MAPS,
                        defaultOutput = OpenDisplayGeoUriOutput(
                            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter, uriQuote
                        ),
                        allOutputs = listOf(
                            OpenDisplayGeoUriOutput(
                                UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter, uriQuote
                            ),
                            OpenNavigationGoogleUriOutput(
                                UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
                                coordinateConverter,
                                uriQuote,
                            ),
                            OpenStreetViewGoogleUriOutput(
                                UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_STREET_VIEW),
                                coordinateConverter,
                                uriQuote,
                            ),
                            OpenRouteGpxOutput(
                                FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX), coordinateConverter, log
                            ),
                            OpenPointsGpxOutput(
                                FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX), coordinateConverter, log
                            ),
                        )
                    ),
                    AppOutputs(
                        packageName = PackageNames.CARTES_IGN,
                        defaultOutput = OpenDisplayCartesIGNUrlOutput(
                            UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN), coordinateConverter, uriQuote
                        ),
                        allOutputs = listOf(
                            OpenDisplayCartesIGNUrlOutput(
                                UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN),
                                coordinateConverter,
                                uriQuote,
                            ),
                        )
                    ),
                    AppOutputs(
                        packageName = PackageNames.MAGIC_EARTH,
                        defaultOutput = OpenDisplayMagicEarthUriOutput(
                            UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                            coordinateConverter,
                            uriQuote,
                        ),
                        allOutputs = listOf(
                            OpenDisplayMagicEarthUriOutput(
                                UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                                coordinateConverter,
                                uriQuote,
                            ),
                            OpenNavigationMagicEarthUriOutput(
                                UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                                coordinateConverter,
                                uriQuote,
                            ),
                        )
                    ),
                    AppOutputs(
                        packageName = PackageNames.TOMTOM,
                        defaultOutput = OpenRouteOnePointGpxOutput(
                            FileActivity(PackageNames.TOMTOM, fileType = FileType.GPX_ONE_POINT),
                            coordinateConverter,
                            log,
                        ),
                        allOutputs = listOf(
                            OpenRouteOnePointGpxOutput(
                                FileActivity(PackageNames.TOMTOM, fileType = FileType.GPX_ONE_POINT),
                                coordinateConverter,
                                log,
                            ),
                        )
                    ),
                ),
                messagingAppsOutputs = listOf(
                    AppOutputs(
                        packageName = PackageNames.SIGNAL,
                        defaultOutput = SendPointOutput(
                            TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN), coordinateConverter, uriQuote
                        ),
                        allOutputs = listOf(
                            SendPointOutput(
                                TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN), coordinateConverter, uriQuote
                            ),
                        )
                    ),
                ),
                exampleAppOutputs = AppOutputs(
                    packageName = PackageNames.GOOGLE_MAPS,
                    defaultOutput = OpenDisplayGeoUriOutput(
                        UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter, uriQuote
                    ),
                    allOutputs = listOf(
                        OpenDisplayGeoUriOutput(
                            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter, uriQuote
                        ),
                        OpenNavigationGoogleUriOutput(
                            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
                            coordinateConverter,
                            uriQuote,
                        ),
                        OpenStreetViewGoogleUriOutput(
                            UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_STREET_VIEW),
                            coordinateConverter,
                            uriQuote,
                        ),
                        OpenRouteGpxOutput(
                            FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX), coordinateConverter, log
                        ),
                        OpenPointsGpxOutput(
                            FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX), coordinateConverter, log
                        ),
                    )
                ),
            ),
            outputRepository.getAppsOutputs(
                listOf(
                    UriActivity(PackageNames.TEST, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_STREET_VIEW),
                    FileActivity(PackageNames.GOOGLE_MAPS, FileType.GPX),
                    UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN),
                    UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                    TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN),
                    FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                ),
                emptySet(),
            ),
        )
    }

    @Test
    fun getAppsOutputs_doesNotReturnOutputsForHiddenApps() {
        assertEquals(
            AppsOutputs(
                mapAppsOutputs = listOf(
                    AppOutputs(
                        packageName = PackageNames.TEST,
                        defaultOutput = OpenDisplayGeoUriOutput(
                            UriActivity(PackageNames.TEST, UriScheme.GEO),
                            coordinateConverter,
                            uriQuote,
                        ),
                        allOutputs = listOf(
                            OpenDisplayGeoUriOutput(
                                UriActivity(PackageNames.TEST, UriScheme.GEO),
                                coordinateConverter,
                                uriQuote,
                            ),
                        )
                    ),
                    AppOutputs(
                        packageName = PackageNames.TOMTOM,
                        defaultOutput = OpenRouteOnePointGpxOutput(
                            FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                            coordinateConverter,
                            log,
                        ),
                        allOutputs = listOf(
                            OpenRouteOnePointGpxOutput(
                                FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                                coordinateConverter,
                                log,
                            ),
                        )
                    ),
                ),
            ),
            outputRepository.getAppsOutputs(
                listOf(
                    UriActivity(PackageNames.TEST, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
                    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
                    FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                ),
                setOf(
                    PackageNames.GOOGLE_MAPS,
                    PackageNames.MAGIC_EARTH,
                ),
            ),
        )
    }

    @Test
    fun getLinksOutputs_returnsOutputsForLinksThatHaveAppEnabled() {
        assertEquals(
            listOf(
                LinkOutputs(
                    group = "Apple Maps",
                    defaultOutput = ShareLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                    allOutputs = listOf(
                        ShareLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                    ),
                ),
                LinkOutputs(
                    group = "Google Maps",
                    defaultOutput = ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                    allOutputs = listOf(
                        ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter, uriQuote),
                    ),
                ),
                LinkOutputs(
                    group = "Magic Earth",
                    defaultOutput = ShareLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                    allOutputs = listOf(
                        ShareLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter, uriQuote),
                    ),
                ),
                LinkOutputs(
                    group = "OpenStreetMap",
                    defaultOutput = ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                    allOutputs = listOf(
                        ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter, uriQuote),
                    ),
                ),
            ),
            outputRepository.getLinksOutputs(defaultFakeLinks),
        )
    }

    @Test
    fun getLinksOutputs_returnsOutputsGroupedByLinkGroupOrName() {
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
            listOf(
                LinkOutputs(
                    group = "first",
                    defaultOutput = ShareLinkUriOutput(
                        linkInFirstGroupWithoutExplicitGroup, coordinateConverter, uriQuote
                    ),
                    allOutputs = listOf(
                        ShareLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, coordinateConverter, uriQuote),
                        ShareLinkUriOutput(linkInFirstGroup, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(linkInFirstGroupWithoutExplicitGroup, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(linkInFirstGroup, coordinateConverter, uriQuote),
                    ),
                ),
                LinkOutputs(
                    group = "second",
                    defaultOutput = ShareLinkUriOutput(linkInSecondGroup, coordinateConverter, uriQuote),
                    allOutputs = listOf(
                        ShareLinkUriOutput(linkInSecondGroup, coordinateConverter, uriQuote),
                        CopyLinkUriOutput(linkInSecondGroup, coordinateConverter, uriQuote),
                    ),
                )
            ),
            outputRepository.getLinksOutputs(
                listOf(
                    linkInFirstGroupWithoutExplicitGroup,
                    linkInFirstGroup,
                    linkInSecondGroup,
                )
            ),
        )
    }

    @Test
    fun getPointChipsOutputs_returnsOutputsForLinksThatHaveChipEnabled() {
        assertEquals(
            listOf(
                CopyGeoUriOutput(coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
            ),
            outputRepository.getPointChipsOutputs(defaultFakeLinks),
        )
    }

    @Test
    fun pointsChipsOutputs_returnsHardCodedOutputs() {
        assertEquals(
            listOf(
                ShareRouteGpxOutput(coordinateConverter),
                SaveRouteGpxOutput(coordinateConverter),
                SavePointsGpxOutput(coordinateConverter),
            ),
            outputRepository.pointsChipsOutputs,
        )
    }

    @Test
    fun sourceCopyOutput_returnsHardCodedOutput() {
        assertEquals(
            CopyStringOutput,
            outputRepository.sourceCopyOutput,
        )
    }

    @Test
    fun getSourceAppsOutputs_returnsOutputsForTextActivityAndUriActivityAndIgnoresFileActivity() {
        assertEquals(
            listOf(
                AppOutputs(
                    packageName = PackageNames.OSMAND_PLUS,
                    defaultOutput = OpenUnknownUriOutput(
                        UriActivity(packageName = PackageNames.OSMAND_PLUS, uriScheme = UriScheme.GEO)
                    ),
                    allOutputs = emptyList(),
                ),
                AppOutputs(
                    packageName = PackageNames.CONVERSATIONS,
                    defaultOutput = SendStringOutput(
                        TextActivity(packageName = PackageNames.CONVERSATIONS, mimeType = MimeType.TEXT_PLAIN),
                    ),
                    allOutputs = emptyList(),
                ),
            ),
            outputRepository.getSourceAppsOutputs(
                listOf(
                    UriActivity(packageName = PackageNames.OSMAND_PLUS, uriScheme = UriScheme.GEO),
                    TextActivity(packageName = PackageNames.CONVERSATIONS, mimeType = MimeType.TEXT_PLAIN),
                    FileActivity(packageName = PackageNames.COMAPS_FDROID, fileType = FileType.GPX),
                )
            )
        )
    }

    @Test
    fun toOutput_convertsAllAutomationsToOutputs() = runTest {
        assertEquals(
            listOf(
                listOf(
                    NoopOutput,
                ),
                listOf(
                    CopyCoordsDecOutput(coordinateConverter, uriQuote),
                    CopyCoordsDegMinSecOutput(coordinateConverter, uriQuote),
                    CopyNameOutput(uriQuote),
                    CopyGeoUriOutput(coordinateConverter, uriQuote),
                    ShareDisplayGeoUriOutput(coordinateConverter, uriQuote),
                    ShareNavigationGoogleUriOutput(coordinateConverter, uriQuote),
                    ShareStreetViewGoogleUriOutput(coordinateConverter, uriQuote),
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
                    OpenDisplayCartesIGNUrlOutput(
                        UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN),
                        coordinateConverter,
                        uriQuote,
                    ),
                    OpenDisplayMagicEarthUriOutput(
                        UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                        coordinateConverter,
                        uriQuote,
                    ),
                    OpenNavigationMagicEarthUriOutput(
                        UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
                        coordinateConverter,
                        uriQuote,
                    ),
                    SendPointOutput(
                        TextActivity(PackageNames.SIGNAL, MimeType.TEXT_PLAIN),
                        coordinateConverter,
                        uriQuote,
                    ),
                    OpenRouteOnePointGpxOutput(
                        FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
                        coordinateConverter,
                        log,
                    ),
                    OpenDisplayGeoUriOutput(
                        UriActivity(PackageNames.TEST, UriScheme.GEO),
                        coordinateConverter,
                        uriQuote
                    ),
                ),
                listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink, coordinateConverter, uriQuote),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink, coordinateConverter, uriQuote),
                ),
            ),
            listOf(
                listOf(
                    NoopAutomation,
                ),
                listOf(
                    CopyCoordsDecAutomation,
                    CopyCoordsDegMinSecAutomation,
                    CopyNameAutomation,
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
                    OpenDisplayCartesIGNUrlAutomation(PackageNames.CARTES_IGN),
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
                    when (automation) {
                        is BasicAutomation -> automation.toOutput(coordinateConverter, uriQuote)
                        is ActivityAutomation -> automation.toOutput(coordinateConverter, log)
                        is LinkAutomation -> defaultFakeLinks.findByUUID(automation.linkUUID)?.let { link ->
                            automation.toOutput(coordinateConverter, link)
                        }
                    }
                }
            },
        )
    }

    @Test
    fun toOutput_convertsAllOldAutomationsToOutputs() = runTest {
        assertEquals(
            listOf(
                CopyLinkUriOutput(FakeAppleMapsNavigationLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeAppleMapsDisplayLink, coordinateConverter, uriQuote),
                CopyCoordsDecOutput(coordinateConverter, uriQuote),
                CopyCoordsDegMinSecOutput(coordinateConverter, uriQuote),
                CopyGeoUriOutput(coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsNavigationLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsStreetViewLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeGoogleMapsDisplayLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeMagicEarthNavigationLink, coordinateConverter, uriQuote),
                CopyLinkUriOutput(FakeMagicEarthDisplayLink, coordinateConverter, uriQuote),
                NoopOutput,
                OpenDisplayGeoUriOutput(UriActivity(PackageNames.TEST, UriScheme.GEO), coordinateConverter, uriQuote),
                OpenNavigationGoogleUriOutput(
                    UriActivity(PackageNames.TEST, UriScheme.GOOGLE_NAVIGATION),
                    coordinateConverter,
                    uriQuote,
                ),
                OpenStreetViewGoogleUriOutput(
                    UriActivity(PackageNames.TEST, UriScheme.GOOGLE_STREET_VIEW),
                    coordinateConverter,
                    uriQuote,
                ),
                OpenRouteOnePointGpxOutput(
                    FileActivity(PackageNames.TEST, FileType.GPX_ONE_POINT),
                    coordinateConverter,
                    log,
                ),
                OpenNavigationMagicEarthUriOutput(
                    UriActivity(PackageNames.TEST, UriScheme.MAGIC_EARTH),
                    coordinateConverter,
                    uriQuote,
                ),
                SavePointsGpxOutput(coordinateConverter),
                ShareDisplayGeoUriOutput(coordinateConverter, uriQuote),
                ShareRouteGpxOutput(coordinateConverter),
            ),
            listOf(
                CopyLinkUriAutomation(UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID)),
                CopyLinkUriAutomation(UUID.fromString(InitialLinks.APPLE_MAPS_DISPLAY_UUID)),
                CopyCoordsDecAutomation,
                CopyCoordsDegMinSecAutomation,
                CopyGeoUriAutomation,
                CopyLinkUriAutomation(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a")),
                CopyLinkUriAutomation(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a")),
                CopyLinkUriAutomation(UUID.fromString(InitialLinks.GOOGLE_MAPS_DISPLAY_UUID)),
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
                when (automation) {
                    is BasicAutomation -> automation.toOutput(coordinateConverter, uriQuote)
                    is ActivityAutomation -> automation.toOutput(coordinateConverter, log)
                    is LinkAutomation ->
                        when (automation.linkUUID) {
                            UUID.fromString(InitialLinks.APPLE_MAPS_DISPLAY_UUID) -> FakeAppleMapsDisplayLink
                            UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID) -> FakeAppleMapsNavigationLink
                            UUID.fromString(InitialLinks.GOOGLE_MAPS_DISPLAY_UUID) -> FakeGoogleMapsDisplayLink
                            UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a") -> FakeGoogleMapsNavigationLink
                            UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a") -> FakeGoogleMapsStreetViewLink
                            UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07") -> FakeMagicEarthDisplayLink
                            UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7") -> FakeMagicEarthNavigationLink
                            else -> null
                        }?.let { link ->
                            automation.toOutput(coordinateConverter, link)
                        }
                }
            },
        )
    }
}
