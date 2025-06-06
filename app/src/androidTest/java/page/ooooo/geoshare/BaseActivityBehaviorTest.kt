package page.ooooo.geoshare

import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Before
import page.ooooo.geoshare.lib.XiaomiTools
import java.lang.Thread.sleep
import java.util.regex.Pattern

open class BaseActivityBehaviorTest {

    protected lateinit var device: UiDevice

    protected val packageName = "page.ooooo.geoshare.debug"
    protected val googleMapsPackageName = "com.google.android.apps.maps"
    protected val launchTimeout = 10_000L
    protected val timeout = 10_000L

    private var xiaomiTools: XiaomiTools = XiaomiTools()

    @Before
    fun goToLauncher() {
        // Initialize UiDevice instance
        device =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

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
                device.displayWidth / 2,
                device.displayHeight / 2,
                device.displayWidth / 2,
                0,
                5
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
                    Until.gone(By.text("clear all".toPattern(Pattern.CASE_INSENSITIVE))),
                    3000L
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

    protected fun clickObject(selector: BySelector) {
        device.findObject(selector)?.click()
    }

    protected fun waitAndClickObject(selector: BySelector) {
        waitAndAssertObjectExists(selector)
        clickObject(selector)
    }

    protected fun waitAndConfirmDialogAndAssertNewWindowIsOpen(
        selector: BySelector,
        doNotAsk: Boolean = false,
    ) {
        waitAndAssertObjectExists(selector)
        toggleDialogDoNotAsk(doNotAsk)
        device.findObject(By.res("geoShareConfirmationDialogConfirmButton"))
            ?.clickAndWait(Until.newWindow(), timeout)
    }

    protected fun waitAndDismissDialogAndAssertItIsClosed(
        selector: BySelector,
        doNotAsk: Boolean = false,
    ) {
        waitAndAssertObjectExists(selector)
        toggleDialogDoNotAsk(doNotAsk)
        device.findObject(By.res("geoShareConfirmationDialogDismissButton"))
            ?.click()
        device.wait(Until.gone(selector), timeout)
        assertObjectDoesNotExist(selector)
    }

    protected fun toggleDialogDoNotAsk(doNotAsk: Boolean) {
        if (doNotAsk) {
            device.findObject(By.res("geoShareConfirmationDialogDoNotAskSwitch"))
                ?.click()
        }
    }

    protected fun waitAndAssertGoogleMapsTextExists(textValue: Pattern) {
        // Grant Xiaomi permission
        if (xiaomiTools.isMiuiDevice()) {
            val xiaomiPermissionDialogSelector =
                By.res("geoShareXiaomiPermissionDialog")
            if (waitForObject(xiaomiPermissionDialogSelector, 1000L)) {
                waitAndConfirmDialogAndAssertNewWindowIsOpen(
                    xiaomiPermissionDialogSelector
                )
                clickObject(By.text("Other permissions"))
                waitAndClickObject(By.textStartsWith("Display pop-up windows while"))
                waitAndClickObject(By.text("Always allow"))
                device.pressBack()
                device.pressBack()
                device.pressBack()
            }
        }

        // If the share menu opens, Select Google Maps from it
        val shareMenuHeadingSelector = By.textStartsWith("Open with")
        if (waitForObject(shareMenuHeadingSelector, 2000L)) {
            if (device.hasObject(By.text("Open with Maps"))) {
                clickObject(By.text("Just once"))
            } else {
                clickObject(By.text("Maps"))
                clickObject(By.text("Just once"))
            }
        }

        // If there is a Google Maps sign in screen, skip it
        val googleMapsSignInHeadline =
            By.pkg(googleMapsPackageName).text("Make it your map")
        if (waitForObject(googleMapsSignInHeadline, 5000L)) {
            clickObject(
                By.pkg(googleMapsPackageName)
                    .text("skip".toPattern(Pattern.CASE_INSENSITIVE))
            )
        }

        // Verify Google Maps content
        waitAndAssertObjectExists(By.pkg(googleMapsPackageName).text(textValue))
    }

    protected fun assertGoogleMapsDoesNotOpenWithinTimeout() {
        assertFalse(
            waitForObject(By.pkg(googleMapsPackageName).depth(0), 3000L)
        )
    }
}
