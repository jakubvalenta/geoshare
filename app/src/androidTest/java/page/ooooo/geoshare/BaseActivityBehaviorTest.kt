package page.ooooo.geoshare

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Before
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.Position
import java.lang.Thread.sleep
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.roundToLong

abstract class BaseActivityBehaviorTest {
    companion object {
        const val PACKAGE_NAME = "page.ooooo.geoshare.debug"
        const val LAUNCH_TIMEOUT = 10_000L
        const val TIMEOUT = 10_000L
        const val ELEMENT_DOES_NOT_EXIST_TIMEOUT = 500L
        val NETWORK_TIMEOUT = (1..MAX_RETRIES).fold(CONNECT_TIMEOUT + REQUEST_TIMEOUT) { acc, curr ->
            acc + (2.0.pow(curr - 1) * EXPONENTIAL_DELAY).roundToLong() + CONNECT_TIMEOUT + REQUEST_TIMEOUT
        }
    }

    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

    protected fun launchApplication() = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi.
        executeShellCommand("monkey -p $PACKAGE_NAME 1")

        // Wait for the app to appear
        waitForAppToBeVisible(PACKAGE_NAME, LAUNCH_TIMEOUT)
    }

    protected fun closeApplication() = uiAutomator {
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
                    Until.gone(By.text("clear all".toPattern(Pattern.CASE_INSENSITIVE))), 3_000L
                )
                if (success) {
                    break
                }
            }
        }
    }

    protected fun executeShellCommand(cmd: String) = uiAutomator {
        val output = device.executeShellCommand(cmd)
        Log.i(null, "Executed shell command `$cmd`: $output")
    }

    protected fun waitAndAssertPositionIsVisible(expectedPosition: Position) = uiAutomator {
        onElement(NETWORK_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" || viewIdResourceName == "geoShareConversionErrorMessage" }
        val expectedCoordinatesText = expectedPosition.toNorthSouthWestEastDecCoordsString()
        onElement { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" && textAsString() == expectedCoordinatesText }
        if (!expectedPosition.q.isNullOrEmpty() || !expectedPosition.z.isNullOrEmpty()) {
            val expectedParamsText = expectedPosition.toParamsString()
            onElement { viewIdResourceName == "geoShareConversionSuccessPositionParams" && textAsString() == expectedParamsText }
        } else {
            assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionParams" })
        }
    }

    protected fun shareUri(unsafeUriString: String) {
        // Use shell command instead of startActivity() to support Xiaomi
        executeShellCommand(
            "am start -a android.intent.action.VIEW -d $unsafeUriString -n $PACKAGE_NAME/page.ooooo.geoshare.ConversionActivity $PACKAGE_NAME"
        )
    }

    protected fun goToMenuItem(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement(block = block).click()
    }

    protected fun goToUrlConvertersScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUrlConverters" }
    }

    protected fun goToUserPreferencesScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
    }
}
