package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.closeIntro
import page.ooooo.geoshare.tests.confirmDialog
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.goBackToMainForm
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class ScreenshotsProBehaviorTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = uiAutomator {
            enableSystemUIDemoMode()
            enableDarkMode()
        }

        @AfterClass
        @JvmStatic
        fun teardown() = uiAutomator {
            disableSystemUIDemoMode()
        }
    }

    /**
     * Take all screenshots in one big test method.
     *
     * See `ScreenshotsFreeBehaviorTest`.
     */
    @Test
    fun screenshots() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        launchApplication()
        waitForAppToBeVisible()
        closeIntro()
        quickWaitForStableInActiveWindow() // Wait for the intro to close

        // Test all screens in alphabetical order
        testConversion()
        testServers()
    }

    fun testConversion() = uiAutomator {
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsAddress_null" }
            .click()
        quickWaitForStableInActiveWindow() // Wait for the server to get saved

        // Conversion - Error - Unsupported source place list
        shareUri("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg")
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        saveScreenshot("conversion_error_unsupported_source_place_list")

        // Conversion - Check - Name only
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        saveScreenshot("conversion_result_check_name_only")

        // Conversion - Check - Points name only
        shareUri("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        saveScreenshot("conversion_result_check_points_name_only")

        goBackToMainForm()
    }

    fun testServers() = uiAutomator {
        // Servers - List - Page 1
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("servers_list_pro_page_1")

        goBackToMainForm()
    }
}
