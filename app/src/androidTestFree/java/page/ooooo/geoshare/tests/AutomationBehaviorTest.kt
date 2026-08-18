package page.ooooo.geoshare.tests

import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import kotlin.time.Duration.Companion.seconds

class AutomationBehaviorTest {
    @Test
    fun copiesCoordinates() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(CopyCoordsDecAutomation).click()

        // Share a URI with the app
        shareUri()

        // Shows automation success message
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun opensApp() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)).click()

        // Share a URI with the app
        shareUri()

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000) { packageName == PackageNames.GOOGLE_MAPS })

        // Google Maps opens
        onElement(20_000) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows automation screen instead of result screen, because the app finished and the automation screen is the
        // one we had last opened when we were configuring automation
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
    }

    @Test
    fun opensMessagingApp() = uiAutomator {
        runBlocking {
            val messagingAppPackageName = PackageNames.CONVERSATIONS
            assumeAppInstalled(messagingAppPackageName)

            // Launch application and close intro
            launchApplication()
            closeIntro()

            // Configure automation
            goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
            scrollToAutomationItem(SendPointAutomation(messagingAppPackageName)).click()

            // Share a URI with the app
            shareUri()

            // Shows automation counter
            onElement { viewIdResourceName == "geoShareResultAutomationCounter" }

            // Opens the messaging app
            onElement { packageName == messagingAppPackageName }
        }
    }

    @Test
    fun launchesNavigationInTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(PackageNames.TOMTOM)
            assumeDomainResolvable("tomtom.com")

            // Launch application and close intro
            launchApplication()
            closeIntro()

            // Configure automation
            goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
            scrollToAutomationItem(OpenRouteOnePointGpxAutomation(PackageNames.TOMTOM)).click()

            // Share a URI with the app
            shareUri()

            // Shows automation counter
            onElement { viewIdResourceName == "geoShareResultAutomationCounter" }

            // Confirm location rationale
            onElement(20_000) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

            // Grant location permission
            waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
            grantSystemPermission()

            mockLocation {
                // Set location
                launch(Dispatchers.IO) {
                    delay(3.seconds)
                    setLocation(52.474160, 13.455457)
                }

                // TomTom starts navigation
                waitAndAssertTomTomContainsElement { textAsString() in setOf("Drive", "Aller") }
            }
        }
    }

    @Test
    fun savesGpxRoute() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(SavePointsGpxAutomation).click()

        // Share a URI with the app
        shareUri()

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultAutomationCounter" }

        // Choose file
        chooseFile()

        // Shows automation success message
        onElement(pollIntervalMs = 50) {
            textAsString() in setOf(
                "Automatically saved GPX",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "GPX enregistré automatiquement",
            )
        }
    }

    @Test
    fun savesPointToContact() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        scrollToAutomationItem(SavePointToContactAutomation).click()

        // Share a unique URI with the app
        val point = WGS84Point(NaivePoint.genRandomPoint())
        sharePoint(point)

        // Insert or edit the test contact
        insertOrEditContact()

        // Open the test contact
        openContact()

        // The test contact contains coordinates
        assertContactContainsText(CoordinateFormatter.formatDecCoords(point))
    }
}
