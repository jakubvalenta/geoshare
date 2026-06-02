package page.ooooo.geoshare

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import kotlin.time.Duration.Companion.seconds

class AutomationBehaviorTest {
    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

    @Test
    fun copiesCoordinates() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetail(UserPreferenceGroupId.AUTOMATION)
        val automation = CopyCoordsDecAutomation
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomation_${Json.encodeToString<Automation>(automation)}" }.click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation success message
        onElement(pollIntervalMs = 50L) { viewIdResourceName == "geoShareResultSuccessMessage" }

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
        val automation = OpenDisplayGeoUriAutomation(PackageNames.GOOGLE_MAPS)
        val serializedString = Json.encodeToString<Automation>(automation)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
            .click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000L) { packageName == PackageNames.GOOGLE_MAPS })

        // Google Maps opens
        onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS }

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
            val automation = SendPointAutomation(messagingAppPackageName)
            val serializedString = Json.encodeToString<Automation>(automation)
            onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
                .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
                .click()

            // Share a geo: URI with the app
            shareUri("geo:52.47254,13.4345")

            // Shows automation counter
            onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

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
            val automation = OpenRouteOnePointGpxAutomation(PackageNames.TOMTOM)
            val serializedString = Json.encodeToString<Automation>(automation)
            onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
                .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
                .click()

            // Share a geo: URI with the app
            shareUri("geo:52.47254,13.4345")

            // Shows automation counter
            onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

            // Confirm location rationale
            onElement(20_000L) { viewIdResourceName == "geoShareLocationRationaleDialog" }.confirmDialog()

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
        val automation = SavePointsGpxAutomation
        val serializedString = Json.encodeToString<Automation>(automation)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
            .click()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

        // Choose file
        chooseFile()

        // Shows automation success message
        onElement {
            textAsString() in setOf(
                "Automatically saved GPX",
                @Suppress("SpellCheckingInspection") "GPX enregistré automatiquement",
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
        val automation = SavePointToContactAutomation
        val serializedString = Json.encodeToString<Automation>(automation)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
            .click()

        // Share a geo: URI with the app
        val point = WGS84Point(NaivePoint.genRandomPoint())
        shareUri(GeoUriFormatter.formatGeoUriString(point))

        // Insert or edit the test contact
        insertOrEditContact()

        // Open the test contact
        openContact()

        // The test contact contains coordinates
        val expectedCoordinates = CoordinateFormatter.formatDecCoords(point)
        onElement { textAsString() == expectedCoordinates }
    }
}
