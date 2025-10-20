package page.ooooo.geoshare

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertNull
import org.junit.Before
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.XiaomiTools
import kotlin.math.pow
import kotlin.math.roundToLong

abstract class BaseActivityBehaviorTest {
    companion object {
        const val PACKAGE_NAME = "page.ooooo.geoshare.debug"
        const val LAUNCH_TIMEOUT = 10_000L
        const val TIMEOUT = 10_000L
        const val ELEMENT_DOES_NOT_EXIST_TIMEOUT = 500L
        val NETWORK_TIMEOUT = (1..MAX_RETRIES).fold(CONNECT_TIMEOUT + REQUEST_TIMEOUT) { acc, curr ->
            acc + (EXPONENTIAL_DELAY_BASE.pow(curr - 1) * EXPONENTIAL_DELAY_BASE_DELAY).roundToLong() + CONNECT_TIMEOUT + REQUEST_TIMEOUT
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
        waitForStableInActiveWindow()
        if (XiaomiTools.isMiuiDevice()) {
            throw Exception("We cannot close the app on Xiaomi MIUI, because it stops the tests")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // On newer Android, swipe from the center of the screen towards the upper edge
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight / 2,
                device.displayWidth / 2,
                0,
                10,
            )
        } else {
            // On older Android, swipe from the bottom left corner of the screen towards the right edge
            device.swipe(
                (device.displayWidth * 0.1).toInt(),
                (device.displayHeight * 0.8).toInt(),
                device.displayWidth,
                (device.displayHeight * 0.8).toInt(),
                5,
            )
        }
        waitForStableInActiveWindow()
    }

    protected fun executeShellCommand(cmd: String) = uiAutomator {
        val output = device.executeShellCommand(cmd)
        Log.i(null, "Executed shell command `$cmd`: $output")
    }

    protected fun closeIntroIfItIsVisible() = uiAutomator {
        onElementOrNull(1000) { viewIdResourceName == "geoShareIntroScreenCloseButton" }?.click()
    }

    protected fun waitAndAssertPositionIsVisible(expectedPosition: Position) = uiAutomator {
        onElement(NETWORK_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" || viewIdResourceName == "geoShareConversionErrorMessage" }
        val expectedCoordinatesText = expectedPosition.toNorthSouthWestEastDecCoordsString()
        onElement { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" && textAsString() == expectedCoordinatesText }
        if (!expectedPosition.q.isNullOrEmpty() || !expectedPosition.z.isNullOrEmpty()) {
            val expectedParamsText = expectedPosition.toParamsString("\t\t")
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

    private fun goToUserPreferencesScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
    }

    protected fun goToUserPreferencesDetailAutomationScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesAutomationItem" }.click()
    }

    protected fun goToUserPreferencesDetailConnectionPermissionScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesConnectionPermissionItem" }.click()
    }

    protected fun goToUserPreferencesDetailDeveloperScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesDeveloperItem" }.click()
    }
}
