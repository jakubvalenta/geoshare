package page.ooooo.geoshare

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.ui.UserPreferencesGroupId

class LinkBehaviorTest {
    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

    @Test
    fun whenLinkIsInserted_allowsCopyingIt() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferencesGroupId.LINKS)

        // Insert link
        onElement { viewIdResourceName == "geoShareLinkListInsert" }.click()
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
        onMainScrollablePane()
            // Scroll by percents, because it's more reliable than scrolling to the app icon
            .scroll(Direction.DOWN, 2f)
        onElement { viewIdResourceName == "geoShareAppLabel" && textAsString() == "My New Maps" }.longClick()
        onElement {
            viewIdResourceName == "geoShareAppOutput" &&
                textAsString() in setOf("Copy My New Maps link", "Copier le lien My New Maps")
        }.click()

        // Shows success message
        onMainScrollablePane()
            // Swipe instead of scrolling, because we need to check the message before it disappears
            .swipe(Direction.DOWN, 1f)
        onElement { viewIdResourceName == "geoShareResultSuccessMessage" }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferencesGroupId.LINKS)

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()

        // Update link
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation" }
            .apply { setText("$text edited") }
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
        onElement { viewIdResourceName == "geoShareLinkListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }
        onElement { textAsString() == "Apple Maps navigation edited" }

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_a5092c63-cf5c-4225-9059-e888ae12e215" }.click()

        // Shows updated values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation edited" }
        onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" && textAsString() == "https://maps.apple.com/?daddr={lat}%2C{lon}&edited=1" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled_unchecked" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled_checked" }
        }

        // Delete link
        onElement { viewIdResourceName == "geoShareLinkDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareLinkDeleteDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareLinkDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareLinkDeleteDialog" }.confirmDialog()

        // Does not show link
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareLinkListItem_a5092c63-cf5c-4225-9059-e888ae12e215" })

        // Wait for the delete toast to disappear, because it covers the restore button
        runBlocking {
            delay(3_000L)
        }

        // Restore initial links
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkRestoreInitialButton" }
            .click()
        onElement { viewIdResourceName == "geoShareLinkRestoreInitialDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareLinkRestoreInitialButton" }.click()
        onElement { viewIdResourceName == "geoShareLinkRestoreInitialDialog" }.confirmDialog()

        // Shows link
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareLinkListItem_a5092c63-cf5c-4225-9059-e888ae12e215" }
    }

    @Test
    fun allowsTogglingLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferencesGroupId.LINKS)

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }
            .click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

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
        onElement { viewIdResourceName == "geoShareLinkListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

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
        onElement { viewIdResourceName == "geoShareLinkListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

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
