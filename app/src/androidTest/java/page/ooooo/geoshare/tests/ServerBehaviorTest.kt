package page.ooooo.geoshare.tests

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.local.database.InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class ServerBehaviorTest {
    @Test
    fun allowsInsertingServer() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Go to server list
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

        // Insert a new server
        val server = Server(
            name = "Test Server",
            urlTemplate = "https://api.example.com/{q}",
            authType = ServerAuthType.ATTESTATION,
            challengeUrl = "https://api.example.com/auth/challenge",
            loginUrl = "https://api.example.com/auth/login",
            registerUrl = "https://api.example.com/auth/register",
        )
        onElement { viewIdResourceName == "geoShareServerListInsert" }.click()
        fillServerForm(server)
        saveServerForm()

        // Wait for the toast message to disappear
        runBlocking {
            delay(TOAST_TIMEOUT)
        }

        // Shows the new server
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsAddress_${server.name}" }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringServer() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Go to server list
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

        // Go to server detail
        onElement { viewIdResourceName == "geoShareServerListItemMenu_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" }.click()

        // Update the server
        val server = Server(
            name = onElement { viewIdResourceName == "geoShareServerFormName" }.run { "$text edited" },
            urlTemplate = onElement { viewIdResourceName == "geoShareServerFormUrlTemplate" }.run { "$text/edited" },
            apiKey = "my_api_key",
            apiKeyHeader = "X-My-Header",
        )
        fillServerForm(server)
        saveServerForm()

        // Wait for the toast message to disappear
        runBlocking {
            delay(TOAST_TIMEOUT)
        }

        // Shows the updated server
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsAddress_Google Maps Geocode Address edited" }

        // Go to the server detail again
        onElement { viewIdResourceName == "geoShareServerListItemMenu_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" }.click()
        onElement { viewIdResourceName == "geoShareServerListItemMenuDetail_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" }.click()

        // Shows the updated values
        onElement { viewIdResourceName == "geoShareServerFormName" && textAsString() == "Google Maps Geocode Address edited" }
        onElement { viewIdResourceName == "geoShareServerFormUrlTemplate" && textAsString() == "https://geocode.googleapis.com/v4/geocode/address/{q}/edited" }
        onElement { viewIdResourceName == "geoShareServerDetailPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKeyHeader" && textAsString() == "X-My-Header" }
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" && textAsString() == "my_api_key" }
        }

        // Delete the server
        onElement { viewIdResourceName == "geoShareServerDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareServerDeleteDialog" }.dismissDialog()
        onElement { viewIdResourceName == "geoShareServerDetailDelete" }.click()
        onElement { viewIdResourceName == "geoShareServerDeleteDialog" }.confirmDialog()

        // Does not show the server anymore
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareServerListItemMenu_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" })

        // Wait for the toast message to disappear, because it covers the restore button
        runBlocking {
            delay(TOAST_TIMEOUT)
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

        // Shows the restored server
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scroll(Direction.DOWN, 10f)
        onElement { viewIdResourceName == "geoShareServerListPane" }
            // Scroll again, because only now can the lazy column pane scroll all the way to the top
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareServerListItemMenu_$GOOGLE_MAPS_GEOCODE_ADDRESS_UUID" }
    }
}
