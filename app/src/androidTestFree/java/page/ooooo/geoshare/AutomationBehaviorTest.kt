package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.SendPointAutomation
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class AutomationBehaviorTest : BehaviorTest {

    @Test
    fun copiesCoordinates() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)
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
        closeIntro()

        // Configure automation
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)
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

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun opensMessagingApp() = uiAutomator {
        runBlocking {
            // Test using Telegram fork, because it's available on F-Droid
            val messagingAppPackageName = PackageNames.TELEGRAM_FORK

            assumeAppInstalled(messagingAppPackageName)

            // Launch application and close intro
            launchApplication()
            closeIntro()

            // Configure automation
            goToUserPreferencesList()
            goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)
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
            onElement { this.packageName == messagingAppPackageName }
        }
    }

    @Test
    fun opensTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(PackageNames.TOMTOM)
            assumeDomainResolvable("tomtom.com")

            // Launch application and close intro
            launchApplication()
            closeIntro()

            // Configure automation
            goToUserPreferencesList()
            goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)
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
            onDialog("geoShareLocationRationaleDialog", timeoutMs = 20_000L) {
                confirm()
            }

            // Grant location permission
            waitForStableInActiveWindow() // Wait, otherwise tapping the location permission grant button does nothing
            grantLocationPermission()

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
        closeIntro()

        // Configure automation
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.AUTOMATION)
        val automation = SavePointsGpxAutomation
        val serializedString = Json.encodeToString<Automation>(automation)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
            .click()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Wait for the conversion to succeed
        assertConversionSucceeded(persistentListOf(WGS84Point(52.47254, 13.4345, source = Source.URI)))

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
}
