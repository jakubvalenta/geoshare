package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.BehaviorTest.Companion.ELEMENT_DOES_NOT_EXIST_TIMEOUT
import page.ooooo.geoshare.lib.inputs.InputDocumentationGroup
import page.ooooo.geoshare.ui.UserPreferencesGroupId

@RunWith(AndroidJUnit4::class)
class InputsBehaviorTest : BehaviorTest {

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
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }

        // Relaunch app
        closeApplication()
        launchApplication()

        // Main screen is visible again
        onElement { viewIdResourceName == "geoShareMainSourceTextField" }
    }

    @Test
    fun whenOpenWithOldVersionCode_showsRecentInputsAndSavesNewVersionCode() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Does not show main menu badge
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareMainMenuBadge" })

        // Go to the inputs screen
        goToInputsList()

        // Does not show recently added documentations
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.MAPY_COM}" })

        // Shows all documentations
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.AMAP}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationAll_${InputDocumentationGroup.APPLE_MAPS}" }

        // Go to main screen
        pressBack()

        // Set user preference changelogShowForVersionCode to version 19
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.DEVELOPER_OPTIONS)
        onElement { viewIdResourceName == "geoShareUserPreferenceChangelogShownForVersionCode" }
            .setText("19")
        goToMainScreenFromUserPreferencesDetail()

        // Shows main menu badge
        onElement { viewIdResourceName == "geoShareMainMenuBadge" }

        // Go to the inputs screen
        goToInputsList()

        // Shows documentations added since version 19
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.HERE_WEGO}" }
        onElement { viewIdResourceName == "geoShareInputsDocumentationRecent_${InputDocumentationGroup.MAGIC_EARTH}" }
    }
}
