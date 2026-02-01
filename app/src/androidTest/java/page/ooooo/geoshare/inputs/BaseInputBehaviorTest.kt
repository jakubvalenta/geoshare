package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.ElementNotFoundException
import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.BaseActivityBehaviorTest
import page.ooooo.geoshare.lib.point.Point

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

    protected fun testUri(
        expectedPoints: ImmutableList<Point>,
        unsafeUriString: String,
        fallbackPoints: ImmutableList<Point>? = null,
    ) =
        uiAutomator {
            // Go to main form
            goToMainForm()

            // Share URI and confirm permission dialog
            shareUri(unsafeUriString)
            confirmDialogIfItIsVisible()

            // Shows position
            try {
                waitAndAssertPositionIsVisible(expectedPoints)
            } catch (e: ElementNotFoundException) {
                if (fallbackPoints != null) {
                    waitAndAssertPositionIsVisible(fallbackPoints)
                } else {
                    throw e
                }
            }
        }

    protected fun testUri(expectedPoint: Point, unsafeUriString: String, fallbackPoint: Point? = null) =
        testUri(persistentListOf(expectedPoint), unsafeUriString, fallbackPoint?.let { persistentListOf(it) })

    protected fun testTextUri(expectedPoints: ImmutableList<Point>, unsafeText: String) = uiAutomator {
        // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
        // there are spaces in the texts, so we put the text in the main screen of the app instead.
        // device.executeShellCommand(
        //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n ${PackageNames.GEO_SHARE_DEBUG}/page.ooooo.geoshare.ConversionActivity ${PackageNames.GEO_SHARE_DEBUG}"
        // )

        // Go to main form
        goToMainForm()

        // Set main input
        onElement { viewIdResourceName == "geoShareMainInputUriStringTextField" }.setText(unsafeText)

        // Submit by pressing Enter, because the submit button can be covered by IME on a small screen
        pressEnter()

        // Confirm permission dialog
        confirmDialogIfItIsVisible()

        // Shows position
        waitAndAssertPositionIsVisible(expectedPoints)
    }

    protected fun testTextUri(expectedPoint: Point, unsafeText: String) =
        testTextUri(persistentListOf(expectedPoint), unsafeText)
}
