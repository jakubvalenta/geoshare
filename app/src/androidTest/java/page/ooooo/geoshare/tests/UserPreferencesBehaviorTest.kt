package page.ooooo.geoshare.tests

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.Finish
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class UserPreferencesBehaviorTest {
    @Test
    fun whenCoordinateFormatIsSet_showsCoordinatesInThatFormat() = uiAutomator {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

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
    fun whenFinishIsAfterActionSucceeded_appClosesItselfAfterCopyingCoordinates() = uiAutomator {
        // Share a unique URI with the app
        val point = WGS84Point(NaivePoint.genRandomPoint())
        sharePoint(point)

        // Shows result coordinates
        onElement {
            viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                textAsString() == CoordinateFormatter.formatDecCoords(point)
        }

        // Copy coordinates
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.apply {
            expandSheet()
            scrollToSheetItem(Direction.UP) {
                textAsString() in setOf(
                    "Copy coordinates",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Copier les coordonnées",
                )
            }
                .click()
        }
        quickWaitForStableInActiveWindow()

        // App still shows coordinates
        onElement {
            viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                textAsString() == CoordinateFormatter.formatDecCoords(point)
        }

        // Set finish preference to Always
        goToUserPreferencesDetail(UserPreferenceGroupId.FINISH)
        onElement { viewIdResourceName == "geoShareUserPreferenceFinish_${Finish.AFTER_ACTION_SUCCEEDED}" }.click()

        // Go back to result screen
        goBackToElement { viewIdResourceName == "geoShareResultLastPointCoordinates" }

        // Copy coordinates again
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.apply {
            expandSheet()
            scrollToSheetItem(Direction.UP) {
                textAsString() in setOf(
                    "Copy coordinates",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Copier les coordonnées",
                )
            }
                .click()
        }
        quickWaitForStableInActiveWindow()

        // App is not visible
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                    textAsString() == CoordinateFormatter.formatDecCoords(point)
            }
        )
    }

    @Test
    fun whenFinishIsNever_appDoesNotCloseItselfAfterOpeningMapApp() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Share a unique URI with the app
        val point = WGS84Point(NaivePoint.genRandomPoint())
        sharePoint(point)

        // Shows result coordinates
        onElement {
            viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                textAsString() == CoordinateFormatter.formatDecCoords(point)
        }

        // Open Google Maps
        clickAppIcon(PackageNames.GOOGLE_MAPS)

        // Wait for Google Maps
        onElement(20_000) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // App is not visible
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                    textAsString() == CoordinateFormatter.formatDecCoords(point)
            }
        )

        // Share the URI with the app again
        sharePoint(point)

        // Set finish preference to Never
        goToUserPreferencesDetail(UserPreferenceGroupId.FINISH)
        onElement { viewIdResourceName == "geoShareUserPreferenceFinish_${Finish.NEVER}" }.click()

        // Go back to result screen
        goBackToElement { viewIdResourceName == "geoShareResultLastPointCoordinates" }

        // Open Google Maps
        clickAppIcon(PackageNames.GOOGLE_MAPS)

        // Wait for Google Maps
        onElement(20_000) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back
        pressBack()
        quickWaitForStableInActiveWindow()
        pressBack()

        // App is visible and still shows the result
        onElement {
            viewIdResourceName == "geoShareResultLastPointCoordinates" &&
                textAsString() == CoordinateFormatter.formatDecCoords(point)
        }
    }

    @Test
    fun whenAppIsHidden_itIsNotShownOnResultScreen() = uiAutomator {
        assumeAppInstalled(PackageNames.OSMAND_PLUS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

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

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Share a URI with the app
        shareUri()

        // Hide a link
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000) {
                viewIdResourceName == "geoShareApp_${InitialLinks.APPLE_MAPS_DISPLAY_UUID}"
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
                viewIdResourceName == "geoShareApp_${InitialLinks.APPLE_MAPS_DISPLAY_UUID}"
            }
        )

        // Make the link visible in preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkListItemToggle_${InitialLinks.APPLE_MAPS_DISPLAY_UUID}" }
            .click()

        // Shows the link
        goBackToElement { viewIdResourceName == "geoShareMainPane" }
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000) {
                viewIdResourceName == "geoShareApp_${InitialLinks.APPLE_MAPS_DISPLAY_UUID}"
            }
    }
}
