package page.ooooo.geoshare.data.local.preferences

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
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
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames

class UserPreferencesTest {
    @Test
    fun getOptionGroups_returnsAllAutomations() = runTest {
        assertEquals(
            listOf(
                listOf(
                    NoopAutomation,
                ),
                listOf(
                    CopyCoordsDecAutomation,
                    CopyCoordsDegMinSecAutomation,
                    CopyGeoUriAutomation,
                    CopyPlusCodeUriAutomation,
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
                    OpenDisplayMagicEarthUriAutomation(PackageNames.MAGIC_EARTH),
                    OpenNavigationMagicEarthUriAutomation(PackageNames.MAGIC_EARTH),
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
            ),
            AutomationPreference
                .getOptionGroups(
                    apps = mapOf(
                        PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                        PackageNames.TEST to setOf(DataType.GEO_URI),
                        PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                    ),
                    appDetails = mapOf(
                        PackageNames.MAGIC_EARTH to AppDetail("Magic Earth", mock()),
                        PackageNames.TEST to AppDetail("ZZZ sort last", mock()),
                        PackageNames.TOMTOM to AppDetail("TomTom", mock()),
                    ),
                    hiddenApps = emptySet(),
                    links = defaultFakeLinks,
                ),
        )
    }

    @Test
    fun getOptionGroups_doesNotReturnAutomationsForHiddenApps() = runTest {
        assertEquals(
            listOf(
                listOf(
                    NoopAutomation,
                ),
                listOf(
                    CopyCoordsDecAutomation,
                    CopyCoordsDegMinSecAutomation,
                    CopyGeoUriAutomation,
                    CopyPlusCodeUriAutomation,
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
                    OpenRouteOnePointGpxAutomation(PackageNames.TOMTOM),
                    OpenDisplayGeoUriAutomation(PackageNames.TEST),
                ),
            ),
            AutomationPreference
                .getOptionGroups(
                    apps = mapOf(
                        PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                        PackageNames.TEST to setOf(DataType.GEO_URI),
                        PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
                    ),
                    appDetails = mapOf(
                        PackageNames.MAGIC_EARTH to AppDetail("Magic Earth", mock()),
                        PackageNames.TEST to AppDetail("ZZZ sort last", mock()),
                        PackageNames.TOMTOM to AppDetail("TomTom", mock()),
                    ),
                    hiddenApps = setOf(PackageNames.MAGIC_EARTH),
                    links = emptyList(),
                ),
        )
    }

    @Test
    fun automationPreference_getValue_serializedStringIsInvalid_returnsNoop() {
        assertEquals(
            NoopAutomation,
            AutomationPreference.getValue(
                preferencesOf(
                    stringPreferencesKey("automation") to "spam",
                    stringPreferencesKey("automation_package_name") to PackageNames.TEST,
                ),
                log = FakeLog,
            )
        )
    }

    @Test
    fun automationPreference_getValue_forEachSerializedString_returnsAutomation() {
        AutomationPreference.getOptionGroups(
            apps = mapOf(
                PackageNames.TEST to setOf(DataType.GEO_URI),
                PackageNames.MAGIC_EARTH to setOf(DataType.MAGIC_EARTH_URI),
                PackageNames.TOMTOM to setOf(DataType.GPX_ONE_POINT_DATA),
            ),
            appDetails = mapOf(
                PackageNames.MAGIC_EARTH to AppDetail("Magic Earth", mock()),
                PackageNames.TEST to AppDetail("ZZZ sort last", mock()),
                PackageNames.TOMTOM to AppDetail("TomTom", mock()),
            ),
            hiddenApps = emptySet(),
            links = defaultFakeLinks,
        ).flatten().forEach { expectedAutomation ->
            val preferences = mutablePreferencesOf()
            AutomationPreference.setValue(preferences, expectedAutomation, log = FakeLog)
            assertEquals(
                expectedAutomation,
                AutomationPreference.getValue(preferences, log = FakeLog),
            )
        }
    }

    @Test
    fun automationPreference_getValue_serializedStringContainsUnknownProperties_returnsAutomation() {
        val preferences = mutablePreferencesOf(
            stringPreferencesKey("automation") to """{"type":"OPEN_APP","packageName":"${PackageNames.TEST}","spam":"spam"}""",
        )
        assertEquals(
            OpenDisplayGeoUriAutomation(PackageNames.TEST),
            AutomationPreference.getValue(preferences, log = FakeLog),
        )
    }

    @Test
    fun automationPreference_getValue_oldTypeIsInvalid_returnsNoop() {
        assertEquals(
            NoopAutomation,
            AutomationPreference.getValue(
                preferencesOf(
                    stringPreferencesKey("automation") to "spam",
                    stringPreferencesKey("automation_package_name") to PackageNames.TEST,
                ),
                log = FakeLog,
            )
        )
    }

    @Test
    fun automationPreference_getValue_forEachOldTypeAndPackageName_returnsAutomation() =
        runTest {
            @Suppress("DEPRECATION", "SpellCheckingInspection")
            for ((testOldAutomationType, expectedAutomation) in mapOf(
                "COPY_APPLE_MAPS_NAVIGATE_TO_URI" to CopyLinkNavigationAppleMapsUriAutomation,
                "COPY_APPLE_MAPS_URI" to CopyLinkDisplayAppleMapsUriAutomation,
                "COPY_COORDS_DEC" to CopyCoordsDecAutomation,
                "COPY_COORDS_NSWE_DEC" to CopyCoordsDegMinSecAutomation,
                "COPY_GEO_URI" to CopyGeoUriAutomation,
                "COPY_GOOGLE_MAPS_NAVIGATE_TO_URI" to CopyLinkNavigationGoogleUriAutomation,
                "COPY_GOOGLE_MAPS_STREET_VIEW_URI" to CopyLinkStreetViewGoogleUriAutomation,
                "COPY_GOOGLE_MAPS_URI" to CopyLinkDisplayGoogleMapsUriAutomation,
                "COPY_MAGIC_EARTH_NAVIGATE_TO_URI" to CopyLinkNavigationMagicEarthUriAutomation,
                "COPY_MAGIC_EARTH_URI" to CopyLinkDisplayMagicEarthUriAutomation,
                "NOOP" to NoopAutomation,
                "OPEN_APP" to OpenDisplayGeoUriAutomation(PackageNames.TEST),
                "OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO" to OpenNavigationGoogleUriAutomation(PackageNames.TEST),
                "OPEN_APP_GOOGLE_MAPS_STREET_VIEW" to OpenStreetViewGoogleUriAutomation(PackageNames.TEST),
                "OPEN_APP_GPX_ROUTE" to OpenRouteOnePointGpxAutomation(PackageNames.TEST),
                "OPEN_APP_MAGIC_EARTH_NAVIGATE_TO" to OpenNavigationMagicEarthUriAutomation(PackageNames.TEST),
                "SAVE_GPX" to SavePointsGpxAutomation,
                "SHARE" to ShareDisplayGeoUriAutomation,
                "SHARE_GPX_ROUTE" to ShareRouteGpxAutomation,
            )) {
                assertEquals(
                    expectedAutomation,
                    AutomationPreference.getValue(
                        preferencesOf(
                            stringPreferencesKey("automation") to testOldAutomationType,
                            stringPreferencesKey("automation_package_name") to PackageNames.TEST,
                        ),
                        log = FakeLog,
                    ),
                )
            }
        }
}
