package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.UiAutomatorTestScope
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.BehaviorTest
import page.ooooo.geoshare.BehaviorTest.Companion.NETWORK_TIMEOUT
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.ui.UserPreferencesGroupId

interface InputBehaviorTest : BehaviorTest {

    fun UiAutomatorTestScope.setUserPreferenceConnectionPermissionToAlways() {
        goToUserPreferencesList()
        goToUserPreferencesDetail(UserPreferencesGroupId.CONNECTION_PERMISSION)
        onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermission_${Permission.ALWAYS}" }.click()
    }

    fun UiAutomatorTestScope.confirmDialogIfVisible() {
        onElementOrNull(3_000L) { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }?.click()
    }

    fun UiAutomatorTestScope.goToMainForm() {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        onElementOrNull(1_000L) { viewIdResourceName == "geoShareMainBackButton" }?.click()
    }

    fun UiAutomatorTestScope.testUri(
        expectedPoints: Points,
        unsafeUriString: String,
        fallbackPoints: Points? = null,
        accurate: Boolean? = null,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) {
        // Go to main form
        goToMainForm()

        // Share URI and confirm permission dialog
        shareUri(unsafeUriString)
        confirmDialogIfVisible()

        try {
            assertConversionSucceeded(expectedPoints, accurate, timeoutMs)
        } catch (e: AssertionError) {
            if (fallbackPoints != null) {
                assertConversionSucceeded(fallbackPoints, accurate, timeoutMs)
            } else {
                throw e
            }
        }
    }

    fun UiAutomatorTestScope.testUri(
        expectedPoint: Point,
        unsafeUriString: String,
        fallbackPoint: Point? = null,
        accurate: Boolean? = null,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) =
        testUri(
            persistentListOf(expectedPoint),
            unsafeUriString,
            fallbackPoint?.let { persistentListOf(it) },
            accurate,
            timeoutMs,
        )

    fun UiAutomatorTestScope.testText(expectedPoints: Points, unsafeText: String) {
        // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
        // there are spaces in the text. So instead, we type the text in the main form of the app.
        // device.executeShellCommand(
        //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n ${BuildConfig.APPLICATION_ID}.debug/${BuildConfig.APPLICATION_ID}.ConversionActivity ${BuildConfig.APPLICATION_ID}.debug"
        // )

        // Go to main form
        goToMainForm()

        // Set main input
        val mainInput = onElement { viewIdResourceName == "geoShareMainSourceTextField" }
        mainInput.setText(unsafeText)
        quickWaitForStableInActiveWindow() // Wait for the submit button to get its final position, after setting text

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

    fun UiAutomatorTestScope.testText(expectedPoint: Point, unsafeText: String) =
        testText(persistentListOf(expectedPoint), unsafeText)
}
