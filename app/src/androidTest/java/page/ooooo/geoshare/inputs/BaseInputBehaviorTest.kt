package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.position.Position

abstract class BaseInputBehaviorTest : BaseActivityBehaviorTest() {

    protected fun setUserPreferenceConnectionPermissionToAlways() = uiAutomator {
        goToUserPreferencesDetailConnectionPermissionScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermissionAlways" }.click()

        // Go back to main screen
        pressBack()
        pressBack()
    }

    protected fun confirmDialogIfItIsVisible() = uiAutomator {
        onElementOrNull(3000L) { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }?.click()
    }

    protected fun goToMainForm() = uiAutomator {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        onElementOrNull(1000L) { viewIdResourceName == "geoShareMainBackButton" }?.click()
    }

    protected fun testUri(expectedPosition: Position, unsafeUriString: String, fallbackPosition: Position? = null) =
        uiAutomator {
            // Go to main form
            goToMainForm()

            // Share URI and confirm permission dialog
            shareUri(unsafeUriString)
            confirmDialogIfItIsVisible()

            // Shows position
            try {
                waitAndAssertPositionIsVisible(expectedPosition)
            } catch (e: AssertionError) {
                if (fallbackPosition != null) {
                    waitAndAssertPositionIsVisible(fallbackPosition)
                } else {
                    throw e
                }
            }
        }

    protected fun testTextUri(expectedPosition: Position, unsafeText: String) = uiAutomator {
        // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
        // there are spaces in the texts, so we put the text in the main screen of the app instead.
        // device.executeShellCommand(
        //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n ${PackageNames.GEO_SHARE_DEBUG}/page.ooooo.geoshare.ConversionActivity ${PackageNames.GEO_SHARE_DEBUG}"
        // )

        // Go to main form
        goToMainForm()

        // Set main input
        onElement { viewIdResourceName == "geoShareMainInputUriStringTextField" }.setText(unsafeText)
        waitForStableInActiveWindow()

        // Submit and confirm permission dialog
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()
        confirmDialogIfItIsVisible()

        // Shows position
        waitAndAssertPositionIsVisible(expectedPosition)
    }
}
