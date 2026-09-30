package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.tests.assumeAppInstalled
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.confirmDialog
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.goBackToMainForm
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.setAppLocales
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
            setAppLocales("en-US")
        }

        @AfterClass
        @JvmStatic
        fun teardown() = uiAutomator {
            disableSystemUIDemoMode()
        }
    }

    /**
     * Takes all screenshots in one big test method.
     *
     * See `ScreenshotsFreeBehaviorTest`.
     */
    @Test
    fun screenshots() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Test all screens in alphabetical order
        testConversion()
        testPreferences()
    }

    fun testConversion() = uiAutomator {
        // Configure server (don't use helper method, because it fails on a managed device for some reason)
        onElement(1_000) { viewIdResourceName == "geoShareMainMenuButton" }.let { mainMenu ->
            mainMenu.click()
            onElement { viewIdResourceName == "geoShareMainMenuUserPreferences" }.click()
        }
        quickWaitForStableInActiveWindow() // Wait for the lazy list to render
        onElement { viewIdResourceName == "geoShareUserPreferencesListPane" }
            .scrollToElement(Direction.DOWN) {
                viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferenceGroupId.SERVERS}"
            }
            .click()
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceServer_google_maps_address_null" }.click()
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceServer_google_maps_place_null" }.click()
        }

        // Conversion - Error - Unsupported source place list
        shareUri("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg")
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        saveScreenshot("main_strings/conversion_error_unsupported_source_place_list")
        saveScreenshot("pro_strings/conversion_error_unsupported_source")

        // Conversion - Check - Name only
        shareUri("https://www.google.com/maps/place/Hermannstr.+20,+Berlin/")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }
            .onElement { viewIdResourceName == "geoShareHelpMessageDismiss" }
            .click()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        saveScreenshot("main_strings/conversion_result_check_name_only")

        // Conversion - Check - Points name only
        shareUri("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        saveScreenshot("main_strings/conversion_result_check_points_name_only")

        goBackToMainForm()
    }

    fun testPreferences() = uiAutomator {
        // Preferences - Servers - Page 1
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("pro_strings/preferences_servers_page_1")

        goBackToMainForm()
    }
}
