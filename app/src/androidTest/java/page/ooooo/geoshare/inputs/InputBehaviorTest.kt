package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.UiAutomatorTestScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.BehaviorTest
import page.ooooo.geoshare.BehaviorTest.Companion.NETWORK_TIMEOUT
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.point.Point

interface InputBehaviorTest : BehaviorTest {

    fun UiAutomatorTestScope.setUserPreferenceConnectionPermissionToAlways() {
        goToUserPreferencesDetailConnectionPermissionScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermission_${Permission.ALWAYS}" }.click()
    }

    fun UiAutomatorTestScope.confirmDialogIfVisible() {
        onElementOrNull(3000L) { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }?.click()
    }

    fun UiAutomatorTestScope.goToMainForm() {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        onElementOrNull(1000L) { viewIdResourceName == "geoShareMainBackButton" }?.click()
    }

    fun UiAutomatorTestScope.goToMainFormAndShareUriAndConfirmDialog(unsafeUriString: String) {
        // Go to main form
        goToMainForm()

        // Share URI and confirm permission dialog
        shareUri(unsafeUriString)
        confirmDialogIfVisible()
    }

    fun UiAutomatorTestScope.testUri(
        expectedPoints: ImmutableList<Point>,
        unsafeUriString: String,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) {
        goToMainFormAndShareUriAndConfirmDialog(unsafeUriString)
        assertConversionSucceeded(expectedPoints, timeoutMs)
    }

    fun UiAutomatorTestScope.testUri(expectedPoint: Point, unsafeUriString: String, timeoutMs: Long = NETWORK_TIMEOUT) =
        testUri(persistentListOf(expectedPoint), unsafeUriString, timeoutMs)

    fun UiAutomatorTestScope.testTextUri(expectedPoints: ImmutableList<Point>, unsafeText: String) {
        // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
        // there are spaces in the texts, so we put the text in the main screen of the app instead.
        // device.executeShellCommand(
        //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n ${BuildConfig.APPLICATION_ID}.debug/${BuildConfig.APPLICATION_ID}.ConversionActivity ${BuildConfig.APPLICATION_ID}.debug"
        // )

        // Go to main form
        goToMainForm()

        // Set main input
        val mainInput = onElement { viewIdResourceName == "geoShareMainInputUriStringTextField" }
        mainInput.setText(unsafeText)

        // Submit main form
        if (mainInput.isFocused) {
            // If the field is focused, the submit button can be covered by IME, so submit by pressing Enter
            pressEnter()
        } else {
            // If the field is not focused, then pressing Enter doesn't submit, so submit by clicking the submit button
            onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()
        }

        // Confirm permission dialog
        confirmDialogIfVisible()

        // Shows points
        assertConversionSucceeded(expectedPoints)
    }

    fun UiAutomatorTestScope.testTextUri(expectedPoint: Point, unsafeText: String) =
        testTextUri(persistentListOf(expectedPoint), unsafeText)
}
