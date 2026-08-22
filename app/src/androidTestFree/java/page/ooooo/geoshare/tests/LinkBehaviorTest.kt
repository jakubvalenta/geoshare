package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class LinkBehaviorTest {
    @Test
    fun whenLinkIsInserted_allowsCopyingIt() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)

        // Insert link
        onElement { viewIdResourceName == "geoShareLinkListInsert" }.click()
        val link = Link(
            name = "My New Maps",
            coordsUriTemplate = "https://www.example.com/?ll={lat}%2C{lon}",
        )
        fillLinkForm(link)
        saveLinkForm()

        // Share a URI with the app
        shareUri()

        // Tap copy link in the context menu
        onMainScrollablePane()
            // Scroll by percents, because it's more reliable than scrolling to the app icon
            .scroll(Direction.DOWN, 2f)
        onElement { viewIdResourceName == "geoShareAppLabel" && textAsString() == "My New Maps" }.longClick()
        onElement {
            viewIdResourceName == "geoShareAppOutput" && textAsString() in setOf(
                "Copy My New Maps link",
                "Copier le lien My New Maps",
            )
        }.click()

        // Shows success message
        onMainScrollablePane()
            // Swipe instead of scrolling, because we need to check the message before it disappears
            .swipe(Direction.DOWN, 1f)
        onElement(pollIntervalMs = 50) { viewIdResourceName == "geoShareResultMessageSuccess" }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()

        // Update link
        val link = Link(
            name = onElement { viewIdResourceName == "geoShareLinkFormName" }.run { "$text edited" },
            coordsUriTemplate = onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" }.run { "$text&edited=1" },
            appEnabled = false,
            sheetEnabled = false,
            chipEnabled = true,
        )
        fillLinkForm(link)
        saveLinkForm()

        // Shows updated link
        onElement { viewIdResourceName == "geoShareLinkListItem_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }
        onElement { textAsString() == "Apple Maps navigation edited" }

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }.click()

        // Shows updated values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Apple Maps navigation edited" }
        onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" && textAsString() == "https://maps.apple.com/?daddr={lat}%2C{lon}&edited=1" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled" }
                .apply { assertTrue(isChecked) }
        }

        // Delete link
        onElement { viewIdResourceName == "geoShareLinkDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareLinkDeleteDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareLinkDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareLinkDeleteDialog" }.confirmDialog()

        // Does not show link
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareLinkListItem_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" })

        // Wait for the toast message to disappear, because it covers the restore button
        runBlocking {
            delay(TOAST_TIMEOUT)
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
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareLinkListItem_${InitialLinks.APPLE_MAPS_NAVIGATION_UUID}" }
    }

    @Test
    fun allowsTogglingLink() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to link list
        goToUserPreferencesDetail(UserPreferenceGroupId.LINKS)

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }
            .click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Shows pre-installed values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled" }
                .apply { assertTrue(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled" }
                .apply { assertFalse(isChecked) }
        }

        // Go back to list, use the save form button, because the device back button might just close the IME
        saveLinkForm()

        // Toggle link
        onElement { viewIdResourceName == "geoShareLinkListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Shows toggled values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled" }
                .apply { assertFalse(isChecked) }
        }

        // Go back to list, use the save form button, because the device back button might just close the IME
        saveLinkForm()

        // Toggle link
        onElement { viewIdResourceName == "geoShareLinkListItemToggle_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Go to link detail
        onElement { viewIdResourceName == "geoShareLinkListItemMenu_b109970a-aef8-4482-9879-52e128fd0e07" }.click()
        onElement { viewIdResourceName == "geoShareLinkListItemMenuDetail_b109970a-aef8-4482-9879-52e128fd0e07" }.click()

        // Shows toggled values
        onElement { viewIdResourceName == "geoShareLinkFormName" && textAsString() == "Magic Earth" }
        onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled" }
                .apply { assertTrue(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled" }
                .apply { assertFalse(isChecked) }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled" }
                .apply { assertFalse(isChecked) }
        }
    }
}

fun UiAutomatorTestScope.fillLinkForm(link: Link) {
    onElement { viewIdResourceName == "geoShareLinkFormName" }.setText(link.name)
    onElement { viewIdResourceName == "geoShareLinkFormCoordsUriTemplate" }.setText(link.coordsUriTemplate)
    onElement { viewIdResourceName == "geoShareLinkDetailPane" }.apply {
        scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormAppEnabled" }
            .apply { if (isChecked != link.appEnabled) click() }
        scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSheetEnabled" }
            .apply { if (isChecked != link.sheetEnabled) click() }
        scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormChipEnabled" }
            .apply { if (isChecked != link.chipEnabled) click() }
    }
}

fun UiAutomatorTestScope.saveLinkForm() {
    onElement { viewIdResourceName == "geoShareLinkDetailPane" }
        .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareLinkFormSave" }
        .click()
}
