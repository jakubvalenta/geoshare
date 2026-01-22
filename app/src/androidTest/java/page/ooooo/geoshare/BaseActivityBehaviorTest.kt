package page.ooooo.geoshare

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.getDescription
import page.ooooo.geoshare.lib.outputs.getText
import page.ooooo.geoshare.lib.position.Position
import kotlin.math.pow
import kotlin.math.roundToLong

abstract class BaseActivityBehaviorTest {

    companion object {
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

    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

    protected fun launchApplication() = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi.
        device.executeShellCommand("monkey -p ${PackageNames.GEO_SHARE_DEBUG} 1")

        // Wait for the app to appear
        waitForAppToBeVisible(PackageNames.GEO_SHARE_DEBUG)
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
                onElement {
                    when (textAsString()) {
                        "Clear", "Effacer" -> true
                        else -> false
                    }
                }.click()
            } else {
                // On Android API >= 28, swipe from the center of the screen towards the upper edge
                device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, 0, 10) }
            }
        } else {
            // On Android API < 28, swipe from the center of the screen towards the bottom edge to reveal "Clear all"
            if (
                onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) {
                    @Suppress("SpellCheckingInspection")
                    when (textAsString()) {
                        "No recent items", "Aucun élément récent" -> true
                        else -> false
                    }
                } != null
            ) {
                // Sometimes it can happen that the recent apps screen shows nothing, so we tap the recent button again
                device.pressRecentApps()
                waitForStableInActiveWindow()
                device.pressRecentApps()
                waitForStableInActiveWindow()
            }
            device.apply { swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, displayHeight, 10) }
            onElement {
                when (textAsString()) {
                    "CLEAR ALL", "TOUT EFFACER" -> true
                    else -> false
                }
            }.click()
        }
        waitForStableInActiveWindow()
    }

    protected fun closeIntro() = uiAutomator {
        waitForStableInActiveWindow()
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
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

    private fun isLocationGrantButton(element: AccessibilityNodeInfo): Boolean =
        @Suppress("SpellCheckingInspection")
        when (element.textAsString()?.lowercase()) {
            "only this time", "uniquement cette fois-ci" -> true
            else -> false
        }

    protected fun grantLocationPermission() = uiAutomator {
        onElement { isLocationGrantButton(this) }.click()
    }

    protected fun grantLocationPermissionIfNecessary() = uiAutomator {
        onElementOrNull(3_000L) { isLocationGrantButton(this) }?.click()
    }

    protected fun denyLocationPermission() = uiAutomator {
        onElement {
            @Suppress("SpellCheckingInspection")
            when (textAsString()?.lowercase()) {
                "don't allow", "don’t allow", "ne pas autoriser" -> true
                else -> false
            }
        }.click()
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
        val coordinatesElement = onElement { viewIdResourceName == "geoShareConversionSuccessPositionCoordinates" }
        assertEquals(expectedText, coordinatesElement.text)
        val expectedName = expectedPosition.points?.lastOrNull()?.name?.replace('+', ' ')
            ?: expectedPosition.points?.size?.takeIf { it > 1 }?.let { "point $it" }
        val nameElement = onElement { viewIdResourceName == "geoShareConversionSuccessPositionName" }
        if (expectedName != null) {
            assertEquals(expectedName, nameElement.text)
        } else {
            assertTrue(
                @Suppress("SpellCheckingInspection")
                when (nameElement.text) {
                    "Coordinates", "Coordonnées" -> true
                    else -> false
                }
            )
        }
        if (!expectedPosition.q.isNullOrEmpty() || expectedPosition.z != null) {
            val expectedDescription = allOutputs.getDescription(expectedPosition)
            val descriptionElement =
                onElement { viewIdResourceName == "geoShareConversionSuccessPositionDescription" }
            assertEquals(expectedDescription, descriptionElement.text)
        } else {
            assertNull(onElementOrNull(ELEMENT_DOES_NOT_EXIST_TIMEOUT) { viewIdResourceName == "geoShareConversionSuccessPositionDescription" })
        }
    }

    protected fun waitAndAssertGoogleMapsContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        // Wait for Google Maps
        onElement { packageName == PackageNames.GOOGLE_MAPS }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) {
            packageName == PackageNames.GOOGLE_MAPS &&
                @Suppress("SpellCheckingInspection")
                when (textAsString()) {
                    "Make it your map", "Profitez d'une carte personnalisée" -> true
                    else -> false
                }
        }?.also {
            onElement {
                packageName == PackageNames.GOOGLE_MAPS &&
                    when (textAsString()?.lowercase()) {
                        "skip", "ignorer" -> true
                        else -> false
                    }
            }.click()
        }

        // Verify Google Maps content
        onElement { packageName == PackageNames.GOOGLE_MAPS && this.block() }
    }

    protected fun waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        // Wait for TomTom
        onElement(30_000L) { packageName == PackageNames.TOMTOM }

        // If there is location permission, grant it
        grantLocationPermissionIfNecessary()

        // If there is Importing GPX tracks dialog, confirm it
        onElementOrNull(5_000L) {
            @Suppress("SpellCheckingInspection")
            when (textAsString()) {
                "Got it", "J'ai compris" -> true
                else -> false
            }
        }?.click()

        // Verify TomTom content
        onElement { packageName == PackageNames.TOMTOM && this.block() }
    }

    protected fun shareUri(unsafeUriString: String) = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi
        device.executeShellCommand(
            @Suppress("SpellCheckingInspection")
            "am start -a android.intent.action.VIEW -d $unsafeUriString -n ${PackageNames.GEO_SHARE_DEBUG}/page.ooooo.geoshare.ConversionActivity ${PackageNames.GEO_SHARE_DEBUG}"
        )
    }

    protected fun goToMenuItem(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement(block = block).click()
    }

    protected fun goToInputsScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuInputs" }
    }

    protected fun goToUserPreferencesScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
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
