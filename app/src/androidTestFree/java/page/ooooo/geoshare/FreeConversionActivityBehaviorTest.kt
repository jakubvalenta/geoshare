package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.android.PackageNames

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
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareResultSuccessMessage" }

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun automationOpensApp() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationOpenApp_${PackageNames.GOOGLE_MAPS}" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000L) { packageName == PackageNames.GOOGLE_MAPS })

        // Google Maps opens
        onElement { packageName == PackageNames.GOOGLE_MAPS }

        // Go back to Geo Share
        launchApplication()

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun automationOpensTomTom() = uiAutomator {
        assumeAppInstalled(PackageNames.TOMTOM)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        waitForStableInActiveWindow()
        device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, 0, 10) }
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomationShareGpxWithApp" }.click()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

        // Confirm location rationale
        onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
            confirm()
        }

        // Grant location permission
        grantLocationPermission()

        // TomTom starts navigation
        waitAndAssertTomTomContainsElement {
            when (textAsString()) {
                "Drive", "Aller" -> true
                else -> false
            }
        }
    }

    private fun goToUserPreferencesDetailAutomationScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_AUTOMATION" }.click()
    }
}
