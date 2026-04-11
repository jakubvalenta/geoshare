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
import page.ooooo.geoshare.lib.android.MAGIC_EARTH_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TEST_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import java.util.UUID

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
            ),
            AutomationPreference
                .getOptionGroups(
                    apps = mapOf(
                        MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                        TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                        TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                    ),
                    appDetails = mapOf(
                        MAGIC_EARTH_PACKAGE_NAME to AppDetail("Magic Earth", mock()),
                        TEST_PACKAGE_NAME to AppDetail("ZZZ sort last", mock()),
                        TOMTOM_PACKAGE_NAME to AppDetail("TomTom", mock()),
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
                    OpenRouteOnePointGpxAutomation(TOMTOM_PACKAGE_NAME),
                    OpenDisplayGeoUriAutomation(TEST_PACKAGE_NAME),
                ),
            ),
            AutomationPreference
                .getOptionGroups(
                    apps = mapOf(
                        MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                        TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                        TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
                    ),
                    appDetails = mapOf(
                        MAGIC_EARTH_PACKAGE_NAME to AppDetail("Magic Earth", mock()),
                        TEST_PACKAGE_NAME to AppDetail("ZZZ sort last", mock()),
                        TOMTOM_PACKAGE_NAME to AppDetail("TomTom", mock()),
                    ),
                    hiddenApps = setOf(MAGIC_EARTH_PACKAGE_NAME),
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
                    stringPreferencesKey("automation_package_name") to TEST_PACKAGE_NAME,
                ),
                log = FakeLog,
            )
        )
    }

    @Test
    fun automationPreference_getValue_forEachSerializedString_returnsAutomation() {
        AutomationPreference.getOptionGroups(
            apps = mapOf(
                TEST_PACKAGE_NAME to setOf(DataType.GEO_URI),
                MAGIC_EARTH_PACKAGE_NAME to setOf(DataType.MAGIC_EARTH_URI),
                TOMTOM_PACKAGE_NAME to setOf(DataType.GPX_ONE_POINT_DATA),
            ),
            appDetails = mapOf(
                MAGIC_EARTH_PACKAGE_NAME to AppDetail("Magic Earth", mock()),
                TEST_PACKAGE_NAME to AppDetail("ZZZ sort last", mock()),
                TOMTOM_PACKAGE_NAME to AppDetail("TomTom", mock()),
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
            stringPreferencesKey("automation") to """{"type":"OPEN_APP","packageName":"$TEST_PACKAGE_NAME","spam":"spam"}""",
        )
        assertEquals(
            OpenDisplayGeoUriAutomation(TEST_PACKAGE_NAME),
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
                    stringPreferencesKey("automation_package_name") to TEST_PACKAGE_NAME,
                ),
                log = FakeLog,
            )
        )
    }

    @Test
    fun automationPreference_getValue_forEachOldTypeAndPackageName_returnsAutomation() =
        runTest {
            @Suppress("SpellCheckingInspection")
            for ((testOldAutomationType, expectedAutomation) in mapOf(
                "COPY_APPLE_MAPS_NAVIGATE_TO_URI" to CopyLinkUriAutomation(UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215")),
                "COPY_APPLE_MAPS_URI" to CopyLinkUriAutomation(UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603")),
                "COPY_COORDS_DEC" to CopyCoordsDecAutomation,
                "COPY_COORDS_NSWE_DEC" to CopyCoordsDegMinSecAutomation,
                "COPY_GEO_URI" to CopyGeoUriAutomation,
                "COPY_GOOGLE_MAPS_NAVIGATE_TO_URI" to CopyLinkUriAutomation(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a")),
                "COPY_GOOGLE_MAPS_STREET_VIEW_URI" to CopyLinkUriAutomation(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a")),
                "COPY_GOOGLE_MAPS_URI" to CopyLinkUriAutomation(UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0")),
                "COPY_MAGIC_EARTH_NAVIGATE_TO_URI" to CopyLinkUriAutomation(UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7")),
                "COPY_MAGIC_EARTH_URI" to CopyLinkUriAutomation(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07")),
                "NOOP" to NoopAutomation,
                "OPEN_APP" to OpenDisplayGeoUriAutomation(TEST_PACKAGE_NAME),
                "OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO" to OpenNavigationGoogleUriAutomation(TEST_PACKAGE_NAME),
                "OPEN_APP_GOOGLE_MAPS_STREET_VIEW" to OpenStreetViewGoogleUriAutomation(TEST_PACKAGE_NAME),
                "OPEN_APP_GPX_ROUTE" to OpenRouteOnePointGpxAutomation(TEST_PACKAGE_NAME),
                "OPEN_APP_MAGIC_EARTH_NAVIGATE_TO" to OpenNavigationMagicEarthUriAutomation(TEST_PACKAGE_NAME),
                "SAVE_GPX" to SavePointsGpxAutomation,
                "SHARE" to ShareDisplayGeoUriAutomation,
                "SHARE_GPX_ROUTE" to ShareRouteGpxAutomation,
            )) {
                assertEquals(
                    expectedAutomation,
                    AutomationPreference.getValue(
                        preferencesOf(
                            stringPreferencesKey("automation") to testOldAutomationType,
                            stringPreferencesKey("automation_package_name") to TEST_PACKAGE_NAME,
                        ),
                        log = FakeLog,
                    ),
                )
            }
        }
}
