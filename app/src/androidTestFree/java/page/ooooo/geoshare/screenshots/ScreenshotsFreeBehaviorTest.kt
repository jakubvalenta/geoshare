package page.ooooo.geoshare.screenshots

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.CopyLinkUriAutomation
import page.ooooo.geoshare.data.local.preferences.DynamicColorPreference
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenPointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.data.local.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.lib.inputs.InputDocumentationGroup
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.chooseFile
import page.ooooo.geoshare.tests.confirmDialog
import page.ooooo.geoshare.tests.disableSystemUIDemoMode
import page.ooooo.geoshare.tests.dismissDialog
import page.ooooo.geoshare.tests.enableDarkMode
import page.ooooo.geoshare.tests.enableSystemUIDemoMode
import page.ooooo.geoshare.tests.expandSheet
import page.ooooo.geoshare.tests.fillLinkForm
import page.ooooo.geoshare.tests.fillServerForm
import page.ooooo.geoshare.tests.goBackToElement
import page.ooooo.geoshare.tests.goBackToMainForm
import page.ooooo.geoshare.tests.goToInputList
import page.ooooo.geoshare.tests.goToUserPreferencesDetail
import page.ooooo.geoshare.tests.grantSystemPermission
import page.ooooo.geoshare.tests.isAppInstalled
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.launchNavigationInApp
import page.ooooo.geoshare.tests.longScrollSheet
import page.ooooo.geoshare.tests.mockLocation
import page.ooooo.geoshare.tests.onMainScrollablePane
import page.ooooo.geoshare.tests.onSheetItem
import page.ooooo.geoshare.tests.quickWaitForStableInActiveWindow
import page.ooooo.geoshare.tests.saveLinkForm
import page.ooooo.geoshare.tests.saveScreenshot
import page.ooooo.geoshare.tests.saveServerForm
import page.ooooo.geoshare.tests.scrollToAutomationItem
import page.ooooo.geoshare.tests.scrollToSheetItem
import page.ooooo.geoshare.tests.setAppLocales
import page.ooooo.geoshare.tests.setMainInput
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible
import page.ooooo.geoshare.tests.withNetworkOff
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Make screenshots for documentation purposes, such as the Weblate translation service.
 */
