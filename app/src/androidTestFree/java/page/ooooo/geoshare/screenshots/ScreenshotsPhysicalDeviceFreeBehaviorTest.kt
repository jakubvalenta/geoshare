package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.confirmDialog
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.grantSystemPermission
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.launchNavigationInApp
import page.ooooo.geoshare.tests.mockLocation
import page.ooooo.geoshare.tests.onMainScrollablePane
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.scrollToAppIcon
import page.ooooo.geoshare.tests.scrollToAppIcons
import page.ooooo.geoshare.tests.scrollToTop
import page.ooooo.geoshare.tests.setAppLocales
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import kotlin.time.Duration.Companion.seconds

/**
 * Takes screenshots for documentation purposes, such as the Weblate translation service.
 *
 * This test suit must be run on a device with the TomTom app installed.
 */
class ScreenshotsPhysicalDeviceFreeBehaviorTest {
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
    fun screenshotsPhysicalDevice() = uiAutomator {
        assumeAppInstalled(PackageNames.TOMTOM)
        assumeTrue(
            "This test currently fails, because the current version of the TomTom app doesn't seem to support GPX anymore",
            false,
        )

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Conversion - Result - Location - Rationale
        shareUri()
        scrollToAppIcons()
        scrollToAppIcon(PackageNames.TOMTOM).longClick()
        launchNavigationInApp()
        onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.let { dialog ->
            quickWaitForStableInActiveWindow()
            saveScreenshot("main_strings/conversion_result_location_rationale")
            dialog.confirmDialog()
        }

        // Conversion - Result - Location - Loading
        waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
        grantSystemPermission()
        onMainScrollablePane().scrollToTop()
        onElement { viewIdResourceName == "geoShareResultSmallLoadingIndicatorMessage" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_location_loading_indicator")

        // Conversion - Message - Error
        mockLocation {
            // Don't set location
        }
        runBlocking {
            delay(2.seconds)
        }
        saveScreenshot("main_strings/conversion_result_message_error")
    }
}
