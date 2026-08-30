package page.ooooo.geoshare.tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class MainBehaviorTest {
    @Test
    fun whenTextIsEntered_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Enter text in the main form and submit it
        testText(
            WGS84Point(45.4786785, 9.2473799, source = Source.URI),
            "geo:45.4786785,9.2473799",
        )

        // Tap the Google Maps icon
        clickAppIcon(PackageNames.GOOGLE_MAPS)

        // Wait for Google Maps
        onElement(20_000) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows result screen, because the app didn't finish
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }

    @Test
    fun showsHelpMessagesAndAllowsDismissingThem() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Help message WELCOME is visible
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}" }

        // Dismiss help message WELCOME
        dismissHelpMessage()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.WELCOME}"
            }
        )

        // Share a URI with the app
        shareUri()

        // Help message OPEN_BY_DEFAULT is visible
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}" }

        // Help message SHARE_SOURCE is not visible
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )

        // Dismiss help message OPEN_BY_DEFAULT
        dismissHelpMessage()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Enter text in the main form and submit it
        goBackToMainForm()
        setMainInput()
        submitMainForm()

        // Help message SHARE_SOURCE is visible
        onElement { viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}" }

        // Help message OPEN_BY_DEFAULT is not visible
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.OPEN_BY_DEFAULT}"
            }
        )

        // Dismiss help message SHARE_SOURCE
        dismissHelpMessage()
        quickWaitForStableInActiveWindow() // Wait for help message exit animation
        assertNull(
            onElementOrNull(1_000) {
                viewIdResourceName == "geoShareHelpMessage_${HelpMessage.SHARE_SOURCE}"
            }
        )
    }
}
