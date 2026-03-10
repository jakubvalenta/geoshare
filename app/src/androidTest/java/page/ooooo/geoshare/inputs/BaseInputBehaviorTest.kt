package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.point.Point

abstract class BaseInputBehaviorTest : BaseActivityBehaviorTest() {

    protected fun setUserPreferenceConnectionPermissionToAlways() = uiAutomator {
        goToUserPreferencesDetailConnectionPermissionScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermission_${Permission.ALWAYS}" }.click()
    }

    protected fun confirmDialogIfVisible() = uiAutomator {
        onElementOrNull(3000L) { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }?.click()
    }

    protected fun goToMainForm() = uiAutomator {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        onElementOrNull(1000L) { viewIdResourceName == "geoShareMainBackButton" }?.click()
    }

    private fun goToMainFormAndShareUriAndConfirmDialog(unsafeUriString: String) = uiAutomator {
        // Go to main form
        goToMainForm()

        // Share URI and confirm permission dialog
        shareUri(unsafeUriString)
        confirmDialogIfVisible()
    }

    protected fun testUri(
        expectedPoints: ImmutableList<Point>,
        unsafeUriString: String,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) = uiAutomator {
        goToMainFormAndShareUriAndConfirmDialog(unsafeUriString)
        assertConversionSucceeded(expectedPoints, timeoutMs)
    }

    protected fun testUri(expectedPoint: Point, unsafeUriString: String, timeoutMs: Long = NETWORK_TIMEOUT) =
        testUri(persistentListOf(expectedPoint), unsafeUriString, timeoutMs)

    protected fun testTextUri(expectedPoints: ImmutableList<Point>, unsafeText: String) = uiAutomator {
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

    protected fun testTextUri(expectedPoint: Point, unsafeText: String) =
        testTextUri(persistentListOf(expectedPoint), unsafeText)
}
