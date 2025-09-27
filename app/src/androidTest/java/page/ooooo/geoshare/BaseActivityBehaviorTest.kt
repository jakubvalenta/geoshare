package page.ooooo.geoshare

import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import page.ooooo.geoshare.lib.Position
import java.lang.Thread.sleep
import java.util.regex.Pattern

abstract class BaseActivityBehaviorTest {
    protected lateinit var device: UiDevice

    protected val packageName = "page.ooooo.geoshare.debug"
    protected val googleMapsPackageName = "com.google.android.apps.maps"
    protected val launchTimeout = 10_000L
    protected val timeout = 10_000L

    @Before
    fun goToLauncher() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()
    }

    protected fun launchApplication() {
        // Use shell command instead of startActivity() to support Xiaomi.
        executeShellCommand("monkey -p $packageName 1")

        // Wait for the app to appear
        waitForApplication()
    }

    protected fun waitForApplication() {
        waitForObject(By.pkg(packageName).depth(0), launchTimeout)
    }

    protected fun closeApplication() {
        device.pressRecentApps()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // On newer Android, swipe up to close the most recent app
            sleep(1000) // Crude way to make sure recent apps finished loading
            device.swipe(
                device.displayWidth / 2, device.displayHeight / 2, device.displayWidth / 2, 0, 5
            )
        } else {
            // On older Android, swipe right to close the most recent app
            var retries = 5
            while (--retries >= 0) {
                // Retry swiping several times, because it sometimes fails
                device.swipe(
                    (device.displayWidth * 0.1).toInt(),
                    (device.displayHeight * 0.8).toInt(),
                    (device.displayWidth * 0.9).toInt(),
                    (device.displayHeight * 0.8).toInt(),
                    10
                )
                val success = device.wait(
                    Until.gone(By.text("clear all".toPattern(Pattern.CASE_INSENSITIVE))), 3000L
                )
                if (success) {
                    break
                }
            }
        }
    }

    protected fun executeShellCommand(cmd: String) {
        val output = device.executeShellCommand(cmd)
        Log.i(null, "Executed shell command `$cmd`: $output")
    }

    protected fun assertObjectDoesNotExist(selector: BySelector) {
        assertNull(device.findObject(selector))
    }

    protected fun waitForObject(
        selector: BySelector,
        timeout: Long = this.timeout,
    ): Boolean = device.wait(Until.hasObject(selector), timeout)

    protected fun waitAndAssertObjectExists(selector: BySelector) {
        assertTrue(waitForObject(selector))
    }

    protected fun waitAndAssertPositionIsVisible(expectedPosition: Position) {
        waitAndAssertObjectExists(By.text(expectedPosition.toNorthSouthWestEastDecCoordsString()))
        val paramsBox = By.res("geoShareConversionSuccessPositionParams")
        if (!expectedPosition.q.isNullOrEmpty() || !expectedPosition.z.isNullOrEmpty()) {
            waitAndAssertObjectExists(paramsBox.text(expectedPosition.toParamsString()))
        } else {
            assertObjectDoesNotExist(paramsBox)
        }
    }

    protected fun clickObject(selector: BySelector) {
        device.findObject(selector)?.click()
    }

    protected fun shareUri(unsafeUriString: String) {
        // Use shell command instead of startActivity() to support Xiaomi
        executeShellCommand(
            "am start -a android.intent.action.VIEW -d $unsafeUriString -n $packageName/page.ooooo.geoshare.ConversionActivity $packageName"
        )
    }

    protected fun testUri(expectedPosition: Position, unsafeUriString: String) {
        shareUri(unsafeUriString)
        waitAndAssertPositionIsVisible(expectedPosition)
    }

    protected fun testTextUri(expectedPosition: Position, unsafeTextWithoutSpaces: String) {
        executeShellCommand(
            "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeTextWithoutSpaces -n $packageName/page.ooooo.geoshare.ConversionActivity $packageName"
        )
        waitAndAssertPositionIsVisible(expectedPosition)
    }

    protected fun waitAndConfirmDialogAndAssertNewWindowIsOpen(
        selector: BySelector,
        doNotAsk: Boolean = false,
    ) {
        waitAndAssertObjectExists(selector)
        toggleDialogDoNotAsk(doNotAsk)
        device.findObject(By.res("geoShareConfirmationDialogConfirmButton"))?.clickAndWait(Until.newWindow(), timeout)
    }

    protected fun waitAndDismissDialogAndAssertItIsClosed(
        selector: BySelector,
        doNotAsk: Boolean = false,
    ) {
        waitAndAssertObjectExists(selector)
        toggleDialogDoNotAsk(doNotAsk)
        clickObject(By.res("geoShareConfirmationDialogDismissButton"))
        device.wait(Until.gone(selector), timeout)
        assertObjectDoesNotExist(selector)
    }

    protected fun toggleDialogDoNotAsk(doNotAsk: Boolean) {
        if (doNotAsk) {
            clickObject(By.res("geoShareConfirmationDialogDoNotAskSwitch"))
        }
    }

    protected fun clickIntroCloseButton() {
        val button = By.res("geoShareIntroScreenCloseButton")
        waitAndAssertObjectExists(button)
        clickObject(button)
    }

    protected fun setUserPreferenceConnectionPermissionToAlways() {
        clickObject(By.res("geoShareMainMenuButton"))
        val menuButton = By.res("geoShareMainMenuUserPreferences")
        waitAndAssertObjectExists(menuButton)
        clickObject(menuButton)
        val option = By.res("geoShareUserPreferenceConnectionPermissionAlways")
        waitAndAssertObjectExists(option)
        clickObject(option)
    }

    @Suppress("SameParameterValue")
    protected fun clickMapApp(packageName: String) {
        // Open the coordinates with Google Maps
        val googleMapsApp = By.res("geoShareResultCardApp_$packageName")
        waitAndAssertObjectExists(googleMapsApp)
        clickObject(googleMapsApp)
    }

    protected fun assertGoogleMapsHasText(textValue: Pattern) {
        // If there is a Google Maps sign in screen, skip it
        val googleMapsSignInHeadline = By.pkg(googleMapsPackageName).text("Make it your map")
        if (waitForObject(googleMapsSignInHeadline, 5000L)) {
            clickObject(
                By.pkg(googleMapsPackageName).text("skip".toPattern(Pattern.CASE_INSENSITIVE))
            )
        }

        // Verify Google Maps content
        waitAndAssertObjectExists(By.pkg(googleMapsPackageName).text(textValue))
    }
}
