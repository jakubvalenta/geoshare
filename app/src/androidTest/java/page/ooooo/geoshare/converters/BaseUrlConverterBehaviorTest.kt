package page.ooooo.geoshare.converters

import androidx.test.uiautomator.uiAutomator
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.Position

abstract class BaseUrlConverterBehaviorTest : BaseActivityBehaviorTest() {

    protected fun clickIntroCloseButton() = uiAutomator {
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }

    protected fun setUserPreferenceConnectionPermissionToAlways() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermissionAlways" }.click()
    }

    protected fun testUri(expectedPosition: Position, unsafeUriString: String) = uiAutomator {
        // Make sure we leave the result screen, if we're there, so that we don't accidentally test the old result.
        pressBack()

        shareUri(unsafeUriString)
        waitAndAssertPositionIsVisible(expectedPosition)
    }

    protected fun testTextUri(expectedPosition: Position, unsafeTextWithoutSpaces: String) {
        executeShellCommand(
            "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeTextWithoutSpaces -n $PACKAGE_NAME/page.ooooo.geoshare.ConversionActivity $PACKAGE_NAME"
        )
        waitAndAssertPositionIsVisible(expectedPosition)
    }
}
