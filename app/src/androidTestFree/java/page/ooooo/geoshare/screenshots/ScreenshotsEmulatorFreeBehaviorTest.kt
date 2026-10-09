package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenPointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.goBackToMainForm
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.scrollToAppIcon
import page.ooooo.geoshare.tests.scrollToAutomationItem
import page.ooooo.geoshare.tests.setAppLocales
import page.ooooo.geoshare.tests.setMainInput
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.submitMainForm
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import kotlin.time.Duration.Companion.seconds

/**
 * Takes screenshots for documentation purposes, such as the Weblate translation service.
 *
 * This test suit must be run on a device with the Google Maps, OsmAnd and Conversations apps installed.
 */
class ScreenshotsEmulatorFreeBehaviorTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = uiAutomator {
            enableSystemUIDemoMode()
            enableDarkMode()
            setAppLocales("en-US")
        }

        @AfterClass
        @JvmStatic
        fun teardown() = uiAutomator {
            disableSystemUIDemoMode()
        }
    }

    @Test
    fun screenshotsEmulator() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        assumeAppInstalled(PackageNames.OSMAND_PLUS)
        assumeAppInstalled(PackageNames.CONVERSATIONS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Close help messages
        setMainInput()
        submitMainForm()
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        shareUri()
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        quickWaitForStableInActiveWindow()

        // Conversion - Result - App - Messaging
        scrollToAppIcon(PackageNames.CONVERSATIONS).longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_app_messaging")
        pressBack() // Close app menu

        // Conversion - Result - App - OsmAnd
        scrollToAppIcon(PackageNames.OSMAND_PLUS).longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_app_osmand")
        pressBack() // Close app menu

        // Preferences - Automation - Messaging
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
        scrollToAutomationItem(SendPointAutomation(PackageNames.CONVERSATIONS))
        saveScreenshot("main_strings/preferences_automation_messaging")

        // Preferences - Automation - OsmAnd
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
        scrollToAutomationItem(OpenPointsGpxAutomation(PackageNames.OSMAND_PLUS))
        saveScreenshot("main_strings/preferences_automation_osm_and")

        // Automation - Share - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(ShareDisplayGeoUriAutomation).click()
        goBackToMainForm()
        setMainInput()
        submitMainForm()
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_share_waiting")
        runBlocking {
            delay(5.seconds) // Wait for the automation waiting to finish
        }
        pressBack() // Close the system share menu

        // Automation - Share GPX route - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(ShareRouteGpxAutomation).click()
        goBackToMainForm()
        setMainInput()
        submitMainForm()
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_share_gpx_route_waiting")
        runBlocking {
            delay(5.seconds) // Wait for the automation waiting to finish
        }
        pressBack() // Close the system share menu

        // Automation - Share GPX route - Success
        saveScreenshot("main_strings/automation_share_gpx_route_success") // Don't wait, because the message will disappear fast

        // Reset automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(NoopAutomation).click()
    }
}
