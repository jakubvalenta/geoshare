package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.inputs.InputBehaviorTest
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

@RunWith(AndroidJUnit4::class)
class MainBehaviorTest : InputBehaviorTest {
    @Test
    fun whenTextIsEntered_showsPointAndAllowsOpeningGoogleMaps() = uiAutomator {
        assumeAppInstalled(PackageNames.GOOGLE_MAPS)

        // Launch app and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Enter text in the main form and submit it
        testText(
            WGS84Point(45.4786785, 9.2473799, source = Source.URI),
            "geo:45.4786785,9.2473799",
        )

        // Tap the Google Maps icon
        onElement { viewIdResourceName == "geoShareApp_${PackageNames.GOOGLE_MAPS}" }.click()

        // Wait for Google Maps
        onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS }

        // Go back to app
        launchApplication()
        waitForAppToBeVisible()

        // Shows result screen, because the app didn't finish
        onElement { viewIdResourceName == "geoShareResultAutomationButton" }
    }
}
