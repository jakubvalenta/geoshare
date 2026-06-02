package page.ooooo.geoshare

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

class IntroBehaviorTest {
    @Test
    fun whenAppIsOpenTwice_isVisibleOnlyFirstTime() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Go to the second intro page
        onElement { viewIdResourceName == "geoShareIntroPage0HeadingText" }
        onElement { viewIdResourceName == "geoShareIntroScreenNextButton" }.click()

        // Relaunch app
        closeApplication()
        launchApplication()
        waitForAppToBeVisible()

        // Intro is still visible; go through all intro pages
        for (page in 0..1) {
            onElement { viewIdResourceName == "geoShareIntroPage${page}HeadingText" }
            onElement { viewIdResourceName == "geoShareIntroScreenNextButton" }.click()
        }

        // Main screen is visible
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }

        // Relaunch app
        closeApplication()
        launchApplication()
        waitForAppToBeVisible()

        // Main screen is visible again
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }
    }
}
