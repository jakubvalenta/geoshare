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
import page.ooooo.geoshare.data.local.database.findByUUID
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.MAGIC_EARTH_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TEST_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationMagicEarthUriOutput
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
import java.util.UUID

class UserPreferencesTest {

    @Test
    fun getOptionGroups_returnsAllAutomationsWhichCanBeConvertedToOutputs() = runTest {
        assertEquals(
            listOf(
                listOf(
                    NoopOutput,
                ),
                listOf(
                    CopyCoordsDecOutput,
                    CopyCoordsDegMinSecOutput,
                    CopyGeoUriOutput,
                    ShareDisplayGeoUriOutput,
                    ShareNavigationGoogleUriOutput,
                    ShareStreetViewGoogleUriOutput,
                    SavePointGpxOutput,
                ),
                listOf(
                    ShareRouteGpxOutput,
                    SharePointsGpxOutput,
                    SaveRouteGpxOutput,
                    SavePointsGpxOutput,
                ),
                listOf(
                    OpenDisplayMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME),
                    OpenNavigationMagicEarthUriOutput(MAGIC_EARTH_PACKAGE_NAME),
                    OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME),
                    OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME),
                ),
                listOf(
                    ShareLinkUriOutput(FakeAppleMapsDisplayLink),
                    ShareLinkUriOutput(FakeAppleMapsNavigationLink),
                    CopyLinkUriOutput(FakeAppleMapsDisplayLink),
                    CopyLinkUriOutput(FakeAppleMapsNavigationLink),
                    ShareLinkUriOutput(FakeGoogleMapsDisplayLink),
                    ShareLinkUriOutput(FakeGoogleMapsNavigationLink),
                    ShareLinkUriOutput(FakeGoogleMapsStreetViewLink),
                    CopyLinkUriOutput(FakeGoogleMapsDisplayLink),
                    CopyLinkUriOutput(FakeGoogleMapsNavigationLink),
                    CopyLinkUriOutput(FakeGoogleMapsStreetViewLink),
                    ShareLinkUriOutput(FakeMagicEarthDisplayLink),
                    ShareLinkUriOutput(FakeMagicEarthNavigationLink),
                    CopyLinkUriOutput(FakeMagicEarthDisplayLink),
                    CopyLinkUriOutput(FakeMagicEarthNavigationLink),
                    ShareLinkUriOutput(FakeOpenStreetMapDisplayLink),
                    ShareLinkUriOutput(FakeOpenStreetMapNavigationLink),
                    CopyLinkUriOutput(FakeOpenStreetMapDisplayLink),
                    CopyLinkUriOutput(FakeOpenStreetMapNavigationLink),
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
                    links = defaultFakeLinks,
                )
                .map { group ->
                    group.map { automation ->
                        automation.toOutput { defaultFakeLinks.findByUUID(it) }
                    }
                },
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
    fun automationPreference_getValue_forEachOldTypeAndPackageName_returnsAutomationThatCanBeConvertedToAnOutput() =
        runTest {
            @Suppress("SpellCheckingInspection")
            for ((testOldOutputType, expectedAutomation) in mapOf(
                "COPY_APPLE_MAPS_NAVIGATE_TO_URI" to CopyLinkUriOutput(FakeAppleMapsNavigationLink),
                "COPY_APPLE_MAPS_URI" to CopyLinkUriOutput(FakeAppleMapsDisplayLink),
                "COPY_COORDS_DEC" to CopyCoordsDecOutput,
                "COPY_COORDS_NSWE_DEC" to CopyCoordsDegMinSecOutput,
                "COPY_GEO_URI" to CopyGeoUriOutput,
                "COPY_GOOGLE_MAPS_NAVIGATE_TO_URI" to CopyLinkUriOutput(FakeGoogleMapsNavigationLink),
                "COPY_GOOGLE_MAPS_STREET_VIEW_URI" to CopyLinkUriOutput(FakeGoogleMapsStreetViewLink),
                "COPY_GOOGLE_MAPS_URI" to CopyLinkUriOutput(FakeGoogleMapsDisplayLink),
                "COPY_MAGIC_EARTH_NAVIGATE_TO_URI" to CopyLinkUriOutput(FakeMagicEarthNavigationLink),
                "COPY_MAGIC_EARTH_URI" to CopyLinkUriOutput(FakeMagicEarthDisplayLink),
                "NOOP" to NoopOutput,
                "OPEN_APP" to OpenDisplayGeoUriOutput(TEST_PACKAGE_NAME),
                "OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO" to OpenNavigationGoogleUriOutput(TEST_PACKAGE_NAME),
                "OPEN_APP_GOOGLE_MAPS_STREET_VIEW" to OpenStreetViewGoogleUriOutput(TEST_PACKAGE_NAME),
                "OPEN_APP_GPX_ROUTE" to OpenRouteOnePointGpxOutput(TEST_PACKAGE_NAME),
                "OPEN_APP_MAGIC_EARTH_NAVIGATE_TO" to OpenNavigationMagicEarthUriOutput(TEST_PACKAGE_NAME),
                "SAVE_GPX" to SavePointsGpxOutput,
                "SHARE" to ShareDisplayGeoUriOutput,
                "SHARE_GPX_ROUTE" to ShareRouteGpxOutput,
            )) {
                assertEquals(
                    expectedAutomation,
                    AutomationPreference
                        .getValue(
                            preferencesOf(
                                stringPreferencesKey("automation") to testOldOutputType,
                                stringPreferencesKey("automation_package_name") to TEST_PACKAGE_NAME,
                            ),
                            log = FakeLog,
                        )
                        .toOutput {
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
            }
        }
}
