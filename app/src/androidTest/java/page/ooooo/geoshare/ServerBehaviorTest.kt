package page.ooooo.geoshare

import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.ui.UserPreferenceGroupId

class ServerBehaviorTest {
    @Test
    fun allowsInsertingServer() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to server preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

        // Go to server list
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceNavigateToServerList" }.click()
        }

        // Insert a new server
        val server = Server(
            name = "Test Server",
            description = "Test description",
            urlTemplate = "https://api.example.com/{q}",
            authType = ServerAuthType.ATTESTATION,
            challengeUrl = "https://api.example.com/auth/challenge",
            loginUrl = "https://api.example.com/auth/login",
            registerUrl = "https://api.example.com/auth/register",
        )
        onElement { viewIdResourceName == "geoShareServerListInsert" }.click()
        fillAndSaveServerForm(server)

        // Shows the new server
        onElement { viewIdResourceName == "geoShareSegmentedListItemContent" && textAsString() == server.name }
    }

    @Test
    fun allowsUpdatingAndDeletingAndRestoringServer() = uiAutomator {
        // Launch application and close intro
        launchApplication()
        waitForAppToBeVisible()
        closeIntro()

        // Go to server preferences
        goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

        // Go to server list
        onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }.apply {
            scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferenceNavigateToServerList" }.click()
        }

        // Go to server detail
        onElement { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()

        // Update the server
        val server = Server(
            name = onElement { viewIdResourceName == "geoShareServerFormName" }.run { "$text edited" },
            urlTemplate = onElement { viewIdResourceName == "geoShareServerFormUrlTemplate" }.run { "$text/edited" },
            apiKey = "my_api_key",
            apiKeyHeader = "X-My-Header",
        )
        fillAndSaveServerForm(server)

        // Shows the updated server
        onElement { viewIdResourceName == "geoShareSegmentedListItemContent" && textAsString() == "Google Maps edited" }

        // Go to the server detail again
        onElement { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }.click()

        // Shows the updated values
        onElement { viewIdResourceName == "geoShareServerFormName" && textAsString() == "Google Maps edited" }
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
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" })

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

        // Shows the restored server
        onElement { viewIdResourceName == "geoShareServerListPane" }
            .scrollToElement(Direction.UP) { viewIdResourceName == "geoShareServerListItem_16b3bb06-3a3b-4853-ac06-c4bf1eb346f8" }
    }
}

fun UiAutomatorTestScope.fillAndSaveServerForm(server: Server) {
    server.name.takeIf { it.isNotEmpty() }?.let {
        onElement { viewIdResourceName == "geoShareServerFormName" }.setText(it)
    }
    server.description.takeIf { it.isNotEmpty() }?.let {
        onElement { viewIdResourceName == "geoShareServerFormDescription" }.setText(it)
    }
    server.urlTemplate.takeIf { it.isNotEmpty() }?.let {
        onElement { viewIdResourceName == "geoShareServerFormUrlTemplate" }.setText(it)
    }
    quickWaitForStableInActiveWindow() // Wait for IME to appear
    pressBack() // Hide IME
    onElement { viewIdResourceName == "geoShareServerDetailPane" }.let { pane ->
        when (server.authType) {
            ServerAuthType.API_KEY -> {
                server.apiKeyHeader.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKeyHeader" }
                        .setText(it)
                }
                server.apiKey.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" }
                        .setText(it)
                }
            }

            ServerAuthType.ATTESTATION -> {
                onElement { viewIdResourceName == "geoShareServerFormAuthType_${ServerAuthType.API_KEY}" }.click()
                onElement { viewIdResourceName == "geoShareDropdownFieldMenuItem_${ServerAuthType.ATTESTATION}" }.click()
                server.challengeUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormChallengeUrl" }
                        .setText(it)
                }
                server.loginUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormLoginUrl" }
                        .setText(it)
                }
                server.registerUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormRegisterUrl" }
                        .setText(it)
                }
            }
        }
        pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormSave" }.click()
    }
}
