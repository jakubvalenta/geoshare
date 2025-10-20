package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class MainActivityBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun introScreen_whenAppIsOpenTwice_isVisibleOnlyFirstTime() = uiAutomator {
        // Launch app
        launchApplication()

        // Go to the second intro page
        onElement { viewIdResourceName == "geoShareIntroPage0HeadingText" }
        onElement { viewIdResourceName == "geoShareIntroScreenNextButton" }.click()

        // Relaunch app
        closeApplication()
        launchApplication()

        // Intro is still visible; go through all intro pages
        for (page in 0..1) {
            onElement { viewIdResourceName == "geoShareIntroPage${page}HeadingText" }
            onElement { viewIdResourceName == "geoShareIntroScreenNextButton" }.click()
        }

        // Main screen is visible
        onElement { viewIdResourceName == "geoShareMainInputUriStringTextField" }

        // Relaunch app
        closeApplication()
        launchApplication()

        // Main screen is visible again
        onElement { viewIdResourceName == "geoShareMainInputUriStringTextField" }
    }

    @Test
    fun urlConvertersScreen_whenOpenWithOldVersionCode_showsRecentInputsAndSavesNewVersionCode() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntroIfItIsVisible()

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the url converters screen
        goToUrlConvertersScreen()

        // Shows only url converter added since version 22
        onElement { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "Mapy.com" }
        waitForStableInActiveWindow()
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "HERE WeGo" })
        waitForStableInActiveWindow()
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "Google Maps" })

        // Go to main screen
        pressBack()

        // Does not show main menu badge
        waitForStableInActiveWindow()
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainMenuBadge" })

        // Go to the url converters screen
        goToUrlConvertersScreen()

        // Shows all url converters
        // Don't test Mapy.com, because it's behind the fold
        onElement { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "HERE WeGo" }
        onElement { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "Google Maps" }

        // Go to main screen
        pressBack()

        // Set user preference changelogShowForVersionCode to version 19
        goToUserPreferencesDetailDeveloperScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }.setText("19")

        // Go to user preferences screen
        pressBack()

        // Go to main screen
        pressBack()

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the url converters screen
        goToUrlConvertersScreen()

        // Shows only url converters added since version 19
        onElement { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "Mapy.com" }
        onElement { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "HERE WeGo" }
        waitForStableInActiveWindow()
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareUrlConvertersHeadline" && textAsString() == "Google Maps" })
    }
}
