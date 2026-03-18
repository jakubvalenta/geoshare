package page.ooooo.geoshare

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import page.ooooo.geoshare.BehaviorTest.Companion.ELEMENT_DOES_NOT_EXIST_TIMEOUT
import page.ooooo.geoshare.ui.UserPreferencesGroupId

@RunWith(AndroidJUnit4::class)
class LinksBehaviorTest : BehaviorTest {

    @Test
    fun whenLinkIsInserted_allowsCopyingIt() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Go to link detail
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.LINKS}" }.click()

        // Insert link
        onElement { viewIdResourceName == "geoShareLinksListInsert" }.click()
        onElement { viewIdResourceName == "geoShareLinkFormName" }.setText("My New Maps")
        onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" }.setText("https://www.example.com/?ll={lat}%2C{lon}")
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSave" }
            .click()

        // Share a URI with the app
        shareUri("https://www.google.com/maps/@52.5067296,13.2599309,11z")

        // Tap copy link in the context menu
        onElement { viewIdResourceName == "geoShareMainPane" }
            // Scroll by percents, because it's more reliable than scrolling to the app icon
            .scroll(Direction.DOWN, 2f)
        onElement { viewIdResourceName == "geoShareResultSuccessAppLabel" && textAsString() == "My New Maps" }.longClick()
        onElement {
            viewIdResourceName == "geoShareAppContextMenuItem" &&
                textAsString() in setOf("Copy My New Maps link", "Copier le lien My New Maps")
        }.click()

        // Shows success message
        onElement { viewIdResourceName == "geoShareMainPane" }
            // Swipe instead of scrolling, because we need to check the message before it disappears
            .swipe(Direction.DOWN, 1f)
        onElement { viewIdResourceName == "geoShareResultSuccessMessage" }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Go to link list
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.LINKS}" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()

        // Update link
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation" }.apply {
            setText("$text edited")
        }
        onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" && textAsString() == "https://maps.apple.com/?daddr={lat}%2C{lon}" }
            .apply { setText("$text&edited=1") }
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_checked" }.click()
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_checked" }.click()
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_unchecked" }.click()
            quickWaitForStableInActiveWindow() // Wait before checking whether the checkbox has been really checked
            onElementOrNull(1_000L) { viewIdResourceName == "geoShareLinkFormChipEnabled_unchecked" }?.click() // Check the checkbox again, which is necessary on Xiaomi
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSave" }.click()
        }

        // Shows updated link
        onElement { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }
        onElement { textAsString() == "Apple Maps navigation edited" }

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()

        // Shows updated values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation edited" }
        onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" && textAsString() == "https://maps.apple.com/?daddr={lat}%2C{lon}&edited=1" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_checked" }
        }

        // Delete link
        onElement { viewIdResourceName == "geoShareLinksDetailDelete" }.click()
        onDialog("geoShareLinkDeleteDialog") {
            dismiss()
        }
        onElement { viewIdResourceName == "geoShareLinksDetailDelete" }.click()
        onDialog("geoShareLinkDeleteDialog") {
            confirm()
        }

        // Does not show link
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" })

        // Restore initial links
        onElement { viewIdResourceName == "geoShareLinksListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareLinksListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinksRestoreInitialButton" }
            .click()
        onDialog("geoShareLinksRestoreInitialDialog") {
            dismiss()
        }
        onElement { viewIdResourceName == "geoShareLinksRestoreInitialButton" }.click()
        onDialog("geoShareLinksRestoreInitialDialog") {
            confirm()
        }

        // Shows link
        onElement { viewIdResourceName == "geoShareLinksListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareLinksListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }
    }

    @Test
    fun allowsTogglingLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        closeIntro()

        // Go to links list
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.LINKS}" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinksListItem_b109970a-aef8-4482-9879-52e128fd0e07" }
            .click()

        // Shows pre-installed values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_checked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_unchecked" }
        }

        // Go back to list
        pressBack()

        // Toggle link
        onElement { viewIdResourceName == "geoShareLinksListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListItem_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Shows toggled values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_unchecked" }
        }

        // Go back to list
        pressBack()

        // Toggle link
        onElement { viewIdResourceName == "geoShareLinksListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinksListItem_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Shows toggled values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_checked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_unchecked" }
        }
    }
}
