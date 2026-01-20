package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.AndroidTools

@RunWith(AndroidJUnit4::class)
class FreeConversionActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun automationCopiesCoordinates() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationCopyCoordsDec" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation success message
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareConversionSuccessMessage" }

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationPreferencesButton" }
    }

    @Test
    fun automationOpensApp() = uiAutomator {
        assertAppInstalled(AndroidTools.GOOGLE_MAPS_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationOpenApp_${AndroidTools.GOOGLE_MAPS_PACKAGE_NAME}" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000L) { packageName == AndroidTools.GOOGLE_MAPS_PACKAGE_NAME })

        // Google Maps opens
        onElement { packageName == AndroidTools.GOOGLE_MAPS_PACKAGE_NAME }

        // Go back to Geo Share
        launchApplication()

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareConversionSuccessAutomationPreferencesButton" }
    }
}
