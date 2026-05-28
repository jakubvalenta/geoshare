package page.ooooo.geoshare

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.ui.UserPreferencesGroupId

class ServerBehaviorTest : BehaviorTest {
    @Test
    fun allowsInsertingServer() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to server list
        goToUserPreferencesList()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.SERVER}" }.click()

        // Insert server
        onElement { viewIdResourceName == "geoShareServerListInsert" }.click()
        onElement { viewIdResourceName == "geoShareServerFormBaseUrl" }.setText("https://api.example.com")
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareServerFormAuthType_${ServerAuthType.API_KEY}" }.click()
        onElement { viewIdResourceName == "geoShareDropdownFieldMenuItem_${ServerAuthType.ATTESTATION}" }.click()
        onElement { viewIdResourceName == "geoShareServerDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormSave" }.click()
        }

        // Shows inserted server
        onElement { viewIdResourceName == "geoShareServerListItemContent" && textAsString() == "api.example.com" }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringServer() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to server list
        goToUserPreferencesList()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.SERVER}" }.click()

        // Go to server detail
        onElement { viewIdResourceName == "geoShareServerListItemMenu_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()

        // Update server
        onElement { viewIdResourceName == "geoShareServerFormBaseUrl" }.apply { setText("$text/edited") }
        quickWaitForStableInActiveWindow() // Wait for IME to appear
        pressBack() // Hide IME
        onElement { viewIdResourceName == "geoShareServerDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKeyHeader" }.setText("X-My-Header")
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" }.setText("my_api_key")
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormSave" }.click()
        }

        // Shows updated server
        onElement { viewIdResourceName == "geoShareServerListItemContent" && textAsString() == "geocode.googleapis.com/edited" }.click()

        // Go to server detail
        onElement { viewIdResourceName == "geoShareServerListItemMenu_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()

        // Shows updated values
        onElement { viewIdResourceName == "geoShareServerFormBaseUrl" && textAsString() == "https://geocode.googleapis.com/edited" }
        onElement { viewIdResourceName == "geoShareServerDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKeyHeader" && textAsString() == "X-My-Header" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" && textAsString() == "my_api_key" }
        }

        // Delete server
        onElement { viewIdResourceName == "geoShareServerDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareServerDeleteDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareServerDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareServerDeleteDialog" }.confirmDialog()

        // Does not show server
        assertNull(onElementOrNull(BehaviorTest.ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" })

        // Wait for the delete toast to disappear, because it covers the restore button
        runBlocking {
            delay(3_000L)
        }

        // Restore initial servers
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareServerListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the bottom
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerRestoreInitialButton" }
            .click()
        onElement { viewIdResourceName == "geoShareServerRestoreInitialDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareServerRestoreInitialButton" }.click()
        onElement { viewIdResourceName == "geoShareServerRestoreInitialDialog" }.confirmDialog()

        // Shows server
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }
    }

    @Test
    fun allowsSelectingServerAfterMakingItValid() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to server list
        goToUserPreferencesList()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.SERVER}" }.click()

        // Shows no server selected
        onElement { viewIdResourceName == "geoShareServerListItemRadio_null_selected_true" }
        onElement { viewIdResourceName == "geoShareServerListItemRadio_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8_selected_false" }

        // Go to invalid server detail
        onElement { viewIdResourceName == "geoShareServerListItemMenu_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()

        // Update invalid server
        onElement { viewIdResourceName == "geoShareServerDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" }.setText("my_api_key")
            quickWaitForStableInActiveWindow() // Wait for IME to appear
            pressBack() // Hide IME
            onElement { viewIdResourceName == "geoShareServerFormSave" }.click()
        }

        // Select updated server
        onElement { viewIdResourceName == "geoShareServerListItemRadio_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8_selected_false" }.click()

        // Shows server selected
        onElement { viewIdResourceName == "geoShareServerListItemRadio_null_selected_false" }
        onElement { viewIdResourceName == "geoShareServerListItemRadio_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8_selected_true" }

        // Select no server again
        onElement { viewIdResourceName == "geoShareServerListItemRadio_null_selected_false" }.click()

        // Shows no server selected
        onElement { viewIdResourceName == "geoShareServerListItemRadio_null_selected_true" }
        onElement { viewIdResourceName == "geoShareServerListItemRadio_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8_selected_false" }
    }
}