class ScreenshotsFreeBehaviorTest {
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
     * Take all screenshots in one big test method.
     *
     * This method is not split into several smaller methods annotated by [Test], because AndroidJunitRunner's
     * `clearPackageData` setting (which we set in build.gradle) clears all files from
     * `build/outputs/(connected|managed_device)_android_test_additional_output` after each test method. We want all
     * screenshot files to be kept there, so that we can later copy them to `docs/screenshots`.
     *
     * This method is however internally split into several smaller non-test methods, so that developers can easily
     * comment out parts of it during debugging.
     */
    @Test
    fun screenshots() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.google.com")
        }

        launchApplication()
        waitForAppToBeVisible()

        // First test intro, because it automatically appears
        testIntro()

        // Then test main, because it's already visible after closing the intro
        testMain()

        // Then test preferences, because they've not been changed by other tests yet
        testPreferences()
        testPreferencesAutomationMessaging()
        testPreferencesAutomationOsmAnd()

        // Then test all other screens in alphabetical order
        testAbout()
        testAutomation()
        testAutomationShare()
        testFaq()
        testConversionErrors()
        testConversionPermission()
        testConversionResultApps()
        testConversionResultAppsMessaging()
        testConversionResultAppsOsmAnd()
        testConversionResultChecks()
        testConversionResultLocation()
        testConversionResultPoints()
        testInputs()
        testLinks()
        testServers()
    }

    fun testIntro() = uiAutomator {
        // Intro - How to show a map location
        onElement { viewIdResourceName == "geoShareIntroPage_0" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/intro_how_to_show_a_map_location")

        // Intro - Open links in GeoShare - Page 1
        onElement { viewIdResourceName == "geoShareIntroNextButton" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/intro_open_links_in_geo_share_page_1")

        // Intro - Open links in GeoShare - Page 2
        onElement { viewIdResourceName == "geoShareIntroPage_1" }.scroll(Direction.DOWN, 2f)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/intro_open_links_in_geo_share_page_2")

        onElement { viewIdResourceName == "geoShareIntroNextButton" }.click() // Finish intro
    }

    fun testMain() = uiAutomator {
        // Main
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/main")

        // Main - Error - Missing URL
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/main_error_missing_url")

        // Main - Menu
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/main_menu")

        pressBack() // Close main menu
    }

    fun testAbout() = uiAutomator {
        // About
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuAbout" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/about")

        // Licenses
        onElement { textAsString() == "Licenses" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/licenses")
        pressBack() // Go back to about screen

        goBackToMainForm()
    }

    fun testAutomation() = uiAutomator {
        if (!isAppInstalled(PackageNames.GOOGLE_MAPS)) {
            return@uiAutomator
        }

        // Automation - Copy coordinates - Success
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(CopyCoordsDecAutomation).click()
        goBackToMainForm()
        setMainInput()
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_copy_coords_success")

        // Automation - Open app - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)).click()
        goBackToMainForm()
        setMainInput()
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_open_app_waiting")
        onElement { viewIdResourceName == "geoShareResultAutomationCancel" }.click()

        // Automation - Copy link - Success
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(CopyLinkUriAutomation(UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID))).click()
        goBackToMainForm()
        setMainInput()
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_copy_link_success")

        // Automation - Save GPX - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(SavePointsGpxAutomation).click()
        goBackToMainForm()
        setMainInput()
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_save_gpx_waiting")

        // Automation - Save GPX - Success
        chooseFile()
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
        saveScreenshot("main_strings/automation_save_gpx_success")

        // Reset automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(NoopAutomation).click()

        goBackToMainForm()
    }

    fun testAutomationShare() = uiAutomator {
        // Require at least two installed map apps, so that Android shows a share menu
        if (!isAppInstalled(PackageNames.GOOGLE_MAPS) || !isAppInstalled(PackageNames.OSMAND_PLUS)) {
            return@uiAutomator
        }

        // Automation - Share - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(ShareDisplayGeoUriAutomation).click()
        goBackToMainForm()
        setMainInput()
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/automation_share_waiting")
        runBlocking {
            delay(5.seconds) // Wait for the automation waiting to finish
        }
        pressBack() // Close the system share menu

        // Automation - Share - Success
        saveScreenshot("main_strings/automation_share_success") // Don't wait, because the message will disappear fast

        // Automation - Share GPX route - Waiting
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(ShareRouteGpxAutomation).click()
        goBackToMainForm()
        setMainInput()
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

        goBackToMainForm()
    }

    fun testConversionErrors() = uiAutomator {
        // Conversion - Error - Parse error
        shareUri("https://www.google.com/maps/spam")
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_error_parse_error")

        // Conversion - Error - Unshorten error
        shareUri("https://maps.app.goo.gl/spam")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_error_unshorten_error")

        // Conversion - Error - Unsupported map service
        shareUri("spam")
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_error_unsupported_map_service")

        goBackToMainForm()
    }

    fun testConversionPermission() = uiAutomator {
        val uriString = "https://www.openstreetmap.org/relation/910699"
        shareUri(uriString)

        withNetworkOff {
            // Conversion - Permission
            onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.let { dialog ->
                quickWaitForStableInActiveWindow()
                saveScreenshot("main_strings/conversion_permission")
                dialog.confirmDialog()
            }

            // Conversion - Loading indicator
            onElement(20_000) { viewIdResourceName == "geoShareMainLoadingIndicatorDescription" }
            quickWaitForStableInActiveWindow()
            saveScreenshot("main_strings/conversion_loading_indicator")

            // Conversion - Error - Canceled
            onElement { viewIdResourceName == "geoShareMainLoadingIndicatorCancel" }.click()
            onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
            quickWaitForStableInActiveWindow()
            saveScreenshot("main_strings/conversion_error_cancelled")
        }

        // Conversion - Error - Permission denied
        shareUri(uriString)
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareConversionErrorMessage" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_error_permission_denied")

        goBackToMainForm()
    }

    fun testConversionResultApps() = uiAutomator {
        if (!isAppInstalled(PackageNames.GOOGLE_MAPS)) {
            return@uiAutomator
        }

        shareUri()

        // Conversion - Result - App - Google Maps
        quickWaitForStableInActiveWindow() // Wait for the result to render
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, 3_000) {
                viewIdResourceName == "geoShareApp_${PackageNames.GOOGLE_MAPS}"
            }
            .longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_app_google_maps")

        // Conversion - Result - Message - App hidden
        onElement { viewIdResourceName == "geoShareAppHide" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_message_app_hidden")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Conversion - Result - Share
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, 3_000) {
                viewIdResourceName == "geoShareApp_share"
            }
            .longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_share")
        pressBack() // Close app menu

        // Conversion - Result - Web map
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, 3_000) {
                viewIdResourceName == "geoShareApp_${InitialLinks.APPLE_MAPS_DISPLAY_UUID}"
            }
            .longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_web_map")

        // Conversion - Result - Message - Web map hidden
        onElement { viewIdResourceName == "geoShareAppHide" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_message_web_map_hidden")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        goBackToMainForm()
    }

    fun testConversionResultAppsMessaging() = uiAutomator {
        if (!isAppInstalled(PackageNames.CONVERSATIONS)) {
            return@uiAutomator
        }

        shareUri()

        // Conversion - Result - App - Messaging
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, 3_000) {
                viewIdResourceName == "geoShareApp_${PackageNames.CONVERSATIONS}"
            }
            .longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_app_messaging")
        pressBack() // Close app menu

        goBackToMainForm()
    }

    fun testConversionResultAppsOsmAnd() = uiAutomator {
        if (!isAppInstalled(PackageNames.OSMAND_PLUS)) {
            return@uiAutomator
        }

        shareUri()

        // Conversion - Result - App - OsmAnd
        onMainScrollablePane()
            .scrollToElement(Direction.DOWN, 3_000) {
                viewIdResourceName == "geoShareApp_${PackageNames.OSMAND_PLUS}"
            }
            .longClick()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_app_osmand")
        pressBack() // Close app menu

        goBackToMainForm()
    }

    fun testConversionResultChecks() = uiAutomator {
        // Conversion - Check - Experimental
        shareUri("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_check_experimental")

        // Conversion - Check - Map center
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_check_map_center")

        // Conversion - Check - SRS
        shareUri("https://www.google.com/maps/place/Forbidden+City/@39.9165742,116.3945834,17z/data=!4m7!3m6!1s0x35f052e94515d43d:0x674e2bd4dd3079f!8m2!3d39.9168038!4d116.3971621!15sCg5mb3JiaWRkZW4gY2l0eVoQIg5mb3JiaWRkZW4gY2l0eZIBEnRvdXJpc3RfYXR0cmFjdGlvbuABAA!16zL20vMGowYjI?entry=tts&g_ep=EgoyMDI2MDMwOS4wIPu8ASoASAFQAw%3D%3D&skid=5f340da1-a0d3-4b1c-bc05-7f90cfbd502a")
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_check_srs")

        goBackToMainForm()
    }

    fun testConversionResultLocation() = uiAutomator {
        if (!isAppInstalled(PackageNames.TOMTOM)) {
            return@uiAutomator
        }

        shareUri()

        // Conversion - Result - Location - Rationale
        onMainScrollablePane()
            // Scroll by percents, because it's more reliable than scrolling to the app icon
            .scroll(Direction.DOWN, 2f)
        launchNavigationInApp(PackageNames.TOMTOM)
        onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.let { dialog ->
            quickWaitForStableInActiveWindow()
            saveScreenshot("main_strings/conversion_result_location_rationale")
            dialog.confirmDialog()
        }

        // Conversion - Result - Location - Loading
        waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
        grantSystemPermission()
        onMainScrollablePane().scroll(Direction.UP, 3f) // Scroll up to see message
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

        goBackToMainForm()
    }

    fun testConversionResultPoints() = uiAutomator {
        if (!isAppInstalled(PackageNames.GOOGLE_MAPS)) {
            return@uiAutomator
        }

        shareUri("https://www.openstreetmap.org/relation/910699")
        onElement { viewIdResourceName == "geoShareConnectionPermissionDialog" }.confirmDialog()

        // Conversion - Result
        onElement { viewIdResourceName == "geoShareResultLastPointName" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result")

        // Conversion - Result - Message - Copy success
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        onElement { viewIdResourceName == "geoShareResultSheet" }.apply {
            expandSheet()
            scrollToSheetItem(Direction.UP) {
                textAsString() in setOf(
                    "Copy coordinates",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Copier les coordonnées",
                )
            }
                .click()
        }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_message_copy_success")

        // Conversion - Result - Sheet - Page 1
        onElement { viewIdResourceName == "geoShareResultLastPointMenu" }.click()
        val sheet = onElement { viewIdResourceName == "geoShareResultSheet" }
        sheet.expandSheet()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_sheet_page_1")

        // Conversion - Result - Sheet - Page 2
        sheet.longScrollSheet() // Speed up scrolling to the item, which is at the bottom of the sheet
        sheet.scrollToSheetItem {
            textAsString() in setOf(
                "Save to contact",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Enregistrer dans les contacts",
            )
        }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_sheet_page_2")

        // Conversion - Result - Save GPX - File chooser
        sheet.onSheetItem {
            textAsString() in setOf(
                "Save GPX route",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Enregistrer l’itinéraire GPX",
            )
        }
            .click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_save_gpx_file_chooser")

        // Conversion - Result - Message - Save GPX success
        chooseFile()
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_message_save_gpx_success")

        // Conversion - Result - Points
        onElement { viewIdResourceName == "geoShareResultPoints" }.click() // Expand points pane
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_points")
        onElement { viewIdResourceName == "geoShareResultPoints" }.click() // Collapse points pane
        quickWaitForStableInActiveWindow() // Wait for the points pane to collapse

        // Conversion - Result - Save GPX
        onElement { viewIdResourceName == "geoShareResultPointsChips" }
            .scroll(Direction.RIGHT, 10f) // Notice that we assume the language is LTR and swipe to the right
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/conversion_result_save_gpx")

        goBackToMainForm()
    }

    fun testFaq() = uiAutomator {
        // FAQ
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuFaq" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/faq_list")

        // FAQ - How it works
        onElement { viewIdResourceName == "geoShareFaqItem_${FaqItemId.HOW_IT_WORKS}" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/faq_how_it_works")

        // FAQ - Location permission
        onElement { viewIdResourceName == "geoShareFaqItem_${FaqItemId.LOCATION_PERMISSION}" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/faq_location_permission")

        // FAQ - Name only
        onElement { viewIdResourceName == "geoShareFaqItem_${FaqItemId.NAME_ONLY}" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/faq_name_only")

        // FAQ - Privacy
        onElement { viewIdResourceName == "geoShareFaqItem_${FaqItemId.PRIVACY}" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/faq_privacy_considerations")

        goBackToMainForm()
    }

    fun testInputs() = uiAutomator {
        // Supported maps - Recent
        goToUserPreferencesDetail(UserPreferenceGroupId.DEVELOPER_OPTIONS)
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }
            .setText("44")
        goBackToMainForm()
        goToInputList()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/supported_maps_recent")

        // Supported maps - All - Page 1
        onElement { viewIdResourceName == "geoShareInputListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.OSM_AND}" }
        saveScreenshot("main_strings/supported_maps_all_page_1")

        // Supported maps - All - Page 2
        onElement { viewIdResourceName == "geoShareInputListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.GEO_URI}" }
        saveScreenshot("main_strings/supported_maps_all_page_2")

        // Supported maps - Detail - Coordinates
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.COORDINATES}" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/supported_maps_detail_coordinates")
        goBackToElement { viewIdResourceName == "geoShareInputListPane" }

        // Supported maps - Detail
        onElement { viewIdResourceName == "geoShareInputListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.APPLE_MAPS}" }
            .click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/supported_maps_detail")

        goBackToMainForm()
    }

    fun testLinks() = uiAutomator {
        // Web maps - List
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_list")

        // Web maps - Reset - Button
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkRestoreInitialButton" }
        saveScreenshot("main_strings/web_maps_reset_button")

        // Web maps - Reset - Dialog
        onElement { viewIdResourceName == "geoShareLinkRestoreInitialButton" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_reset_dialog")

        // Web maps - Message - Reset
        onElement { viewIdResourceName == "geoShareLinkRestoreInitialDialog" }.confirmDialog()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_message_reset")

        // Web maps - Insert
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareLinkListInsert" }
            .click()
        quickWaitForStableInActiveWindow()
        pressBack() // Hide IME
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_insert")

        // Web maps - Message - Insert
        val link = Link(
            name = "My New Maps",
            coordsUriTemplate = "https://www.example.com/?ll={lat}%2C{lon}",
        )
        fillLinkForm(link)
        saveLinkForm()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_message_insert")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Web maps - Detail - Page 1
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        quickWaitForStableInActiveWindow()
        pressBack() // Hide IME
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_detail_page_1")

        // Web maps - Detail - Page 2
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSave" }
        saveScreenshot("main_strings/web_maps_detail_page_2")

        // Web maps - Detail - Advanced
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareLinkFormAdvanced" }
            .click()
        quickWaitForStableInActiveWindow()
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.scroll(Direction.DOWN, 5f)
        saveScreenshot("main_strings/web_maps_detail_advanced")

        // Web maps - Detail - Advanced - Coordinate system
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSRS_${Srs.WGS84}" }
            .click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_detail_advanced_coordinate_system")

        // Web maps - Message - Update
        saveLinkForm()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_message_update")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Web maps - Delete dialog
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkDetailDelete" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_delete_dialog")

        // Web maps - Message - Delete
        onElement { viewIdResourceName == "geoShareLinkDeleteDialog" }.confirmDialog()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/web_maps_message_delete")

        goBackToMainForm()
    }

    fun testPreferences() = uiAutomator {
        if (!isAppInstalled(PackageNames.GOOGLE_MAPS)) {
            return@uiAutomator
        }

        // Preferences - List (initial values, before the user changes anything)
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement { viewIdResourceName == "geoShareMainMenuUserPreferences" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_list")

        // Preferences - List - Page 2
        onElement { viewIdResourceName == "geoShareUserPreferencesListPane" }.scroll(Direction.DOWN, 1f)
        saveScreenshot("main_strings/preferences_list_page_2")

        if (DynamicColorPreference.isAvailable()) {
            // Preferences - Appearance - Dynamic color
            goToUserPreferencesDetail(UserPreferenceGroupId.APPEARANCE_DYNAMIC_COLOR)
            quickWaitForStableInActiveWindow()
            saveScreenshot("main_strings/preferences_dynamic_color")
        }

        // Preferences - Apps
        goToUserPreferencesDetail(UserPreferenceGroupId.HIDDEN_APPS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_apps")
        onElement { viewIdResourceName == "geoShareVisibleAppToggle_${PackageNames.GOOGLE_MAPS}" }.click()
        goBackToElement { viewIdResourceName == "geoShareUserPreferencesListPane" }
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_apps_visible_count")
        goToUserPreferencesDetail(UserPreferenceGroupId.HIDDEN_APPS)
        onElement { viewIdResourceName == "geoShareVisibleAppToggle_${PackageNames.GOOGLE_MAPS}" }.click() // Make the app visible again to not affect other tests

        // Preferences - Automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_automation")

        // Preferences - Automation - Web maps
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            // Scroll by percent not to element, because scrolling to element is unreliable, due to the lazy list loading
            .scroll(Direction.DOWN, 3f)
        saveScreenshot("main_strings/preferences_automation_web_maps")

        // Preferences - Automation delay
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION_DELAY)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_automation_delay")

        // Preferences - Connection permission
        goToUserPreferencesDetail(UserPreferenceGroupId.CONNECTION_PERMISSION)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_connection_permission")

        // Preferences - Coordinate format
        goToUserPreferencesDetail(UserPreferenceGroupId.COORDINATE_FORMAT)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_coordinate_format")

        // Preferences - Developer options
        goToUserPreferencesDetail(UserPreferenceGroupId.DEVELOPER_OPTIONS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/preferences_developer_options")

        goBackToMainForm()
    }

    fun testPreferencesAutomationMessaging() = uiAutomator {
        if (!isAppInstalled(PackageNames.CONVERSATIONS)) {
            return@uiAutomator
        }

        // Preferences - Automation - Messaging
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
        scrollToAutomationItem(SendPointAutomation(PackageNames.CONVERSATIONS))
        saveScreenshot("main_strings/preferences_automation_messaging")

        goBackToMainForm()
    }

    fun testPreferencesAutomationOsmAnd() = uiAutomator {
        if (!isAppInstalled(PackageNames.OSMAND_PLUS)) {
            return@uiAutomator
        }

        // Preferences - Automation - OsmAnd
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
        scrollToAutomationItem(OpenPointsGpxAutomation(PackageNames.OSMAND_PLUS))
        saveScreenshot("main_strings/preferences_automation_osm_and")

        goBackToMainForm()
    }

    fun testServers() = uiAutomator {
        // Servers - List - Page 1
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_list_free_page_1")

        // Servers - List - Page 2
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareServerListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerRestoreInitialButton" }
        saveScreenshot("main_strings/servers_list_free_page_2")

        // Servers - Reset dialog
        onElement { viewIdResourceName == "geoShareServerRestoreInitialButton" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_reset_dialog")

        // Servers - Message - Reset
        onElement { viewIdResourceName == "geoShareServerRestoreInitialDialog" }.confirmDialog()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_message_reset")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Servers - Insert - API Key
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareServerListInsert" }
            .click()
        quickWaitForStableInActiveWindow()
        pressBack() // Hide IME
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_insert_api_key")

        // Servers - Insert - Attestation
        fillServerForm(
            Server(
                name = "Test Server",
                urlTemplate = "https://api.example.com/{q}",
                authType = ServerAuthType.ATTESTATION,
                challengeUrl = "https://api.example.com/auth/challenge",
                loginUrl = "https://api.example.com/auth/login",
                registerUrl = "https://api.example.com/auth/register",
            )
        )
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_insert_attestation")

        // Servers - Message - Insert
        saveServerForm()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_message_insert")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Servers - Update
        onElement { viewIdResourceName == "geoShareServerListItemMenu_${InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_${InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID}" }.click()
        fillServerForm(
            Server(
                name = "Google Maps Geocode Address",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
                apiKey = "abc",
                apiKeyHeader = "X-Goog-Api-Key",
                authType = ServerAuthType.API_KEY,
            )
        )
        quickWaitForStableInActiveWindow()
        pressBack() // Hide IME
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_update")

        // Servers - Message - Update
        saveServerForm()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_message_update")
        runBlocking {
            delay(3.seconds) // Wait for the message to disappear
        }

        // Servers - Delete - Dialog
        onElement { viewIdResourceName == "geoShareServerListItemMenu_${InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_${InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareServerDetailDelete" }.click()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_delete_dialog")

        // Servers - Message - Delete
        onElement { viewIdResourceName == "geoShareServerDeleteDialog" }.confirmDialog()
        quickWaitForStableInActiveWindow()
        saveScreenshot("main_strings/servers_message_delete")

        goBackToMainForm()
    }
}
