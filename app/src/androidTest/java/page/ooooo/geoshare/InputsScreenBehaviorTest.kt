package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.lib.inputs.InputDocumentationId
import page.ooooo.geoshare.ui.UserPreferencesGroupId

@RunWith(AndroidJUnit4::class)
class InputsScreenBehaviorTest : BaseActivityBehaviorTest() {

    @Test
    fun whenAppIsOpenTwice_isVisibleOnlyFirstTime() = uiAutomator {
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
    fun whenOpenWithOldVersionCode_showsRecentInputsAndSavesNewVersionCode() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Does not show main menu badge
        Assert.assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainMenuBadge" })

        // Go to the inputs screen
        goToInputsScreen()

        // Does not show recently added documentations
        Assert.assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationId.MAPY_COM}" })

        // Shows all documentations
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationId.AMAP}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationId.APPLE_MAPS}" }

        // Go to main screen
        pressBack()

        // Set user preference changelogShowForVersionCode to version 19
        goToUserPreferencesDetailDeveloperScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }
            .setText("19")

        // Go to main screen
        onElement { viewIdResourceName == "geoShareBack" }.click()
        if (onElementOrNull(1_000L) { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.DEVELOPER_OPTIONS}" } != null) {
            // On a non-tablet screen, we need to tap the back button one more time to get from the user preferences
            // list screen to the main screen
            onElement { viewIdResourceName == "geoShareBack" }.click()
        }

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the inputs screen
        goToInputsScreen()

        // Shows documentations added since version 19
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationId.HERE_WEGO}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationId.MAGIC_EARTH}" }
    }
}
