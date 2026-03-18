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
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.point.WGS84Point
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class FreeConversionBehaviorTest : BehaviorTest {

    @Test
    fun automationCopiesCoordinates() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
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
    fun automationOpensApp() = uiAutomator {
        assumeAppInstalled(GOOGLE_MAPS_PACKAGE_NAME)

        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        val automation = OpenDisplayGeoUriAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val serializedString = Json.encodeToString<Automation>(automation)
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceAutomation_${serializedString}" }
            .click()

        // Share a Google Maps coordinates link with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Shows automation counter
        onElement { viewIdResourceName == "geoShareResultSuccessAutomationCounter" }

        // Google Maps doesn't open while the counter is running
        assertNull(onElementOrNull(3_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME })

        // Google Maps opens
        onElement(20_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME }

        // Go back to Geo Share
        launchApplication()

        // Shows automation preferences button
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun automationOpensTomTom() = uiAutomator {
        runBlocking {
            assumeAppInstalled(TOMTOM_PACKAGE_NAME)
            assumeDomainResolvable("tomtom.com")

            // Launch application and close intro
            launchApplication()
            closeIntro()

            // Configure automation
            goToUserPreferencesDetailAutomationScreen()
            val automation = OpenRouteOnePointGpxAutomation(TOMTOM_PACKAGE_NAME)
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
    fun automationSavesGpx() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Configure automation
        goToUserPreferencesDetailAutomationScreen()
        val automation = SavePointsGpxAutomation
        onElement { viewIdResourceName == "geoShareUserPreferenceAutomation_${Json.encodeToString<Automation>(automation)}" }.click()

        // Share a geo: URI with the app
        shareUri("geo:52.47254,13.4345")

        // Wait for the conversion to succeed
        assertConversionSucceeded(persistentListOf(WGS84Point(52.47254, 13.4345)))

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

    private fun goToUserPreferencesDetailAutomationScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.AUTOMATION}" }.click()
    }
}
