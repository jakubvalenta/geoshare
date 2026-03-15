package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.formats.CoordsFormat
import page.ooooo.geoshare.lib.point.GCJ02Point

@RunWith(AndroidJUnit4::class)
class UserPreferencesBehaviorTest : BaseActivityBehaviorTest() {

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
        goToUserPreferencesDetailCoordinateFormatScreen()
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
}
