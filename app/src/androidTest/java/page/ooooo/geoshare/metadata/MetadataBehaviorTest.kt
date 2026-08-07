package page.ooooo.geoshare.metadata

import androidx.test.uiautomator.uiAutomator
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import page.ooooo.geoshare.tests.assertConversionSucceeds
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.confirmDialog
import page.ooooo.geoshare.tests.disableDarkMode
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Take screenshots for F-Droid and Google Play listing using [Screengrab].
 *
 * You should run this test via fastlane, which takes care of copying the screenshots from the testing device to the
 * metadata directory after the tests finish.
 */
class MetadataBehaviorTest {
    @get:Rule
    val localeTestRule = LocaleTestRule()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = uiAutomator {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
            enableSystemUIDemoMode()
            disableDarkMode()
        }

        @AfterClass
        @JvmStatic
        fun teardown() = uiAutomator {
            disableSystemUIDemoMode()
        }
    }

    @Test
    fun metadata() = uiAutomator {
        launchApplication()
        waitForAppToBeVisible()

        // Intro screen
        onElement { viewIdResourceName == "geoShareIntroPage_0" }
        quickWaitForStableInActiveWindow()
        Screengrab.screenshot("5")

        // Main form
        closeIntro()
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }
        quickWaitForStableInActiveWindow()
        Screengrab.screenshot("1")

        // Connection permission dialog
        shareUri("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")
        val dialog = onElement(20_000) { viewIdResourceName == "geoShareConnectionPermissionDialog" }
        Screengrab.screenshot("4")

        // Result screen
        dialog.confirmDialog()
        assertConversionSucceeds(WGS84Point(42.5784957, 1.8955661, source = Source.URI))
        Screengrab.screenshot("2")

        // Automation preferences screen
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }.click()
        quickWaitForStableInActiveWindow()
        Screengrab.screenshot("3")
    }
}
