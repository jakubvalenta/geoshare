package page.ooooo.geoshare.tests

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class UserPreferencesBehaviorTest {
    @Test
    fun whenCoordinateFormatIsSet_showsCoordinatesInThatFormat() = uiAutomator {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)

        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Share a URI with the app
        shareUri("geo:52.5067296,13.2599309")

        // Shows coordinates in the decimal degrees format
        onElement {
            if (viewIdResourceName == "geoShareResultLastPointCoordinates") {
                assertEquals(
                    CoordinateFormatter.formatDecCoords(
                        coordinateConverter.toWGS84(
                            GCJ02Point(52.5067296, 13.2599309, source = Source.MAP_CENTER)
                        )
                    ),
                    textAsString(),
                )
                true
            } else {
                false
            }
        }

        // Set coordinate format to degrees, minutes, seconds
        goToUserPreferencesDetail(UserPreferenceGroupId.COORDINATE_FORMAT)
        onElement { viewIdResourceName == "geoShareUserPreferenceCoordinateFormat_${CoordinateFormat.DEG_MIN_SEC}" }.click()

        // Shows coordinates in the degrees, minutes, seconds format
        val coordinates = goBackToElement { viewIdResourceName == "geoShareResultLastPointCoordinates" }
        assertEquals(
            CoordinateFormatter.formatDegMinSecCoords(
                coordinateConverter.toWGS84(
                    GCJ02Point(52.5067296, 13.2599309, source = Source.MAP_CENTER)
                )
            ),
            coordinates.text,
        )
    }

    @Test
    fun whenAppIsHidden_itIsNotShownOnResultScreen() = uiAutomator {
        assumeAppInstalled(PackageNames.OSMAND_PLUS)

        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Share a URI with the app
        shareUri()

        // Hide an app
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareApp_${PackageNames.OSMAND_PLUS}" }
            .longClick()
        onElement { viewIdResourceName == "geoShareAppHide" }.click()

        // Shows a message
        onElement(pollIntervalMs = 50) {
            textAsString()?.startsWith("The app has been hidden") == true ||
                textAsString()?.startsWith(
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "L’application a été masquée"
                ) == true
        }

        // Does not show the app
        waitForStableInActiveWindow(stableIntervalMs = 1_000) // Wait for the app to get hidden
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareApp_${PackageNames.OSMAND_PLUS}"
            }
        )

        // Make the app visible in preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.HIDDEN_APPS)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareVisibleAppToggle_${PackageNames.OSMAND_PLUS}" }
            .click()

        // Shows the app
        goBackToElement { viewIdResourceName == "geoShareMainPane" }
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000) {
                viewIdResourceName == "geoShareApp_${PackageNames.OSMAND_PLUS}"
            }
    }

    @Test
    fun whenLinkIsHidden_itIsNotShownOnResultScreen() = uiAutomator {
        assumeAppInstalled(PackageNames.OSMAND_PLUS)

        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Share a URI with the app
        shareUri()

        // Hide a link
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
            .longClick()
        onElement { viewIdResourceName == "geoShareAppHide" }.click()

        // Shows a message
        onElement(pollIntervalMs = 50) {
            textAsString()?.startsWith("The web map has been hidden") == true ||
                textAsString()?.startsWith(
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "La carte web a été masquée"
                ) == true
        }

        // Does not show the link
        waitForStableInActiveWindow(stableIntervalMs = 3_000) // Wait for the app to get hidden
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
        )

        // Make the link visible in preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkListItemToggle_ce900ea1-2c5d-4641-82f3-a5429a68d603" }
            .click()

        // Shows the link
        goBackToElement { viewIdResourceName == "geoShareMainPane" }
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
    }
}
