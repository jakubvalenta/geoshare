package page.ooooo.geoshare

import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.inputs.InputDocumentationGroup
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class InputsBehaviorTest {
    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

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

        // Does not show recently added documentations
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.MAPY_COM}" })

        // Shows all documentations
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.AMAP}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.APPLE_MAPS}" }

        // Go to main screen
        pressBack()

        // Set user preference changelogShowForVersionCode to version 19
        goToUserPreferencesDetail(UserPreferenceGroupId.DEVELOPER_OPTIONS)
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }
            .setText("19")
        goToMainScreenFromUserPreferencesDetail()

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the inputs screen
        goToInputList()

        // Shows documentations added since version 19
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.HERE_WEGO}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.MAGIC_EARTH}" }
    }
}
