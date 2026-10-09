package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.tests.TestServer
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.configureServer
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.grantConnectionPermission
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.setAppLocales
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import page.ooooo.geoshare.ui.UserPreferenceGroupId

/**
 * Takes screenshots for documentation purposes, such as the Weblate translation service.
 *
 * This test suit must be run on a device with the Google Maps app installed.
 */
class ScreenshotsManagedDeviceProBehaviorTest {
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
    fun screenshots() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Preferences - Servers - Page 1
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("pro_strings/preferences_servers_page_1")

        // Conversion - Error - Unsupported source place list
        configureServer(TestServer.None)
        shareUri("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg")
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        saveScreenshot("main_strings/conversion_error_unsupported_source_place_list")
        saveScreenshot("pro_strings/conversion_error_unsupported_source")

        // Conversion - Check - Name only
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")
        grantConnectionPermission()
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        saveScreenshot("main_strings/conversion_result_check_name_only")

        // Conversion - Check - Points name only
        shareUri("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France")
        grantConnectionPermission()
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        saveScreenshot("main_strings/conversion_result_check_points_name_only")
    }
}
