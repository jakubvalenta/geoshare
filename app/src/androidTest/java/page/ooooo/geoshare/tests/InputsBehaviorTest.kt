package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.inputs.InputGroupId
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class InputsBehaviorTest {
    @Test
    fun whenAppIsOpenTwice_isVisibleOnlyFirstTime() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Go to the second intro page
        onElement { viewIdResourceName == "geoShareIntroPage0Headline" }
        onElement { viewIdResourceName == "geoShareIntroNextButton" }.click()

        // Relaunch app
        closeApplication()
        launchApplication()
        waitForAppToBeVisible()

        // Intro is still visible; go through all intro pages
        for (page in 0..1) {
            onElement { viewIdResourceName == "geoShareIntroPage${page}Headline" }
            onElement { viewIdResourceName == "geoShareIntroNextButton" }.click()
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

    @Test
    fun whenOpenWithOldVersionCode_showsRecentInputsAndSavesNewVersionCode() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Does not show main menu badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainMenuBadge" })

        // Go to the inputs screen
        goToInputList()

        // Does not show recently added input groups
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareInputListRecent_${InputGroupId.MAPY_COM}" })

        // Shows all input groups
        onElement { viewIdResourceName == "geoShareInputListAll_${InputGroupId.AMAP}" }
        onElement { viewIdResourceName == "geoShareInputListAll_${InputGroupId.APPLE_MAPS}" }

        // Go to main screen
        goBackToMainForm()

        // Set user preference changelogShowForVersionCode to version 19
        goToUserPreferencesDetail(UserPreferenceGroupId.DEVELOPER_OPTIONS)
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }
            .setText("19")
        goBackToMainForm()

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the inputs screen
        goToInputList()

        // Shows input groups added since version 19
        onElement { viewIdResourceName == "geoShareInputListPane" }.apply {
            scrollToElement(Direction.DOWN) {
                viewIdResourceName == "geoShareInputListRecent_${InputGroupId.HERE_WEGO}"
            }
            scrollToElement(Direction.DOWN) {
                viewIdResourceName == "geoShareInputListRecent_${InputGroupId.MAGIC_EARTH}"
            }
        }
    }
}
