package page.ooooo.geoshare.converters

import androidx.test.uiautomator.uiAutomator
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

abstract class BaseUrlConverterBehaviorTest : BaseActivityBehaviorTest() {

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

    protected fun goBackFromConversionScreenIfItIsVisible() = uiAutomator {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        onElementOrNull(1000L) { viewIdResourceName == "geoShareConversionBackButton" }?.click()
    }

    protected fun testUri(expectedPosition: Position, unsafeUriString: String) = uiAutomator {
        // Go back from conversion screen
        goBackFromConversionScreenIfItIsVisible()

        // Share URI and confirm permission dialog
        shareUri(unsafeUriString)
        confirmDialogIfItIsVisible()

        // Shows position
        waitAndAssertPositionIsVisible(expectedPosition)
    }

    protected fun testTextUri(expectedPosition: Position, unsafeText: String) = uiAutomator {
        // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
        // there are spaces in the texts, so we put the text in the main screen of the app instead.
        // device.executeShellCommand(
        //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n $PACKAGE_NAME/page.ooooo.geoshare.ConversionActivity $PACKAGE_NAME"
        // )

        // Go back from conversion screen
        goBackFromConversionScreenIfItIsVisible()

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
