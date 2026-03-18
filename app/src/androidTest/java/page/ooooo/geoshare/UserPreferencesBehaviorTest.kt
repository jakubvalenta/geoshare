package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.BehaviorTest.Companion.ELEMENT_DOES_NOT_EXIST_TIMEOUT
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.android.OSMAND_PLUS_PACKAGE_NAME
import page.ooooo.geoshare.lib.formats.CoordsFormat
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.ui.UserPreferencesGroupId

@RunWith(AndroidJUnit4::class)
class UserPreferencesBehaviorTest : BehaviorTest {

    @Test
    fun whenCoordinateFormatIsSet_showsCoordinatesInThatFormat() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a URI with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows coordinates in the decimal degrees format
        onElement {
            if (viewIdResourceName == "geoShareResultSuccessLastPointCoordinates") {
                assertEquals(
                    CoordsFormat.formatDecCoords(GCJ02Point(52.5067296, 13.2599309)),
                    textAsString(),
                )
                true
            } else {
                false
            }
        }

        // Set coordinate format to degrees, minutes, seconds
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.COORDINATE_FORMAT)
        onElement { viewIdResourceName == "geoShareUserPreferenceCoordinateFormat_${CoordinateFormat.DEG_MIN_SEC}" }.click()
        goToMainScreenFromUserPreferencesDetail()

        // Shows coordinates in the degrees, minutes, seconds format
        onElement {
            if (viewIdResourceName == "geoShareResultSuccessLastPointCoordinates") {
                assertEquals(
                    CoordsFormat.formatDegMinSecCoords(GCJ02Point(52.5067296, 13.2599309)),
                    textAsString(),
                )
                true
            } else {
                false
            }
        }
    }

    @Test
    fun whenAppIsHidden_itIsNotShownOnResultScreen() = uiAutomator {
        assumeAppInstalled(OSMAND_PLUS_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a URI with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Hide an app
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareApp_${OSMAND_PLUS_PACKAGE_NAME}" }
            .longClick()
        onElement { viewIdResourceName == "geoShareAppHide" }.click()

        // Shows a message
        onElement(pollIntervalMs = 50L) {
            textAsString()?.startsWith("The app has been hidden") == true
            // TODO Add French
        }

        // Does not show the app
        waitForStableInActiveWindow(stableIntervalMs = 1_000L) // Wait for the app to get hidden
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareApp_${OSMAND_PLUS_PACKAGE_NAME}"
            }
        )

        // Make the app visible in preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.HIDDEN_APPS)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareVisibleAppToggle_${OSMAND_PLUS_PACKAGE_NAME}" }
            .click()
        goToMainScreenFromUserPreferencesDetail()

        // Shows the app
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000L) {
                viewIdResourceName == "geoShareApp_${OSMAND_PLUS_PACKAGE_NAME}"
            }
    }

    @Test
    fun whenLinkIsHidden_itIsNotShownOnResultScreen() = uiAutomator {
        assumeAppInstalled(OSMAND_PLUS_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Share a URI with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Hide a link
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000L) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
            .longClick()
        onElement { viewIdResourceName == "geoShareAppHide" }.click()

        // Shows a message
        onElement(pollIntervalMs = 50L) {
            textAsString()?.startsWith("The web map has been hidden") == true
            // TODO Add French
        }

        // Does not show the link
        waitForStableInActiveWindow(stableIntervalMs = 3_000L) // Wait for the app to get hidden
        assertNull(
            onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
        )

        // Make the link visible in preferences
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.LINKS)
        onElement { viewIdResourceName == "geoShareLinksListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinksListItemToggle_ce900ea1-2c5d-4641-82f3-a5429a68d603" }
            .click()
        goToMainScreenFromUserPreferencesDetail()

        // Shows the link
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, timeoutMs = 3_000L) {
                viewIdResourceName == "geoShareApp_ce900ea1-2c5d-4641-82f3-a5429a68d603"
            }
    }
}
