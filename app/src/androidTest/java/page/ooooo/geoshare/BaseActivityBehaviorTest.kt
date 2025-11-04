package page.ooooo.geoshare

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.XiaomiTools
import page.ooooo.geoshare.lib.outputs.allOutputManagers
import page.ooooo.geoshare.lib.outputs.getOutputs
import page.ooooo.geoshare.lib.outputs.getSupportingText
import page.ooooo.geoshare.lib.outputs.getText
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
        device.executeShellCommand("monkey -p $PACKAGE_NAME 1")

        // Wait for the app to appear
        waitForAppToBeVisible(PACKAGE_NAME, LAUNCH_TIMEOUT)
    }

    protected fun closeApplication() = uiAutomator {
        assertNotXiaomi()
        device.pressRecentApps()
        waitForStableInActiveWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val menu = onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { textAsString() == "Geo Share" }
            if (menu != null) {
                // On Android API >= 36.1, use the dropdown menu
                menu.click()
                onElement { textAsString() == "Clear" }.click()
            } else {
                // On Android API >= 28, swipe from the center of the screen towards the upper edge
                device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, 0, 10) }
            }
        } else {
            // On Android API < 28, swipe from the center of the screen towards the bottom edge to reveal "Clear all"
            if (onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { textAsString() == "No recent items" || textAsString() == "Aucun élément récent" } != null) {
                // Sometimes it can happen that the recent apps screen shows nothing, so we tap the recent button again
                device.pressRecentApps()
                waitForStableInActiveWindow()
                device.pressRecentApps()
                waitForStableInActiveWindow()
            }
            device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, displayHeight, 10) }
            // waitForStableInActiveWindow()
            onElement { textAsString() == "CLEAR ALL" || textAsString() == "TOUT EFFACER" }.click()
        }
        waitForStableInActiveWindow()
    }

    protected fun closeIntro() = uiAutomator {
        waitForStableInActiveWindow()
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }


    protected fun assertGoogleMapsInstalled() = uiAutomator {
        assertTrue(
            "This test only works when Google Maps is installed on the device",
            device.executeShellCommand("pm path $GOOGLE_MAPS_PACKAGE_NAME").isNotEmpty(),
        )
    }

    protected fun assertNotXiaomi() = uiAutomator {
        assertFalse(
            "We cannot close the app on Xiaomi MIUI, because it stops the tests",
            XiaomiTools.isMiuiDevice(),
        )
    }

    protected fun waitAndAssertPositionIsVisible(expectedPosition: Position) = uiAutomator {
        onElement(NETWORK_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" || viewIdResourceName == "geoShareConversionErrorMessage" }
        val outputs = allOutputManagers.getOutputs(expectedPosition, emptyList())
        onElement { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" && textAsString() == outputs.getText() }
        if (!expectedPosition.q.isNullOrEmpty() || !expectedPosition.z.isNullOrEmpty()) {
            onElement { viewIdResourceName == "geoShareConversionSuccessPositionParams" && textAsString() == outputs.getSupportingText() }
        } else {
            assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionParams" })
        }
    }

    protected fun shareUri(unsafeUriString: String) = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi
        device.executeShellCommand(
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
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_AUTOMATION" }.click()
    }

    protected fun goToUserPreferencesDetailConnectionPermissionScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_CONNECTION_PERMISSION" }.click()
    }

    protected fun goToUserPreferencesDetailDeveloperScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_DEVELOPER_OPTIONS" }.click()
    }
}
