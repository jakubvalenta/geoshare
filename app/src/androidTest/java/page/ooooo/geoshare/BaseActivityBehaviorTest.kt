package page.ooooo.geoshare

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.*
import org.junit.Before
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.AndroidTools.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.getDescription
import page.ooooo.geoshare.lib.outputs.getText
import page.ooooo.geoshare.lib.position.Position
import kotlin.math.pow
import kotlin.math.roundToLong

abstract class BaseActivityBehaviorTest {

    companion object {
        @Suppress("SpellCheckingInspection")
        const val PACKAGE_NAME = "page.ooooo.geoshare.debug"
        const val ELEMENT_DOES_NOT_EXIST_TIMEOUT = 500L
        val NETWORK_TIMEOUT = (1..MAX_RETRIES).fold(CONNECT_TIMEOUT + REQUEST_TIMEOUT) { acc, curr ->
            acc + (EXPONENTIAL_DELAY_BASE.pow(curr - 1) * EXPONENTIAL_DELAY_BASE_DELAY).roundToLong() + CONNECT_TIMEOUT + REQUEST_TIMEOUT
        }
    }

    class DialogElement(val dialog: UiObject2) {
        fun confirm() {
            dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }.click()
        }

        fun dismiss() {
            dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogDismissButton" }.click()
        }

        fun toggleDoNotAsk() {
            dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogDoNotAskSwitch" }.click()
        }
    }

    protected fun onDialog(
        resourceName: String,
        timeoutMs: Long = 10_000L,
        block: DialogElement.() -> Unit,
    ) = uiAutomator {
        val dialog = onElement(timeoutMs) { viewIdResourceName == resourceName }
        DialogElement(dialog).block()
        assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == resourceName })
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
        waitForAppToBeVisible(PACKAGE_NAME)
    }

    protected fun closeApplication() = uiAutomator {
        assertFalse(
            "We cannot close the app on Xiaomi MIUI, because it stops the tests",
            AndroidTools.isMiuiDevice(),
        )
        device.pressRecentApps()
        waitForStableInActiveWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val menu = onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { textAsString() == "Geo Share" }
            if (menu != null) {
                // On Android API >= 36.1, use the dropdown menu
                menu.click()
                onElement { textAsString() in listOf("Clear", "Effacer") }.click()
            } else {
                // On Android API >= 28, swipe from the center of the screen towards the upper edge
                device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, 0, 10) }
            }
        } else {
            // On Android API < 28, swipe from the center of the screen towards the bottom edge to reveal "Clear all"
            if (
                @Suppress("SpellCheckingInspection")
                onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                    textAsString() in listOf("No recent items", "Aucun élément récent")
                } != null
            ) {
                // Sometimes it can happen that the recent apps screen shows nothing, so we tap the recent button again
                device.pressRecentApps()
                waitForStableInActiveWindow()
                device.pressRecentApps()
                waitForStableInActiveWindow()
            }
            device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, displayHeight, 10) }
            onElement { textAsString() in listOf("CLEAR ALL", "TOUT EFFACER") }.click()
        }
        waitForStableInActiveWindow()
    }

    protected fun closeIntro() = uiAutomator {
        waitForStableInActiveWindow()
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }

    protected fun assertAppInstalled(packageName: String) = uiAutomator {
        assertTrue(
            "This test only works when $packageName is installed on the device",
            device.executeShellCommand("pm path $packageName").isNotEmpty(),
        )
    }

    protected fun waitAndAssertPositionIsVisible(expectedPosition: Position) = uiAutomator {
        onElement(NETWORK_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" || viewIdResourceName == "geoShareConversionErrorMessage" }
        val expectedText = allOutputs.getText(expectedPosition, null)
        onElement { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" && textAsString() == expectedText }
        val expectedName = expectedPosition.mainPoint?.name?.replace('+', ' ')
            ?: expectedPosition.pointCount.takeIf { it > 1 }?.let { "point $it" }
        if (expectedName != null) {
            onElement { viewIdResourceName == "geoShareConversionSuccessPositionName" && textAsString() == expectedName }
        } else {
            onElement {
                viewIdResourceName == "geoShareConversionSuccessPositionName" && textAsString() in listOf(
                    "Coordinates",
                    @Suppress("SpellCheckingInspection")
                    "Coordonnées",
                )
            }
        }
        if (!expectedPosition.q.isNullOrEmpty() || expectedPosition.z != null) {
            val expectedDescription = allOutputs.getDescription(expectedPosition)
            onElement { viewIdResourceName == "geoShareConversionSuccessPositionDescription" && textAsString() == expectedDescription }
        } else {
            assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionDescription" })
        }
    }

    protected fun waitAndAssertGoogleMapsContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        // Wait for Google Maps
        onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) {
            packageName == GOOGLE_MAPS_PACKAGE_NAME && textAsString() in listOf(
                "Make it your map",
                @Suppress("SpellCheckingInspection")
                "Profitez d'une carte personnalisée",
            )
        }?.also {
            onElement {
                packageName == GOOGLE_MAPS_PACKAGE_NAME && textAsString()?.lowercase() in listOf(
                    "skip",
                    "ignorer",
                )
            }.click()
        }

        // Verify Google Maps content
        onElement { packageName == GOOGLE_MAPS_PACKAGE_NAME && this.block() }
    }

    protected fun waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        // Wait for TomTom
        onElement(20_000L) { packageName == GpxOutput.TOMTOM_PACKAGE_NAME }

        // If there is location permission, grant it
        onElementOrNull(3_000L) {
            textAsString() in listOf(
                "Only this time",
                @Suppress("SpellCheckingInspection") "Uniquement cette fois-ci",
            )
        }?.click()

        // If there is Importing GPX tracks dialog, confirm it
        onElementOrNull(3_000L) {
            textAsString() in listOf(
                "Got it",
                @Suppress("SpellCheckingInspection") "J'ai compris",
            )
        }?.click()

        // Verify TomTom content
        onElement { packageName == GpxOutput.TOMTOM_PACKAGE_NAME && this.block() }
    }

    protected fun shareUri(unsafeUriString: String) = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi
        device.executeShellCommand(
            @Suppress("SpellCheckingInspection")
            "am start -a android.intent.action.VIEW -d $unsafeUriString -n $PACKAGE_NAME/page.ooooo.geoshare.ConversionActivity $PACKAGE_NAME"
        )
    }

    protected fun goToMenuItem(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement(block = block).click()
    }

    protected fun goToInputsScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuInputs" }
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
